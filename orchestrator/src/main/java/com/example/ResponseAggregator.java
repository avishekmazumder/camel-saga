
package com.example;

import com.example.model.OrchestrationResponse;
import org.springframework.stereotype.Component;

@Component("responseAggregator")
public class ResponseAggregator {
    public OrchestrationResponse aggregate(String a, String b) {
        return new OrchestrationResponse(a, b);
    }
}
