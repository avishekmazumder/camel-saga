
package com.example;

import com.example.model.TagRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class ServiceAApplication {

    Logger logger = LoggerFactory.getLogger(ServiceAApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ServiceAApplication.class, args);
    }

    @PostMapping("/processA")
    public String processA(@RequestBody TagRequest request) {
        logger.info("Processing A with request: " + request.toString());
        if (request.getTag().contains("failA")) {
            throw new RuntimeException("Simulated failure in Service A");
        }
        return "Processed A: " + request.getTag();
    }

    @PostMapping("/compensateA")
    public String compensateA(@RequestBody(required = false) TagRequest request) {
        logger.info("compensate for A called");
        logger.info(request.toString());
        return "Compensated A for: " + request.getTag();
    }
}
