package com.appdynamics.ace.custom.agent.mobileworkflowagent.dto;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;

/**
 * Created by stefan.marx on 22.02.17.
 */
public class ApplicationInstance {
    private String _appKey;
    private String _appName;



    public String getAppName() {
        return _appName;
    }

    public void setAppName(String appName) {
        _appName = appName;
    }

    public String getAppKey() {

        return _appKey;
    }

    public void setAppKey(String appKey) {
        _appKey = appKey;
    }

    private ArrayList<WorkflowInstance> _workflowInstances = new ArrayList<WorkflowInstance> ();

    public ArrayList<WorkflowInstance> getWorkflowInstances() {
        return _workflowInstances;
    }

    public void setWorkflowInstances(ArrayList<WorkflowInstance> workflowInstances) {
        _workflowInstances = workflowInstances;
    }

    public void addWorkflowInstance(WorkflowInstance flow) {
        _workflowInstances.add(flow);
    }


    public ApplicationInstance(String appKey, String appName) {
        _appKey = appKey;
        _appName = appName;
    }
    public ApplicationInstance() {}

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }


}
