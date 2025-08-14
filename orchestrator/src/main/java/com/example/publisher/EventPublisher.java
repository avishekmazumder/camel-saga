package com.example.publisher;

import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    Logger log = LoggerFactory.getLogger(EventPublisher.class);

    @Value("${spring.pulsar.producer.topic-name1}")
    private String topicName1;
    @Autowired
    private PulsarTemplate<Object> template;

    public void publishPlainMessage(String message) throws PulsarClientException {
        template.send(topicName1, message);
        log.info("EventPublisher::publishPlainMessage publish the event {}", message);
    }

}
