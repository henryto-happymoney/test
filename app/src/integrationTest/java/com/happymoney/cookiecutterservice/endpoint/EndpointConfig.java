package com.happymoney.cookiecutterservice.endpoint;

import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.http.client.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EndpointConfig {

    @Bean(name = "appClient")
    public HttpClient appClient() {
        return CitrusEndpoints
                .http()
                .client()
                .requestUrl("http://localhost:8080")
                .contentType("application/json")
                .build();
    }

}
