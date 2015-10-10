package com.carmanconsulting.sandbox.camel.brokerwars;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Requestor {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger(Requestor.class);

//----------------------------------------------------------------------------------------------------------------------
// main() method
//----------------------------------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        CamelContext camelContext = CamelContextFactory.createCamelContext();
        camelContext.start();
        ProducerTemplatePool pool = new ProducerTemplatePool(camelContext);

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
        executorService.scheduleAtFixedRate(() -> {
            pool.doWithTemplate(Requestor::ping);
        }, 1, 5, TimeUnit.SECONDS);

    }

    private static void ping(ProducerTemplate template) {
        LOGGER.info(template.requestBody("jms:queue:requests", "ping", String.class));
    }
}
