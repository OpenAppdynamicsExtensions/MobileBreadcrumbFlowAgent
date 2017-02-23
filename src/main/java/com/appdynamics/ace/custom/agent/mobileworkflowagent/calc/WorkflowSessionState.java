package com.appdynamics.ace.custom.agent.mobileworkflowagent.calc;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by stefan.marx on 22.02.17.
 */
public class WorkflowSessionState {
    private final String _session;
    private final Date _started;

    HashMap<String, WorkflowState> _workflowStates = new HashMap<String,WorkflowState> ();
    private Date _start;
    private Date _end;


    public WorkflowSessionState(String sessionguid) {
        _session = sessionguid;
        _started = new Date();
    }

    public long age() {
        return System.currentTimeMillis()-_started.getTime();
    }

    public WorkflowState getWorkflowState(String flowName) {
        if (!_workflowStates.containsKey(flowName)) {
            WorkflowState flow = new WorkflowState(flowName);
            _workflowStates.put(flowName,flow);
            return flow;
        } else return _workflowStates.get(flowName);
    }

    public void removeTempData() {
        _workflowStates.clear();
    }

    public void setStart(Date eventTimestamp) {
        _start = eventTimestamp;
    }

    public void setEnd(Date end) {
        _end = end;
    }

    public Date getEnd() {
        return _end;
    }
}
