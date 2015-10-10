package com.carmanconsulting.sandbox.camel.brokerwars;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.impl.DefaultCamelContext;

public class CamelContextFactory {
//----------------------------------------------------------------------------------------------------------------------
// Static Methods
//----------------------------------------------------------------------------------------------------------------------

    public static CamelContext createCamelContext() {
        CamelContext camelContext = new DefaultCamelContext();
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("failover:(tcp://localhost:12345,tcp://localhost:12346)?randomize=true&jms.prefetchPolicy.all=1");
        PooledConnectionFactory pooled = new PooledConnectionFactory();
        pooled.setConnectionFactory(factory);
        pooled.setMaxConnections(20);
        pooled.start();

        final JmsConfiguration configuration = new JmsConfiguration(factory);
        configuration.setListenerConnectionFactory(factory);
        configuration.setTemplateConnectionFactory(pooled);

        camelContext.addComponent("jms", new JmsComponent(configuration));
        return camelContext;
    }
}
