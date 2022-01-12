package com.happymoney.cookiecutterservice.endpoint;

import com.happymoney.cookiecutterservice.entity.ExampleMapperImpl;
import com.happymoney.cookiecutterservice.domain.Example;
import com.happymoney.cookiecutterservice.schema.ExampleEvent;

import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.junit.jupiter.CitrusSupport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.ContentType;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.assertj.core.util.Streams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import static com.consol.citrus.dsl.JsonSupport.json;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;
import static com.happymoney.cookiecutterservice.response.ResponseDTO.response;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@SpringBootTest(webEnvironment = DEFINED_PORT)
@ActiveProfiles(profiles = {"local"})
@CitrusSupport
public class ExampleTest {

    private static final String topic = "store.cookiecutter-service.example-event";

    @CitrusEndpoint
    private HttpClient appClient;
    private ExampleMapperImpl exampleMapper;
    private Random random;
    private ObjectMapper objectMapper;
    private KafkaConsumer<String, Object> consumer;

    @BeforeEach
    void setUp() {
        exampleMapper = new ExampleMapperImpl();
        random = new Random();
        objectMapper = new ObjectMapper();
        consumer = createConsumer();
        consumer.subscribe(List.of(topic));
    }

    @Test
    @CitrusTest
    public void givenCreatedExampleThenItCanBeRetrieved(@CitrusResource TestCaseRunner runner)
            throws JsonProcessingException {
        Example example = generateExample();
        runner.$(http()
                         .client(appClient)
                         .send()
                         .post("/example")
                         .message()
                         .body(toJsonString(example))
                );
        runner.$(http()
                         .client(appClient)
                         .receive()
                         .response(HttpStatus.OK)
                         .message()
                         .body(toJsonResponseString(example))
                         .contentType(ContentType.APPLICATION_JSON.getMimeType())
                         .validate(json().build())
                );

        runner.$(http()
                         .client(appClient)
                         .send()
                         .get(String.format("/example/%s", example.getId()))
                );
        runner.$(http()
                         .client(appClient)
                         .receive()
                         .response(HttpStatus.OK)
                         .message()
                         .body(toJsonResponseString(example))
                         .contentType(ContentType.APPLICATION_JSON.getMimeType())
                         .validate(json().build())
                );
    }

    private Example generateExample() {
        return new Example(
                UUID.randomUUID(),
                String.format("thing-%d", random.nextInt(20)),
                random.nextLong()
        );
    }

    @Test
    @CitrusTest
    public void givenExampleIsSecured(@CitrusResource TestCaseRunner runner)
            throws JsonProcessingException {
        Example example = generateExample();
        runner.$(http()
                        .client(appClient)
                        .send()
                        .post("/api/example")
                        .message()
                        .body(toJsonString(example))
            );
        runner.$(http()
                        .client(appClient)
                        .receive()
                        .response(HttpStatus.UNAUTHORIZED)
                        .message()
                        .contentType(ContentType.APPLICATION_JSON.getMimeType())
                        .validate(json().build())
            );
    }

    @Test
    @CitrusTest
    public void givenExampleIsCreatedThenAnEventIsPosted(@CitrusResource TestCaseRunner runner)
            throws JsonProcessingException {
        Example example = generateExample();
        runner.$(http()
                         .client(appClient)
                         .send()
                         .post("/example")
                         .message()
                         .body(toJsonString(example))
                );
        runner.$(http()
                         .client(appClient)
                         .receive()
                         .response(HttpStatus.OK)
                         .message()
                         .body(toJsonResponseString(example))
                         .contentType(ContentType.APPLICATION_JSON.getMimeType())
                         .validate(json().build())
                );

        final var record = getLastRecord();
        final var event = (ExampleEvent) record.value();

        assertThat(event.getData().getContent())
                .isEqualTo(example.getContent());
    }

    private ConsumerRecord<String, Object> getLastRecord() {
        return Streams.stream(KafkaTestUtils.getRecords(consumer, 10000)
                                      .records(topic)
                                      .iterator())
                .reduce((l, r) -> r)
                .orElse(null);
    }

    @Test
    @CitrusTest
    public void givenRandomIdThenNoExampleIsRetrieved(@CitrusResource TestCaseRunner runner) {
        UUID id = UUID.randomUUID();

        runner.$(http()
                         .client(appClient)
                         .send()
                         .get(String.format("/example/%s", id))
                );
        runner.$(http()
                         .client(appClient)
                         .receive()
                         .response(HttpStatus.NOT_FOUND)
                         .message()
                );
    }

    private String toJsonString(final Example example) throws JsonProcessingException {
        return objectMapper.writeValueAsString(exampleMapper.toJson(example));
    }

    private String toJsonResponseString(final Example example) throws JsonProcessingException {
        return objectMapper.writeValueAsString(response(exampleMapper.toJson(example)));
    }

    private KafkaConsumer<String, Object> createConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "integration-" + UUID.randomUUID());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        properties.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:9091");
        properties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        return new KafkaConsumer<>(properties);
    }
}
