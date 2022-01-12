package com.happymoney.cookiecutterservice.endpoint.standard;

import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.junit.jupiter.CitrusSupport;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.consol.citrus.dsl.JsonPathSupport.jsonPath;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@SpringBootTest(webEnvironment = DEFINED_PORT)
@ActiveProfiles(profiles = {"local"})
@CitrusSupport
public class HealthCheckTest {

    @CitrusEndpoint
    private HttpClient appClient;

    @Test
    @CitrusTest
    public void verifyUp(@CitrusResource TestCaseRunner runner) {
        runner.$(http()
                         .client(appClient)
                         .send()
                         .get("/actuator/health"));

        runner.variable("time", "citrus:currentDate()");

        runner.$(http().client(appClient)
                         .receive()
                         .response(HttpStatus.OK)
                         .message()
                         .contentType(ContentType.APPLICATION_JSON.getMimeType())
                         .validate(jsonPath().expression("$.status", "UP")));
    }

}
