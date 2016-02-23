package com.carmanconsulting.sandbox.camel;

import com.carmanconsulting.sandbox.camel.jms.LoggingConnectionFactory;
import com.carmanconsulting.sandbox.camel.jms.LoggingPooledConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupProducer {
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
        camelContext.start();

        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

        for (int i = 0; i < 5; ++i) {
            new Thread(() -> {
                while (true) {
                    final String groupName = Thread.currentThread().getName() + "-" + (System.currentTimeMillis() / 30000);
                    producerTemplate.sendBodyAndHeader("jms:queue:grouped_messages", ExchangePattern.InOnly, groupName, "JMSXGroupID", groupName);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LOGGER.error("Unable to sleep!", e);
                    }
                }
            }, "GroupProducer-" + i).start();
        }


    }
}
