package com.carmanconsulting.sandbox.camel.brokerwars;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ProducerTemplateFactory extends BasePooledObjectFactory<ProducerTemplate> {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final CamelContext camelContext;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public ProducerTemplateFactory(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

//----------------------------------------------------------------------------------------------------------------------
// PooledObjectFactory Implementation
//----------------------------------------------------------------------------------------------------------------------

    
    @Override
    public void destroyObject(PooledObject<ProducerTemplate> p) throws Exception {
        p.getObject().stop();
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Override
    public ProducerTemplate create() throws Exception {
        return camelContext.createProducerTemplate();
    }

    @Override
    public PooledObject<ProducerTemplate> wrap(ProducerTemplate producerTemplate) {
        return new DefaultPooledObject<>(producerTemplate);
    }
}
