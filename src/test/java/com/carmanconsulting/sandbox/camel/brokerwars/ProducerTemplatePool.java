package com.carmanconsulting.sandbox.camel.brokerwars;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PoolUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerTemplatePool {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerTemplatePool.class);
    private final ObjectPool<ProducerTemplate> pool;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public ProducerTemplatePool(CamelContext camelContext) {
        this(camelContext, new GenericObjectPoolConfig());
    }

    public ProducerTemplatePool(CamelContext camelContext, GenericObjectPoolConfig poolConfig) {
        this.pool = PoolUtils.erodingPool(new GenericObjectPool<>(new ProducerTemplateFactory(camelContext), poolConfig));
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public <T> T createWithTemplate(Function<ProducerTemplate, T> function) {
        ProducerTemplate template = borrowTemplate();
        try {
            return function.apply(template);
        } finally {
            returnTemplate(template);
        }
    }

    private ProducerTemplate borrowTemplate() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            LOGGER.error("Unable to borrow ProducerTemplate from pool.", e);
            throw new RuntimeException("Unable to borrow ProducerTemplate from pool.", e);
        }
    }

    private void returnTemplate(ProducerTemplate template) {
        try {
            pool.returnObject(template);
        } catch (Exception e) {
            LOGGER.error("Unable to return ProducerTemplate to pool.", e);
        }
    }

    public void doWithTemplate(Consumer<ProducerTemplate> consumer) {
        createWithTemplate(template -> {
            consumer.accept(template);
            return null;
        });
    }
}
