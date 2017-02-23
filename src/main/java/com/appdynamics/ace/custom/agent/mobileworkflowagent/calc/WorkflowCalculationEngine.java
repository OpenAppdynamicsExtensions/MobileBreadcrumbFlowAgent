package com.appdynamics.ace.custom.agent.mobileworkflowagent.calc;

import com.appdynamics.ace.custom.agent.mobileworkflowagent.dto.ApplicationInstance;
import com.appdynamics.ace.custom.agent.mobileworkflowagent.dto.LoginConfig;
import com.appdynamics.ace.custom.agent.mobileworkflowagent.dto.WorkflowConfig;
import com.appdynamics.ace.custom.agent.mobileworkflowagent.dto.WorkflowInstance;
import de.appdynamics.client.eventservice.adql.*;
import de.appdynamics.client.eventservice.adql.api.ADQLDataListener;
import de.appdynamics.client.eventservice.adql.api.FilterState;
import de.appdynamics.client.eventservice.adql.dto.PayloadDataElement;
import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


/**
 * Created by stefan.marx on 22.02.17.
 */
public class WorkflowCalculationEngine {
    public static final int LIMIT = 1000;
    private final int _sessionTimeout;
    private final HashMap<String, WorkflowSessionState> _sessions;
    private ADQLConnection _adqlConnection = null;
    private WorkflowConfig _cfg;
    private Date _startDate;
    private Date _endDate;

    private static String queryTmpl = "SELECT sessionguid,appkey,eventTimestamp,endTimeMS,breadcrumb,closed " +
            " FROM mobile_session_record " +
            " WHERE appkey='%%APPKEY%%' " +
            " AND closed = true " +
            " AND endTimeMS>%%START%% " +
            " AND endTimeMS<%%END%%";

    static Logger _logger =  Logger.getLogger(WorkflowCalculationEngine.class);
    private long _rowcount = 0;


    public WorkflowCalculationEngine(WorkflowConfig cfg, Date startDate, Date endDate) throws AnalyticsException {
        _cfg = cfg;
        _startDate = startDate;
        _endDate = endDate;
        _sessionTimeout = 1000*60*60*1;
        _sessions = new HashMap<String,WorkflowSessionState>();
        try {

            ADQLConnectionBuilder builder = new ADQLConnectionBuilder();
            LoginConfig loginCfg = cfg.getLoginConfig();
            if (loginCfg.hasUrl()) {
                builder.withUri(new URI(cfg.getLoginConfig().getUrl()));
            }

            if (loginCfg.isBasicLogin()) {
                builder.withBasicAuth(loginCfg.getUser(),loginCfg.getPasswd());
            } else {
                builder.withTokenAuth(loginCfg.getAccount(),loginCfg.getToken());
            }

            _adqlConnection = builder.build();
        } catch (URISyntaxException e) {
            _logger.error("Error while creating ADQL Client container.",e);
            throw new AnalyticsException("URL Wrong",e);
        } catch (AnalyticsException e) {
            _logger.error("Error while creating ADQL Client container.",e);
            throw new AnalyticsException("Connection Problem",e);
        }

    }

    public WorkflowCalculationEngine(WorkflowConfig cfg) throws AnalyticsException {
        this(cfg,new Date(System.currentTimeMillis()-(1000l*60*60)),new Date());
    }

    public ArrayList<MetricValueContainer> execute() throws AnalyticsException {
        return this.execute(_startDate,_endDate);
        
    }

    public ArrayList<MetricValueContainer> execute(Date startDate, Date endDate) throws AnalyticsException {
        _rowcount = 0;

        ArrayList<MetricValueContainer> metrics = new ArrayList<MetricValueContainer>();

        _logger.debug("Collecting Workflow Stats between "+startDate+" and "+endDate);
        // Iterate on possible Applications
        for (ApplicationInstance app : _cfg.getApps()) {
            _logger.debug("Collecting from App :"+app.getAppKey());

            String query = buildQueryString(app.getAppKey(),startDate,endDate);

            MetricHolder holder = new MetricHolder(app.getAppName());
            ADQLDelegate delegate = new ADQLDelegate(holder, app);
            ADQLQuery q = _adqlConnection.query()
                    .withADQL(query)
                    .withDataListener(delegate)
                    .withLimit(LIMIT);
            ADQLResult res = q.execute(true);
            _rowcount+=res.getPayload().getTotal();

            while (res.isPartialComplete()) {
                try {
                    _logger.debug("Continuing --- fetch additional results ("+LIMIT+"/"+res.getPayload().getTotal()+")");
                    List<PayloadDataElement> data = res.getPayload().getAllData();
                    Date et = data.get(data.size()-1).getDate("eventTimestamp");
                    res = q.continueQuery(et,true,data.get(data.size()-1));
                } catch (Exception ex ) {
                    _logger.error("Limit reached, continuing Transaction not possible because Field eventTimestamp not found.");
                    break;
                }
            }

            // cleanup
            delegate = null;
            metrics.addAll(holder.getMetrics());
            holder.close();



        }


        return metrics;
    }

    private String buildQueryString(String appKey,Date startDate, Date endDate) {


        String query = queryTmpl;
        query = query.replaceAll("%%APPKEY%%",appKey);
        query = query.replaceAll("%%START%%",Long.toString(startDate.getTime()));
        query = query.replaceAll("%%END%%",Long.toString(endDate.getTime()));
        _logger.debug("QueryString :"+query);
        return query;

    }


    private boolean hasSession(String sessionguid) {
        return _sessions.containsKey(sessionguid);
    }


    private WorkflowSessionState openSession(String sessionguid) throws AnalyticsException {
        if (hasSession(sessionguid)) throw new AnalyticsException("Invalid State, session allready opened:"+sessionguid);

        WorkflowSessionState session = new WorkflowSessionState(sessionguid);
        _sessions.put(sessionguid,session);
        return session;


    }

    public void purgeOldSessions (Date purgeDate) {
        ArrayList<String> sessionsToRemove = new ArrayList<String>();

        for (Map.Entry<String,WorkflowSessionState> entry:_sessions.entrySet()) {
            if (entry.getValue().getEnd().getTime()<purgeDate.getTime()) sessionsToRemove.add(entry.getKey());
        }

        int count = sessionsToRemove.size();

        for (String k : sessionsToRemove) {
            _sessions.remove(k);
        }

        _logger.debug("Purged "+count+" Sessions from cache!");
    }

    public String getState() {
        return "Current tracking "+_sessions.size()+ " Sessions. \n" +
                "Last Execution retrieved "+_rowcount+ " Datasets !";
    }


    private class ADQLDelegate implements ADQLDataListener {
        private MetricHolder _metrics;
        private ApplicationInstance _app;

        public ADQLDelegate(MetricHolder metrics, ApplicationInstance app) {
            _metrics = metrics;
            _app = app;
        }

        @Override
        public FilterState filter(PayloadDataElement data) throws AnalyticsException {
            if (WorkflowCalculationEngine.this.hasSession(data.getString("sessionguid"))) return FilterState.SKIP;
            else return FilterState.NONE;
        }

        @Override
        public void process(PayloadDataElement data) throws AnalyticsException {

            // only continue if there are any breadcrumbs
            List<PayloadDataElement> bc = data.getChildren("breadcrumb");
            if (bc.size()>0) {
                _logger.debug("Processing session :"+data.getString("sessionguid")+ "  started "+data.getDate("eventTimestamp"));
                WorkflowSessionState sessionState = WorkflowCalculationEngine.this.openSession(data.getString("sessionguid"));

                sessionState.setStart(data.getDate("eventTimestamp"));
                sessionState.setEnd(new Date(data.getLong("endTimeMS")));

                for (WorkflowInstance wf : _app.getWorkflowInstances()) {
                    WorkflowState wfState = sessionState.getWorkflowState(wf.getFlowName());

                    for ( PayloadDataElement bcData : bc) {
                        // Iterate all breadcrumbs (should be sorted by date)
                        String bcName = bcData.getString("bctext");
                        long bcTime = bcData.getLong("clienttime");

                        _logger.trace(wf.getFlowName()+"-->"+bcName);
                        if (wf.isStartState(bcName)) {
                            switch (wfState.getState()) {
                                case NONE:
                                    wfState.start(bcName,bcTime);
                                    break;
                                case ACTIVE:
                                    wfState.restart(bcName,bcTime);
                                    _logger.debug("Restarted WF because of "+bcName+" ("+wf.getFlowName()+")");
                                    break;
                                case CLOSED:
                                    wfState.start(bcName,bcTime);
                                    break;
                            }
                        }
                        if (wf.isValidState(bcName)) {
                            switch (wfState.getState()) {
                                case NONE:
                                    _logger.warn("Ignoring Valid State, WF not started "+bcName+" ( "+data.getString("sessionguid")+")");
                                    break;
                                case ACTIVE:
                                    wfState.update(bcName,bcTime);
                                    break;
                                case CLOSED:
                                    _logger.warn("Ignoring Valid State, WF closed "+bcName+" ( "+data.getString("sessionguid")+")");
                                    break;
                            }
                        }
                        if (wf.isEndState(bcName)) {
                            switch (wfState.getState()) {
                                case NONE:
                                    _logger.warn("Ignoring END State, WF not started "+bcName+" ( "+data.getString("sessionguid")+")");
                                    break;
                                case ACTIVE:
                                    _metrics.reportOkFlow(wf.getFlowName(),wfState.flowTime(bcTime));
                                    wfState.stop();
                                    break;
                                case CLOSED:
                                    _logger.warn("Ignoring END State, WF closed "+bcName+" ( "+data.getString("sessionguid")+")");
                                    break;
                            }
                        }
                        if (wf.isErrorState(bcName)) {
                            switch (wfState.getState()) {
                                case NONE:
                                    _logger.warn("Ignoring ERROR State, WF not started "+bcName+" ( "+data.getString("sessionguid")+")");
                                    break;
                                case ACTIVE:
                                    _metrics.reportErrorFlow(wf.getFlowName(),wfState.flowTime(bcTime));
                                    wfState.stop();
                                    break;
                                case CLOSED:
                                    _logger.warn("Ignoring ERROR State, WF closed "+bcName+" ( "+data.getString("sessionguid")+")");
                                    break;
                            }
                        }

                        if (wfState.getState() == WorkflowState.State.ACTIVE) {
                            if (wfState.age(bcTime) > wf.getTimeoutMs()) {
                                _metrics.reportTimeoutFlow(wf.getFlowName(),wfState.age(bcTime));
                                if (wf.isShouldLogTimeoutSessions()) {
                                    _logger.warn("Session with Timeout status on flow definition "+wf.getFlowName()
                                            +" App:"+ _app.getAppName()
                                            +"  Session:"+data.getString("sessionguid"));
                                }
                                wfState.stop();
                            }
                        }
                    }
                    if(wfState.getState() == WorkflowState.State.ACTIVE) {
                        _metrics.reportStaleFlow(wf.getFlowName(),wfState.age(data.getLong("endTimeMS")));
                        if (wf.isShouldLogTimeoutSessions()) {
                            _logger.warn("Session with stale status on flow definition "+wf.getFlowName()
                                    +" App:"+ _app.getAppName()
                                    +"  Session:"+data.getString("sessionguid"));
                        }
                    }
                }
                sessionState.removeTempData();
            }



        }
    }



}
