package com.carmanconsulting.sandbox.camel.brokerwars;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

public class Replier {
//----------------------------------------------------------------------------------------------------------------------
// main() method
//----------------------------------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        CamelContext camelContext = CamelContextFactory.createCamelContext();
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jms:queue:requests")
                        .to("log://replier?showAll=true&multiline=true")
                        .setBody(constant("pong"));
            }
        });
        camelContext.start();
    }
}
