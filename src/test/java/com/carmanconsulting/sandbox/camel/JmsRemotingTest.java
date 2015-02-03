package com.carmanconsulting.sandbox.camel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangeTimedOutException;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.ProxyHelper;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.Test;

import javax.xml.ws.Service;

public class JmsRemotingTest extends JmsTestCase {
//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                final RuntimeTypeAdapterFactory<Request> requestAdapterFactory = RuntimeTypeAdapterFactory.of(Request.class, "_type");
                requestAdapterFactory.registerSubtype(FooRequest.class);
                requestAdapterFactory.registerSubtype(BarRequest.class);

                final RuntimeTypeAdapterFactory<Response> responseAdapterFactory = RuntimeTypeAdapterFactory.of(Response.class, "_type");
                responseAdapterFactory.registerSubtype(FooResponse.class);
                responseAdapterFactory.registerSubtype(BarResponse.class);


                final Gson gson = new GsonBuilder().registerTypeAdapterFactory(requestAdapterFactory).registerTypeAdapterFactory(responseAdapterFactory).create();

                from("direct:service")
                        .setBody(simple("${body.args[0]}"))
                        .process(new GsonSerializer(gson, Request.class))
                        .to("jms:queue:service?requestTimeout=500")
                        .process(new GsonDeserializer(gson, Response.class));
                from("jms:queue:service")
                        .onException(Exception.class).setHeader("EXCEPTION_MESSAGE", exceptionMessage()).setBody(constant("")).end()
                        .process(new GsonDeserializer(gson, Request.class))
                        .doTry()
                        .bean(new EchoService())
                        .process(new GsonSerializer(gson, Response.class))
                        .doCatch(Exception.class)
                        .setHeader("EXCEPTION_MESSAGE", exceptionMessage()).setBody(constant(""))
                        .end()
                        .to("log:afterService?level=INFO&multiline=true&showAll=true");
            }
        };
    }

    @Override
    protected void doBindings(SimpleRegistry registry) {
        registry.put("service", new EchoService());
    }

    @Test
    public void testWithCustomSerialization() throws Exception {
        final Endpoint endpoint = context.getEndpoint("direct:service");
        final MyService proxy = ProxyHelper.createProxy(endpoint, MyService.class);
        assertEquals("foo - hello", proxy.foo(new FooRequest("hello")).getValue());
        assertEquals("bar - hello", proxy.bar(new BarRequest("hello")).getValue());
    }

    @Test
    public void testWithException() throws Exception {
        final Endpoint endpoint = context.getEndpoint("direct:service");
        final MyService proxy = ProxyHelper.createProxy(endpoint, MyService.class);
        try {
            proxy.foo(new FooRequest("bogus"));
            fail("Should throw exception!");
        }
        catch(ServiceException e) {
            assertEquals("Not gonna do it!", e.getMessage());
        }
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    public static class BarRequest extends Request {
        public BarRequest(String value) {
            super(value);
        }
    }

    public static class BarResponse extends Response {
        public BarResponse(String value) {
            super(value);
        }
    }

    public class EchoService implements MyService {
        @Override
        public FooResponse foo(FooRequest request) {
            logger.info("Processing foo request...");
            if ("bogus".equals(request.getValue())) {
                throw new IllegalArgumentException("Not gonna do it!");
            }
            return new FooResponse("foo - " + request);
        }

        @Override
        public BarResponse bar(BarRequest request) {
            logger.info("Processing bar request...");
            return new BarResponse("bar - " + request);
        }
    }

    public static class FooRequest extends Request {
        public FooRequest(String value) {
            super(value);
        }
    }

    public static class FooResponse extends Response {
        public FooResponse(String value) {
            super(value);
        }
    }

    private class GsonDeserializer implements Processor {
        private final Gson gson;
        private final Class<?> type;

        public GsonDeserializer(Gson gson, Class<?> type) {
            this.gson = gson;
            this.type = type;
        }

        @Override
        public void process(Exchange exchange) throws Exception {
            logger.info("Deserializing in body to {} object...", type.getCanonicalName());

            final String body = exchange.getIn().getBody(String.class);
            if ("".equals(body)) {
                throw new ServiceException(exchange.getIn().getHeader("EXCEPTION_MESSAGE").toString());
            }
            exchange.getIn().setBody(gson.fromJson(body, type));
        }
    }

    private class GsonSerializer implements Processor {
        private final Gson gson;
        private final Class<?> type;

        public GsonSerializer(Gson gson, Class<?> type) {
            this.gson = gson;
            this.type = type;
        }

        @Override
        public void process(Exchange exchange) throws Exception {
            logger.info("Serializing in body message...");
            exchange.getIn().setBody(gson.toJson(exchange.getIn().getBody(type)));
        }
    }

    public static interface MyService {
        public FooResponse foo(FooRequest request);

        public BarResponse bar(BarRequest request);
    }

    public static class Request {
        private final String value;

        public Request(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String toString() {
            return value;
        }
    }

    public static class Response {
        private final String value;

        private String errorMessage;

        public Response(String value) {
            this.value = value;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getValue() {
            return value;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static class ServiceException extends RuntimeException {
        public ServiceException(String message) {
            super(message);
        }
    }
}
