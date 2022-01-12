package com.happymoney.cookiecutterservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import com.happymoney.cookiecutterservice.consumer.ExampleEventConsumer;


import java.util.HashMap;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.topics.example-event}")
    private String topic;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    /**
     *
     */
    @Value("${spring.kafka.properties.partition.count}")
    private int topicPartitionCount;
    /**
     *
     */
    @Value("${spring.kafka.properties.replication.factor}")
    private int topicReplicationFactor;
    /**
     *
     */
    @Value("${spring.kafka.properties.message.max.attempt}")
    private int maxAttempt;
    /**
     * retry naming pattern.
     */
    @Value("${spring.kafka.properties.retry.naming.pattern}")
    private String retryNamingPattern;
    /**
     * dlt naming pattern.
     */
    @Value("${spring.kafka.properties.dlt.naming.pattern}")
    private String dltNamingPattern;
    /**.
     * Delay Constant.
     */
    public static final long TOPIC_DELAY_MILLISECONDS = 1000;
    /**.
     * Max delay Constant.
     */
    public static final long MAX_DELAY_MILLISECONDS = 30000;
    /**.
     * Delay multiplier.
     */
    public static final long TOPIC_DELAY_MULTIPLIER = 3;

    @Bean
    public NewTopic kafkaTopic() {
        return TopicBuilder.name(topic)
                .partitions(topicPartitionCount)
                .replicas(topicReplicationFactor)
                .build();
    }

    /**
     * Retryable Topic config.
     * @param template message template
     * @return RetryTopicConfiguration
     */
    @Bean
    public RetryTopicConfiguration retry(final KafkaTemplate<String, Object> template) {
        return RetryTopicConfigurationBuilder
                .newInstance()
                .exponentialBackoff(TOPIC_DELAY_MILLISECONDS, TOPIC_DELAY_MULTIPLIER, MAX_DELAY_MILLISECONDS)
                .retryTopicSuffix(retryNamingPattern)
                .dltSuffix(dltNamingPattern)
                .dltHandlerMethod(ExampleEventConsumer.class, "dlt")
                .autoCreateTopics(true, topicPartitionCount, (short) topicReplicationFactor)
                .setTopicSuffixingStrategy(TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
                .maxAttempts(maxAttempt)
                .create(template);
    }

}
