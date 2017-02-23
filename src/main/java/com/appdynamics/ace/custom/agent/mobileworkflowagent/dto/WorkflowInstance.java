package com.appdynamics.ace.custom.agent.mobileworkflowagent.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashSet;

/**
 * Created by stefan.marx on 13.02.17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "name","beaconReplacePattern","beaconReplace" })

public class WorkflowInstance {

    private String _beaconReplacePattern;

    public String getBeaconReplacePattern() {
        return _beaconReplacePattern;
    }

    public void setBeaconReplacePattern(String beaconReplacePattern) {
        _beaconReplacePattern = beaconReplacePattern;
    }

    public String getBeaconReplace() {
        return _beaconReplace;
    }

    public void setBeaconReplace(String beaconReplace) {
        _beaconReplace = beaconReplace;
    }

    private String _beaconReplace;

    public String getFlowName() {
        return _flowName;
    }


    private String _flowName = "UNKNOWN";

    private HashSet<String> _startStates = new HashSet<>();
    private HashSet<String> _validStates = new HashSet<>();
    private HashSet<String> _endStates = new HashSet<>();
    private HashSet<String> _errorStates = new HashSet<>();

    private long _timeoutMs = 60000l;
    private boolean _shouldLogTimeoutSessions = true;

    public boolean isShouldLogTimeoutSessions() {
        return _shouldLogTimeoutSessions;
    }

    public void setShouldLogTimeoutSessions(boolean shouldLogTimeoutSessions) {
        _shouldLogTimeoutSessions = shouldLogTimeoutSessions;
    }

    public long getTimeoutMs() {
        return _timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        _timeoutMs = timeoutMs;
    }

    public HashSet<String> getStartStates() {
        return _startStates;
    }

    public void setStartStates(HashSet<String> startStates) {
        _startStates = startStates;
    }

    public HashSet<String> getValidStates() {
        return _validStates;
    }

    public void setValidStates(HashSet<String> validStates) {
        _validStates = validStates;
    }

    public HashSet<String> getEndStates() {
        return _endStates;
    }

    public void setEndStates(HashSet<String> endStates) {
        _endStates = endStates;
    }

    public HashSet<String> getErrorStates() {
        return _errorStates;
    }

    public void setErrorStates(HashSet<String> errorStates) {
        _errorStates = errorStates;
    }

    public void setFlowName(String flowName) {
        _flowName = flowName;
    }

    public WorkflowInstance(String name) {
        _flowName = name;
    }

    public WorkflowInstance() {
    }

    public WorkflowInstance addStartState(String state) {
        _startStates.add(state);
        return this;
    }

    public WorkflowInstance addEndState(String state) {
        _endStates.add(state);
        return this;
    }

    public WorkflowInstance addErrorState(String state) {
        _errorStates.add(state);
        return this;
    }

    public WorkflowInstance addValidState(String state) {
        _validStates.add(state);
        return this;
    }

    boolean isStateValidationEnabled() {
        return _validStates.size()>0;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public void setReplaceRegexp(String pattern, String replace) {
        _beaconReplacePattern = pattern;
        _beaconReplace = replace;
    }

    public boolean isStartState(String bcName) {
        return _startStates.contains(bcName);
    }

    public boolean isValidState(String bcName) {
        return _validStates.contains(bcName);
    }

    public boolean isEndState(String bcName) {
        return _endStates.contains(bcName);
    }

    public boolean isErrorState(String bcName) {
        return _errorStates.contains(bcName);
    }
}
