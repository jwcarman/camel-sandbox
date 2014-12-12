package com.carmanconsulting.sandbox.camel;

import com.carmanconsulting.sandbox.camel.jms.LoggingConnectionFactory;
import com.carmanconsulting.sandbox.camel.jms.LoggingPooledConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;

import javax.jms.ConnectionFactory;

public abstract class JmsTestCase extends CamelTestCase
{
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private ConnectionFactory connectionFactory;

//----------------------------------------------------------------------------------------------------------------------
// Getter/Setter Methods
//----------------------------------------------------------------------------------------------------------------------

    protected final ConnectionFactory getConnectionFactory()
    {
        if(connectionFactory == null)
        {
            connectionFactory = createConnectionFactory();
        }
        return connectionFactory;
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    protected ConnectionFactory createConnectionFactory()
    {
        return new LoggingConnectionFactory(getBrokerUrl());
    }

    protected JmsConfiguration createJmsConfiguration() {
        final ConnectionFactory factory = createConnectionFactory();
        final LoggingPooledConnectionFactory pooled = new LoggingPooledConnectionFactory();
        pooled.setConnectionFactory(factory);

        final JmsConfiguration configuration = new JmsConfiguration(pooled);

        configuration.setListenerConnectionFactory(factory);


        return configuration;
    }

    protected String getBrokerUrl() {
        return String.format("vm://%s?broker.persistent=false", getClass().getSimpleName());
    }

    @Override
    protected void initializeCamelContext(CamelContext context)
    {
        JmsComponent jms = new JmsComponent(createJmsConfiguration());
        context.addComponent("jms", jms);
    }
}
