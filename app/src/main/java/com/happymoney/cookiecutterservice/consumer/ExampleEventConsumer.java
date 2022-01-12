package com.happymoney.cookiecutterservice.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExampleEventConsumer {
    /**
    * @param message the message
    */
    @KafkaListener(topics = "${spring.kafka.topics.example-event-consumer}", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(final ConsumerRecord<?, Object> message) {
        //Handle here
    }
    /**
    * @param message the message
    */
    @DltHandler
    public void dlt(final ConsumerRecord<?, Object> message) {
        //handle dead letter here
    }

}