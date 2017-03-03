package com.appdynamics.ace.custom.agent.mobileworkflowagent.calc;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public void reportOkFlow(String flowName, long time) {
        _logger.trace("Report OK "+flowName+" ("+time+")");
        getFlowHolder(flowName).reportOK(time);
    }

    public void reportErrorFlow(String flowName, long time) {
        _logger.trace("Report Error "+flowName+" ("+time+")");
        getFlowHolder(flowName).reportError(time);
    }

    public void reportStaleFlow(String flowName, long time) {
        _logger.trace("Report Stale "+flowName+" ("+time+")");
        getFlowHolder(flowName).reportStale(time);
    }
    public void reportTimeoutFlow(String flowName, long time) {
        _logger.trace("Report Timeout "+flowName+" ("+time+")");
        getFlowHolder(flowName).reportTimeout(time);
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

    public void reportErrorState(String flowName, String state) {
        getFlowHolder(flowName).reportErrorState(state);
    }

    public void reportTimeoutState(String flowName, String state) {
        getFlowHolder(flowName).reportTimeoutState(state);
    }

    public void reportStaleState(String flowName, String state) {
        getFlowHolder(flowName).reportStaleState(state);
    }

    private class FlowMetricHolder {
        ArrayList<Long> _ok = new ArrayList<>();
        ArrayList<Long> _error = new ArrayList<>();
        ArrayList<Long> _stale = new ArrayList<>();
        ArrayList<Long> _timeout = new ArrayList<>();
        HashMap<String,Long> _errorStates = new HashMap<>();
        HashMap<String,Long> _timeoutStates = new HashMap<>();
        HashMap<String,Long> _staleStates = new HashMap<>();

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

            if (_ok.size() >0) metrics.add(createAvgMetric("responseTimeMS",(long) calcAverageValue(_ok)));

            metrics.addAll(createStateMetrics("states|timeout",_timeoutStates));
            metrics.addAll(createStateMetrics("states|error",_errorStates));
            metrics.addAll(createStateMetrics("states|stale",_staleStates));

            //TODO: Add State specific metrics


            return metrics ;
        }

        private Collection<? extends MetricValueContainer> createStateMetrics(String metricPrefix, HashMap<String, Long> states) {
            Collection<MetricValueContainer> metrics = new ArrayList<>();

            for (Map.Entry<String,Long> entry : states.entrySet()) {
                metrics.add(createSumMetric(metricPrefix+"|"+entry.getKey(),entry.getValue()));
            }

            return metrics;
        }


        private double calcAverageValue(ArrayList<Long> values) {
            long l = 0;
            for (Long v: values) {
                l+=v;
            }
            return l/values.size();
        }

        private MetricValueContainer createSumMetric(String metricName, long size) {
            return new MetricValueContainer(createMetricPath(metricName),size
                    ,MetricValueContainer.SUM,MetricValueContainer.SUM,MetricValueContainer.COLLECTIVE);
        }

        private MetricValueContainer createAvgMetric(String metricName, long value) {
            return new MetricValueContainer(createMetricPath(metricName), value
                    , MetricValueContainer.AVERAGE, MetricValueContainer.AVERAGE, MetricValueContainer.INDIVIDUAL);
        }


        private String createMetricPath(String metricName) {
                return _appName+MetricValueContainer.SEPERATOR+_name+MetricValueContainer.SEPERATOR+metricName;
            }

        public void reportErrorState(String state) {
          reportState(state, _errorStates);
        }

        public void reportTimeoutState(String state) {
            reportState(state, _timeoutStates);
        }

        public void reportStaleState(String state) {
            reportState(state,_staleStates);
        }


        private void reportState(String state, HashMap<String, Long> states) {
            Long value = null;
            if (states.containsKey(state)) {
                value = states.get(state);
            }
            if (value == null) value = new Long(0);

            value = value+1;

            states.put(state,value);
        }


    }

}
