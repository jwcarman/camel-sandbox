package com.carmanconsulting.sandbox.camel;

import com.carmanconsulting.sandbox.camel.jms.LoggingConnectionFactory;
import com.carmanconsulting.sandbox.camel.jms.LoggingPooledConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupConsumer.class);

    public static void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        ActiveMQConnectionFactory factory = new LoggingConnectionFactory("failover:(tcp://localhost:61616)?jms.prefetchPolicy.all=1");
        PooledConnectionFactory pooled = new LoggingPooledConnectionFactory();
        pooled.setConnectionFactory(factory);
        pooled.setMaxConnections(5);
        pooled.start();

        final JmsConfiguration configuration = new JmsConfiguration(factory);
        configuration.setListenerConnectionFactory(factory);
        configuration.setTemplateConnectionFactory(pooled);

        camelContext.addComponent("jms", new JmsComponent(configuration));
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jms:queue:grouped_messages").process(exchange -> {
                            LOGGER.info("Received message from group {}.", exchange.getIn().getBody(String.class));
                        }
                );
            }
        });
        camelContext.start();

        System.in.read();
        camelContext.stop();
        System.exit(0);
    }

}
