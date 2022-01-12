package com.happymoney.cookiecutterservice.service;

import lombok.Value;
import java.time.Instant;
import java.util.UUID;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.happymoney.cookiecutterservice.domain.Example;
import com.happymoney.cookiecutterservice.entity.ExampleMapper;
import com.happymoney.cookiecutterservice.repository.ExampleRepository;
import com.happymoney.cookiecutterservice.schema.ExampleEvent;
import com.happymoney.cookiecutterservice.producer.ExampleEventProducer;

@Service
@Value
public class ExampleService{

    WebClient webClient;

    ExampleRepository exampleRepository;
    ExampleMapper exampleMapper;
    ExampleEventProducer eventProducer;

    public Optional<Example> findExample(String id){
        return exampleRepository.findById(id).map(exampleMapper::toDomain);
    }

    public void saveExample(Example example) {
        exampleRepository.save(exampleMapper.toDynamo(example));

        eventProducer.sendEvent(ExampleEvent.newBuilder()
                                 .setEventId(UUID.randomUUID().toString())
                                 .setEventMethod("SAVE")
                                 .setData(exampleMapper.toEvent(example))
                                 .setErrors(null)
                                 .setMeta(null)
                                 .setTimestamp(Instant.now().toEpochMilli())
                                 .build());
    }

    public Example getExample(String id){
        Example body = webClient.get()
            .uri("http://localhost:8080/api/my-resource/{id}", id) // Replace with the upstream Service URL
            .retrieve()
            .bodyToMono(Example.class)
            .block();
        return body;
    }

}
