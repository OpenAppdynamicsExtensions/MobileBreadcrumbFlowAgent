package com.appdynamics.ace.custom.agent.mobileworkflowagent.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stefan.marx on 13.02.17.
 */
@JsonPropertyOrder({ "loginConfig", "instances" })

public class WorkflowConfig {

     private LoginConfig _loginConfig;

    private List<ApplicationInstance> _apps = new ArrayList<ApplicationInstance>();




    public List<ApplicationInstance> getApps() {
        return _apps;
    }

    public void setApps(List<ApplicationInstance> apps) {
        _apps = apps;
    }

    public LoginConfig getLoginConfig() {
        return _loginConfig;
    }

    public void setLoginConfig(LoginConfig loginConfig) {
        _loginConfig = loginConfig;
    }


    public File writeToFile(String s) throws IOException {
        File f = new File(s);
        YAMLFactory yf = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(yf);

        SequenceWriter sw = mapper.writerWithDefaultPrettyPrinter().writeValues(f);
        sw.write(this);

        return f;
    }

    public static WorkflowConfig readFromFile(String s) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return  mapper.readValue(new File(s), WorkflowConfig.class);
    }
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }


    public void addApplicationInstance(ApplicationInstance app) {
        _apps.add(app);
    }
}
