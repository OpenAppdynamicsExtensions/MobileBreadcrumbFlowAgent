package com.appdynamics.ace.custom.agent.mobileworkflowagent.calc;

import java.util.Date;

/**
 * Created by stefan.marx on 25.04.16.
 */
public class MetricValueContainer {

        private String _path;
        private long _value ;
        private String _aggregation;
        private String _timeRollup  ;
        private String _cluster      ;
        private Date _evalTime = new Date();

        public static final String AVERAGE = "AVERAGE";
        public static final String SUM = "SUM";
        public static final String OBSERVATION = "OBSERVATION";
        public static final String CURRENT = "CURRENT";
        public static final String INDIVIDUAL = "INDIVIDUAL";
    public static final String COLLECTIVE = "COLLECTIVE";
    public static final String SEPERATOR = "|";


    public String getPath() {
            return _path ;
        }

        public long getValue() {
            return _value ;
        }

        public String getAggregation() {
            return _aggregation;
        }

        public String getTimeRollup() {
            return _timeRollup;
        }

        public String getCluster() {
            return _cluster    ;
        }

        Date getEvalTime() {
            return _evalTime    ;
        }


        /** Report a Metric to Agent or CLI,
         *
         * @param path
         * @param value
         * @param aggregation multi execution (node to tier aggregation) [ AVERAGE, SUM, OBSERVATION ]
         * @param timeRollup Time Rollout (1min -> 10 min -> 60 min) [AVERAGE, SUM, CURRENT  ]
         * @param cluster  Cluster Rollup  (node to tier aggregation) [INDIVIDUAL, COLLECTIVE]
         */
        public MetricValueContainer(String path, long value,
                                    String aggregation, String timeRollup, String cluster) {
            this._cluster = cluster;
            this._timeRollup = timeRollup;
            this._aggregation = aggregation;
            this._value = value;
            this._path = path;
        }


        public MetricValueContainer(String path, long value) {
            this(path,value,AVERAGE,AVERAGE,INDIVIDUAL);
        }

        public String toString () {
            return String.format("%s : (%s) -> %d [%s,%s,%s]",_evalTime,_path,_value,_aggregation,_timeRollup,_cluster);

        }

        public String getValueString () {
            return String.format(" (%s) \t-> %d",_path,_value);
        }

    }
