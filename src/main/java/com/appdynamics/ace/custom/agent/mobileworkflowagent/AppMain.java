package com.appdynamics.ace.custom.agent.mobileworkflowagent;

import com.appdynamics.ace.custom.agent.mobileworkflowagent.calc.MetricValueContainer;
import com.appdynamics.ace.custom.agent.mobileworkflowagent.calc.WorkflowCalculationEngine;
import com.appdynamics.ace.custom.agent.mobileworkflowagent.dto.WorkflowConfig;
import com.appdynamics.ace.util.cli.api.api.*;
import de.appdynamics.client.eventservice.adql.AnalyticsException;
import org.apache.commons.cli.Option;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by stefan.marx on 22.02.17.
 */
public class AppMain {


    public static void main(String[] args) {

        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.TRACE);
        console.activateOptions();


        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(console);
        Logger.getLogger("org.apache").setLevel(Level.INFO);

        CommandlineExecution cle = new CommandlineExecution("MobileWorkflowAgent");
        cle.setHelpVerboseEnabled(true);
        cle.addCommand(new TestCommand());

        System.exit(cle.execute(args));

    }

    private static class TestCommand extends AbstractCommand {
        static Logger _logger =  Logger.getLogger("AppMain");

        private ArrayList<Option> _opts;

        @Override
        protected List<Option> getCLIOptionsImpl() {
            _opts = new ArrayList<Option>();
            Option opt ;

            opt = new Option("cfg",true,"Configuration to load") ;
            opt.setRequired(true);
            _opts.add(opt);

            opt = new Option("start",true,"Start Time  ISO format [YYYY-MM-DD]T[hh:mm:ss]") ;
            opt.setRequired(true);
            _opts.add(opt);
            opt = new Option("end",true,"End Time  ISO format [YYYY-MM-DD]T[hh:mm:ss]") ;
            opt.setRequired(true);
            _opts.add(opt);

            opt = new Option("debug",false,"End Time  ISO format [YYYY-MM-DD]T[hh:mm:ss]") ;
            opt.setRequired(false);
            _opts.add(opt);

            opt = new Option("trace",false,"End Time  ISO format [YYYY-MM-DD]T[hh:mm:ss]") ;
            opt.setRequired(false);
            _opts.add(opt);
            return _opts;
        }

        @Override
        protected int executeImpl(OptionWrapper options) throws CommandException {
            Logger.getRootLogger().setLevel(Level.INFO);
            if (options.hasOption("debug")) Logger.getRootLogger().setLevel(Level.DEBUG);
            if (options.hasOption("trace")) Logger.getRootLogger().setLevel(Level.TRACE);


            _logger.info("STARTED");
            Date startDate = javax.xml.bind.DatatypeConverter.parseDateTime(options.getOptionValue("start")).getTime();
            Date endDate = javax.xml.bind.DatatypeConverter.parseDateTime(options.getOptionValue("end")).getTime();
            _logger.info("Testing Events from:"+startDate+"  till "+endDate);

            try {
                WorkflowConfig cfg = WorkflowConfig.readFromFile(options.getOptionValue("cfg"));
                _logger.info("Using CFG from :"+options.getOptionValue("cfg")+" \n"+cfg);

                WorkflowCalculationEngine engine = new WorkflowCalculationEngine(cfg, startDate, endDate);
                ArrayList<MetricValueContainer> metrics = engine.execute();
                dumpMetrics(metrics);
                _logger.debug("State:"+engine.getState());
            } catch (Throwable e) {
                _logger.error("ERROR while testing :"+e);
            }

            return 0;
        }

        private void dumpMetrics(ArrayList<MetricValueContainer> metrics) {
            System.out.println("METRICS Collected :");
            for (MetricValueContainer m : metrics) {
                System.out.println(m.toString());
            }
        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public String getDescription() {
            return "runs a local test of the workflow engine";
        }
    }
}
