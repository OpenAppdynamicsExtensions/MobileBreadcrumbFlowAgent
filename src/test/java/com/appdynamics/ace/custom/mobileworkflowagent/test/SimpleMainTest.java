package com.appdynamics.ace.custom.mobileworkflowagent.test;

import com.appdynamics.ace.custom.agent.mobileworkflowagent.dto.ApplicationInstance;
import com.appdynamics.ace.custom.agent.mobileworkflowagent.dto.WorkflowConfig;
import com.appdynamics.ace.custom.agent.mobileworkflowagent.dto.WorkflowInstance;
import com.appdynamics.ace.custom.agent.mobileworkflowagent.dto.LoginConfig;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.IOException;

/**
 * Created by stefan.marx on 13.02.17.
 */
public class SimpleMainTest {

    public static void main(String[] args) {
        WorkflowConfig cfg = new WorkflowConfig();

        LoginConfig login = new LoginConfig();
        login.setUser("");
        login.setPasswd("");

        cfg.setLoginConfig(login);

        WorkflowInstance flow = new WorkflowInstance("Test");
        flow.addStartState("start")
            .addEndState("end")
            .addEndState("end2")
            .addErrorState("ERROR");

//        flow.setReplaceRegexp("(.*)_([^_]*)","$1");


        ApplicationInstance app =  new ApplicationInstance("AAA-DDD-EEE","NAME");
        app.addWorkflowInstance(flow);
        app.addWorkflowInstance(flow);


        cfg.addApplicationInstance(app );



        try {
            cfg.writeToFile("./src/main/conf/sample.yml");

           // WorkflowConfig cfg2 = WorkflowConfig.readFromFile("./src/main/conf/sample2.yml");

            System.out.println(ReflectionToStringBuilder.toString(cfg, ToStringStyle.MULTI_LINE_STYLE));
           // System.out.println(ReflectionToStringBuilder.toString(cfg2, ToStringStyle.MULTI_LINE_STYLE));

        } catch (IOException e) {
            e.printStackTrace();
        }




    }
}
