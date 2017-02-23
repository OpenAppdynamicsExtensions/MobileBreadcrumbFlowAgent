package com.appdynamics.ace.custom.agent.mobileworkflowagent;

import com.appdynamics.ace.custom.agent.mobileworkflowagent.calc.MetricValueContainer;
import com.appdynamics.ace.custom.agent.mobileworkflowagent.calc.WorkflowCalculationEngine;
import com.appdynamics.ace.custom.agent.mobileworkflowagent.dto.WorkflowConfig;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import de.appdynamics.client.eventservice.adql.AnalyticsException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Random;

/**
 * Createdkjj with IntelliJ IDEA.
 * User: stefan.marx
 * Date: 09.10.13
 * Time: 14:53
 * To change this template use File | Settings | File Templates.
 */
public class DBWorkflowAgent extends AManagedMonitor {

    private static Logger _logger = Logger.getLogger(DBWorkflowAgent.class);
    private final Random rnd;
    int run = 0;




    private final static String metricPrefix = "Custom Metrics|MySimpleAgent|MyShelter|";
    private String _metricPrefix;
    private long _delay;
    private long _windowSize;
    private String _config;
    private WorkflowCalculationEngine _engine;
    private long _purgeDelay;


    public DBWorkflowAgent() {
        _logger.log(Level.INFO," LOADED !!!!");
         rnd = new Random();

    }

    public TaskOutput execute(Map<String, String> arguments, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {

//      <!-- Config file to read from -->
//      <argument name='config' default-value='./config.yml' is-required='true' />
//		<!-- newest sessions to retrieve in seconds (default:15 minutes old) -->
//      <argument name='delay' default-value='900' is-required='true' />
//		<!-- Session window (oldesSession - newestSession in seconds) -->
//      <argument name='windowSize' default-value='300' is-required='true' />
//		<!-- Custom Metrics prefix.
//        Use 'Server|Component:<id>' with the ID of the tier that this Agent bounds to
//        to make this a tier private metric. -->
//      <argument name='metricPrefix' default-value='Custom Metrics' is-required='true' />


        try {
            _metricPrefix = arguments.get("metricPrefix");
            _windowSize = Long.parseLong(arguments.get("windowSize"));
            _delay = Long.parseLong(arguments.get("delay"));
            _purgeDelay = Long.parseLong(arguments.get("purgeSessions"));
            _config = arguments.get("config");




            if (_engine == null ) {
                File taskDir = new File(taskExecutionContext.getTaskDir());
                WorkflowConfig cfg = WorkflowConfig.readFromFile(new File(taskDir,_config).getAbsolutePath());
                _logger.info("Using CFG from :"+_config+" \n"+cfg);

                _engine = new WorkflowCalculationEngine(cfg);
                _logger.info("Engine created  !");
            }

        } catch (Throwable t) {
            _logger.error("Problem while reading config Data !!!",t);
            throw new TaskExecutionException("Problem while reading config Data",t);
        }

        // start Execution
        long startMS = System.currentTimeMillis();
        try {
            Date startTime = new Date(System.currentTimeMillis() - ((_delay + _windowSize)*1000));
            Date endTime = new Date(System.currentTimeMillis() - (_delay*1000));

            ArrayList<MetricValueContainer> metrics = _engine.execute(startTime, endTime);
            writeMetrics(metrics);
        } catch (AnalyticsException e) {
            throw new TaskExecutionException("Error while executing run.",e);
        }
        long endMS = System.currentTimeMillis();



        _engine.purgeOldSessions(new Date (System.currentTimeMillis()-(_purgeDelay*1000)));


        _logger.debug("Execution ended after "+(endMS-startMS)+"ms : STATE : \n"+_engine.getState());
        return new TaskOutput("Metric Upload Complete");

    }

    private void writeMetrics(ArrayList<MetricValueContainer> metrics) {
        for (MetricValueContainer value : metrics) {
            String path = value.getPath();
            path = fixPath(path);

            _logger.debug("Send Metric to new Path  "+path+"\n  --> "+value);
            getMetricWriter(path,value.getAggregation(),value.getTimeRollup(),value.getCluster())
                    .printMetric(""+value.getValue());
        }
    }

    private String fixPath(String path) {
        if (path.startsWith("Custom Metrics|") || path.startsWith("Server|")) return path;
        else {
            return _metricPrefix+"|"+path;
        }
    }


}

