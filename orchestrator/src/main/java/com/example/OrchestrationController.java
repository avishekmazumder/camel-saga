
package com.example;

import com.example.model.OrchestrationRequest;
import com.example.model.OrchestrationResponse;
import com.example.publisher.EventPublisher;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.apache.camel.model.dataformat.JsonLibrary.Gson;

@RestController
@RequestMapping("/orchestrate")
public class OrchestrationController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    EventPublisher publisher;

    @PostMapping
    public ResponseEntity<Object> orchestrate(@RequestBody OrchestrationRequest request) throws PulsarClientException {
/*        Exchange exchange = producerTemplate.request("direct:startSaga", ex -> ex.getIn().setBody(request));

        Integer statusCode = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        if (statusCode == null) {
            statusCode = 500;
        }
        Object body = exchange.getMessage().getBody();
        if (body instanceof OrchestrationResponse) {
            OrchestrationResponse response = exchange.getMessage().getBody(OrchestrationResponse.class);
            return ResponseEntity.status(statusCode).body(response);
        } else {
            return ResponseEntity.status(statusCode).body(body);  // plain error string
        }*/

        Exchange exchange = producerTemplate.request("direct:startSaga", ex -> ex.getIn().setBody(request));

        int status = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class) != null
                ? exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class)
                : (exchange.isFailed() ? 500 : 200);

        Object body = exchange.getMessage().getBody();

        publisher.publishPlainMessage("hello-world");

        return ResponseEntity.status(status).body(body);
    }
}
