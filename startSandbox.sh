#!/bin/bash
JAVA_OPTIONS='-Dappdynamics.agent.applicationName=machineTest2
-Dappdynamics.controller.hostName=smarxdocker.ddns.net
-Dappdynamics.controller.port=8090
-Dappdynamics.agent.tierName=test
-Dappdynamics.agent.nodeName=test
-Dappdynamics.agent.uniqueHostId=TESTER123
-Dappdynamics.agent.accountAccessKey=ca6c0cd3-a134-4e18-83d0-4f4d7c7e1b29
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005'

java $JAVA_OPTIONS -jar ./build/sandbox/agent/machineagent.jar
