package com.carmanconsulting.sandbox.camel.jms;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import java.util.concurrent.atomic.AtomicInteger;

public class LoggingConnectionFactory extends ActiveMQConnectionFactory {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingConnectionFactory.class);
    private final MetricRegistry registry = new MetricRegistry();
    private final Meter connectionRate = registry.meter("connect");

    private AtomicInteger count = new AtomicInteger();

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public LoggingConnectionFactory(String brokerURL) {
        super(brokerURL);
    }

//----------------------------------------------------------------------------------------------------------------------
// ConnectionFactory Implementation
//----------------------------------------------------------------------------------------------------------------------

    @Override
    public Connection createConnection() throws JMSException {
        LOGGER.info("createConnection()");
        connectionRate.mark();
        final Connection connection = super.createConnection();
        LOGGER.info("Returning connection {}.", count.incrementAndGet());
        return connection;
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        LOGGER.info("createConnection({}, {})", userName, password);
        connectionRate.mark();
        final Connection connection = super.createConnection(userName, password);
        LOGGER.info("Returning connection {}.", count.incrementAndGet());
        return connection;
    }

//----------------------------------------------------------------------------------------------------------------------
// Getter/Setter Methods
//----------------------------------------------------------------------------------------------------------------------

    public MetricRegistry getRegistry() {
        return registry;
    }
}
