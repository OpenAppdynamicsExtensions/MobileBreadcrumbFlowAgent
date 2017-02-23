package com.appdynamics.ace.custom.agent.mobileworkflowagent.calc;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by stefan.marx on 23.02.17.
 */
public class MetricHolder {
    private String _appName;

    private HashMap<String,FlowMetricHolder> _flowStats = new HashMap();

    static Logger _logger =  Logger.getLogger(MetricHolder.class);


    public MetricHolder(String appName) {
        _appName = appName;
    }

    public void reportOkFlow(String bcName, long time) {
        _logger.trace("Report OK "+bcName+" ("+time+")");
        getFlowHolder(bcName).reportOK(time);
    }

    public void reportErrorFlow(String bcName, long time) {
        _logger.trace("Report Error "+bcName+" ("+time+")");
        getFlowHolder(bcName).reportError(time);
    }

    public void reportStaleFlow(String bcName, long time) {
        _logger.trace("Report Stale "+bcName+" ("+time+")");
        getFlowHolder(bcName).reportStale(time);
    }
    public void reportTimeoutFlow(String bcName, long time) {
        _logger.trace("Report Timeout "+bcName+" ("+time+")");
        getFlowHolder(bcName).reportTimeout(time);
    }
    private FlowMetricHolder getFlowHolder(String flowName) {
        FlowMetricHolder flow = null;
        if (_flowStats.containsKey(flowName)) {
            flow = _flowStats.get(flowName);
        } else {
            flow = new FlowMetricHolder(_appName,flowName);
            _flowStats.put(flowName,flow);
        }
        return flow;
    }

    public void close() {
        _flowStats.clear();
    }

    public ArrayList<MetricValueContainer> getMetrics() {
        ArrayList<MetricValueContainer> metrics = new ArrayList<>();
        for (FlowMetricHolder h: _flowStats.values()) {
            metrics.addAll(h.getMetrics());

        }
        return metrics ;
    }

    private class FlowMetricHolder {
        ArrayList<Long> _ok = new ArrayList<>();
        ArrayList<Long> _error = new ArrayList<>();
        ArrayList<Long> _stale = new ArrayList<>();
        ArrayList<Long> _timeout = new ArrayList<>();
        private String _appName;
        private String _name;

        public FlowMetricHolder(String appName, String name) {
            _appName = appName;
            _name = name;
        }

        public void reportOK(long time) {
            _ok.add(new Long(time));
        }

        public void reportError(long time) {
            _error.add(new Long(time));
        }

        public void reportStale(long time) {
            _stale.add(new Long(time));
        }
        public void reportTimeout(long time) {
            _timeout.add(new Long(time));
        }

        public Collection<? extends MetricValueContainer> getMetrics() {

            Collection<MetricValueContainer> metrics = new ArrayList<>();

            metrics.add(createSumMetric("normalExecutions",_ok.size()));
            metrics.add(createSumMetric("errorExecutions",_error.size()));
            metrics.add(createSumMetric("timeoutExecutions",_timeout.size()));
            metrics.add(createSumMetric("staleExecutions",_stale.size()));
            metrics.add(createSumMetric("totalExecutions",_stale.size()+_error.size()+_timeout.size()+_ok.size()));

            //TODO: Add time metrics
            //TODO: Add State specific metrics


            return metrics ;
        }

        private MetricValueContainer createSumMetric(String metricName, long size) {
            return new MetricValueContainer(createMetricPath(metricName),size
                    ,MetricValueContainer.SUM,MetricValueContainer.SUM,MetricValueContainer.COLLECTIVE);
        }

        private String createMetricPath(String metricName) {
            return _appName+MetricValueContainer.SEPERATOR+_name+MetricValueContainer.SEPERATOR+metricName;
        }
    }

}
