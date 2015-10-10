package com.carmanconsulting.sandbox.camel.brokerwars;

import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartBroker {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger(StartBroker.class);

//----------------------------------------------------------------------------------------------------------------------
// main() method
//----------------------------------------------------------------------------------------------------------------------

    public static void startBroker(int port, int... nobPorts) throws Exception {
        BrokerService brokerService = new BrokerService();
        brokerService.addConnector("nio://localhost:" + port);
        brokerService.setPersistent(true);
        brokerService.setDataDirectory("target/activemq-data/broker_" + port + "/");
        brokerService.setUseJmx(false);
        for (int nobPort : nobPorts) {
            brokerService.addNetworkConnector("static://tcp://localhost:" + nobPort);
        }
        brokerService.start();
        LOGGER.info("Broker on port {} started, hit any key to stop.", port);
        System.in.read();
        LOGGER.info("Shutting down broker on port {}...", port);
    }
}
