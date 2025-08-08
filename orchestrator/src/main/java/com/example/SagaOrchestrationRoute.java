
package com.example;

import com.example.model.OrchestrationRequest;
import com.example.model.OrchestrationResponse;
import com.example.model.TagRequest;
import com.example.processor.StatusCodeProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.saga.InMemorySagaService;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SagaOrchestrationRoute extends RouteBuilder {

    @Value("${service.a.url}")
    private String serviceAUrl;

    @Value("${service.b.url}")
    private String serviceBUrl;

    @Autowired
    StatusCodeProcessor statusCodeProcessor;

    @Override
    public void configure() throws Exception {
        getContext().addService(new InMemorySagaService(), true);

        // Let Camel throw HttpOperationFailedException to trigger saga rollback
/*        onException(HttpOperationFailedException.class)
                .handled(false)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500));*/

        errorHandler(defaultErrorHandler()
            .maximumRedeliveries(1)
            .redeliveryDelay(2000)
            .retryAttemptedLogLevel(LoggingLevel.WARN));

        onException(Exception.class)
            .handled(false)
            .log("Saga failed. Exception: ${exception.message}")
            .setBody(simple("Saga failed. Compensation triggered."))
            //.process(statusCodeProcessor);
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500));

        from("direct:startSaga")
            .saga()
                .propagation(SagaPropagation.REQUIRED)
                .compensation("direct:compensateA")
            .routeId("saga-orchestration-route")
            .log("Starting Saga with OrchestrationRequest")
            .setProperty("orchestrationRequest", body())

            .process(exchange -> {
                OrchestrationRequest req = exchange.getProperty("orchestrationRequest", OrchestrationRequest.class);
                TagRequest tag = new TagRequest();
                tag.setTag(req.getServiceATag());
                exchange.getIn().setBody(tag);
            })
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .toD(serviceAUrl + "?bridgeEndpoint=true")
            .log("Service A response: ${body}")
            .process(statusCodeProcessor)
            .setProperty("responseA", simple("${body}"))

            .to("direct:invokeB")

            .bean("responseAggregator", "aggregate(${exchangeProperty.responseA}, ${exchangeProperty.responseB})")
            .log("Aggregated response: ${body}");

            //.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200));

        from("direct:invokeB")
            .saga()
                .propagation(SagaPropagation.REQUIRED)
                .compensation("direct:compensateB")
            .routeId("invoke-service-b")
            .process(exchange -> {
                OrchestrationRequest req = exchange.getProperty("orchestrationRequest", OrchestrationRequest.class);
                TagRequest tag = new TagRequest();
                tag.setTag(req.getServiceBTag());
                exchange.getIn().setBody(tag);
            })
            .marshal().json()
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .toD(serviceBUrl + "?bridgeEndpoint=true")
            .log("Service B response: ${body}")
            .process(statusCodeProcessor)
            .setProperty("responseB", simple("${body}"));

        from("direct:compensateA")
            .log("Compensating A for: ${body}")
            .process(exchange -> {
                TagRequest tag = new TagRequest();
                tag.setTag("compensateA1");
                exchange.getIn().setBody(tag);
            })
            .marshal().json()
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .toD("http://localhost:8081/compensateA?bridgeEndpoint=true")
            .process(statusCodeProcessor);

        from("direct:compensateB")
            .log("Compensating B for: ${body}")
            .process(exchange -> {
                TagRequest tag = new TagRequest();
                tag.setTag("compensateB1");
                exchange.getIn().setBody(tag);
            })
            .marshal().json()
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .toD("http://localhost:8082/compensateB?bridgeEndpoint=true");
    }
}
