package com.happymoney.cookiecutterservice.entity.example;

import com.happymoney.cookiecutterservice.entity.dynamo.ExampleDynamo;
import com.happymoney.cookiecutterservice.repository.ExampleRepository;
import com.happymoney.cookiecutterservice.service.ExampleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles(profiles = {"local"})
public class ExampleRepositoryTest {

    private static final Random random = new Random();

    @Autowired
    ExampleRepository repository;

    @Autowired
    ExampleService service;


    @Test
    public void givenExampleIsSavedItCanBeLoaded() {

        ExampleDynamo exampleDynamo = new ExampleDynamo(
                    UUID.randomUUID().toString(),
                    String.format("test-content-%d", random.nextInt(10)),
                    random.nextLong()
        );
        repository.save(exampleDynamo);

        assertThat(service.findExample(exampleDynamo.getId()).get().getId().toString())
                .isEqualTo(exampleDynamo.getId());

    }
}
