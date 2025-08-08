
package com.example;

import org.springframework.stereotype.Component;

@Component("aggregator")
public class AggregationBean {
    public String aggregate(String responseA, String responseB) {
        return "Aggregated Response -> [" + responseA + "] & [" + responseB + "]";
    }
}
