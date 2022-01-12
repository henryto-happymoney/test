package com.happymoney.cookiecutterservice.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverterFactory;
import io.micrometer.core.instrument.util.StringUtils;
import org.socialsignin.spring.data.dynamodb.config.EnableDynamoDBAuditing;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableDynamoDBAuditing
@EnableDynamoDBRepositories("com.happymoney.cookiecutterservice.repository")
public class DynamoAwsConfig {

    @Value("${amazon.dynamodb.region}")
    private String region;

    @Value("${amazon.dynamodb.endpoint}")
    private String amazonDynamoDBEndpoint;

    @Value("${amazon.dynamodb.accesskey}")
    private String amazonAWSAccessKey;

    @Value("${amazon.dynamodb.secretkey}")
    private String amazonAWSSecretKey;

    @Value("${amazon.dynamodb.tablename-prefix}")
    private String tableNamePrefix;

    @Value("${amazon.dynamodb.tablename-suffix}")
    private String tableNameSuffix;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        if(!StringUtils.isBlank(amazonAWSAccessKey) && !StringUtils.isBlank(amazonAWSSecretKey) ) {
            BasicAWSCredentials credentials = new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
            return AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(amazonDynamoDBEndpoint, region))
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .build();
        }

        return AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(amazonDynamoDBEndpoint, region))
                .build();

    }

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        DynamoDBMapperConfig.Builder builder = new DynamoDBMapperConfig.Builder();
        builder.withTableNameResolver(tableNameResolver()).withTypeConverterFactory(DynamoDBTypeConverterFactory.standard());
        return builder.build();
    }

    private DynamoDBMapperConfig.TableNameResolver tableNameResolver() {
        return (clazz, config) -> {
            final DynamoDBTable dynamoDBTable = clazz.getDeclaredAnnotation(DynamoDBTable.class);
            if (dynamoDBTable == null) {
                throw new DynamoDBMappingException(clazz + " not annotated with @DynamoDBTable");
            }
            return tableNamePrefix + dynamoDBTable.tableName() + tableNameSuffix;
        };
    }
}
