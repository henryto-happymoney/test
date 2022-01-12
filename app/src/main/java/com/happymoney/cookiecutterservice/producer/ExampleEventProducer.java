package com.happymoney.cookiecutterservice.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.happymoney.cookiecutterservice.schema.ExampleEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@lombok.Value
public class ExampleEventProducer {

    NewTopic kafkaTopic;
    KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEvent(ExampleEvent event) {
        kafkaTemplate.send(kafkaTopic.name(), event);
    }

}
