package com.carmanconsulting.sandbox.camel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.BeanInvocation;
import org.apache.camel.component.bean.ProxyHelper;
import org.junit.Test;

public class JmsRemotingTest extends JmsTestCase {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void testWithCustomSerialization() throws Exception {
        final Endpoint endpoint = context.getEndpoint("direct:service");
        final MyService proxy = ProxyHelper.createProxy(endpoint, MyService.class);
        assertEquals("foo - hello", proxy.foo(new Request("hello")));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:service").process(new GsonSerializer()).to("jms:queue:service");
                from("jms:queue:service").to("log:requests?level=INFO&showAll=true&multiline=true").process(new GsonDeserializer()).bean(EchoService.class);
            }
        };
    }

    private static class GsonSerializer implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            final BeanInvocation invocation = exchange.getIn().getBody(BeanInvocation.class);
            for(int i = invocation.getArgs().length - 1; i >= 0; --i) {
                invocation.getArgs()[i] = GSON.toJson(invocation.getArgs()[i]);
            }
        }
    }

    private static class GsonDeserializer implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            final BeanInvocation invocation = exchange.getIn().getBody(BeanInvocation.class);
            for(int i = invocation.getArgs().length - 1; i >= 0; --i) {
                invocation.getArgs()[i] = GSON.fromJson(String.valueOf(invocation.getArgs()[i]), invocation.getMethod().getParameterTypes()[i]);
            }
        }
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

    public static class EchoService implements MyService {
        @Override
        public String foo(Request request) {
            return "foo - " + request;
        }

        @Override
        public String bar(Request request) {
            return "bar - " + request;
        }
    }

    public static interface MyService {

        public String foo(Request request);

        public String bar(Request request);
    }
}
