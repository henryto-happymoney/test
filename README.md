# Cookiecutter Service

## Requirements

1. Docker `brew install --cask docker`
2. Docker logged in to the Happy Money docker registry (requires AWS Configuration from Okta before running) `aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 730502903637.dkr.ecr.us-east-1.amazonaws.com`
3. A Kafka host mapping `echo "127.0.0.1 kafka-local" | sudo tee -a /etc/hosts`
4. A version of Java installed that is 11 or greater.

## Quick Start

1. Make sure you have Docker running on your computer.
2. Run `./gradlew check`. Verify all tests pass. If any do not pass, double-check the requirements.
3. After running `./gradlew check`, the server will be running in docker.
4. To shut it down and its related services, run `./gradlew composeDown`. Alternately, you can also just quit your Docker VM.
5. To turn it on again without running all tests, run `./gradlew composeUp`.
6. When the service is running, it is available at http://localhost:8080.
7. Swagger is available at http://localhost:8080/swagger-ui
8. To see the logs of the running service, you can use the Docker dashboard. Alternately, you can run `./gradlew composeLogs` to output the logs of all containers into the app/build/containers-logs directory.

## Template Features

As we add new features to the template, please note them here so consumers of the template can discover them.

### git

The template automatically initializes the project directory with git, and commits its initial state. This makes it easy to see what was originally generated, and keep that as part of the history of the repository.

### Gradle

The project automatically is configured with gradle, and a gradle wrapper ("gradlew"). [What's a gradle wrapper? Here's the documentation.](https://docs.gradle.org/current/userguide/gradle_wrapper.html)

Gradle provides a unified structure for executing project commands. To see what commands are currently configured with this or any gradle project, you can always run `./gradlew tasks --all` and it will list for you all available tasks.

Most commonly, `./gradlew assemble` will assemble any artifacts for the given project, and `./gradlew check` will run all validations required for the project.

As more features are added to the template project, it is likely these will each have associated tasks.

### Docker, Docker Compose

In order to allow for realistic testing, the template project utilizes Docker and Docker Compose to manage running services.

The `:app:dockerBuildImage` task will generate a Docker image for running the service in a container. This image is designed for fast-building, and is used for manual and automated testing.

The `:app:composeUp` task will start up all the local services required for the template project to run successfully, including the project service. This is automatically triggered when running the `:app:integrationTest` task, so those tests can always expect those services to be available.

Similarly, the `:app:composeLogs` task will download all the logs of the containers into the directory `app/build/containers-logs`, which can be useful when trying to understand problems with the services. This is automatically invoked after `:app:integrationTest`.

By default, containers will be left running after `:app:composeUp`, to better allow speedy development and enable local testing using those containers.

Use the task `:app:composeDown` in order to stop and remove the containers when needed.

### JUnit, AssertJ

For testing purposes, the template includes [JUnit Jupiter (aka JUnit5)](https://junit.org/junit5/docs/current/user-guide/) as its core test runner. Also included is the [AssertJ core library](https://assertj.github.io/doc/).

AssertJ is a slick alternative to standard JUnit assertions, that allow extensible fluency.

For example:

    assertThat(result).isEqualTo(expected);

    Optional<Thing> optionalResult = ...
    assertThat(optionalResult).hasValue(expectedResult);
    
    assertThat(optionalResult).isEmpty();

    assertThat(listResult).contains(expectedEntity);

These are all assertions that include matchers that are context-appropriate based on the type of the value provided to `assertThat`.

For information about how to extend AssertJ and add project-specific assertions, see [here](https://assertj.github.io/doc/#assertj-core-extensions).

### Citrus

The template uses Citrus as a http client and integration test assertion framework. All visible features that are provided by the template project are tested using examples written with Citrus. These examples can be found in `app/src/integrationTest/java/com/happymoney/cookiecutterservice/endpoint`.

### Spring Boot

The template automatically includes Spring Boot configuration.

In order to see the application run, you may run `./gradlew composeUp`, which will run the service in the development docker container, and will be available on port 8080.

### Swagger

The template includes a basic configuration of [Swagger](https://swagger.io/). While the service is running, this can be viewed at http://localhost:8080/swagger-ui/

### Dynamo DB Integration

The template includes an example of persistence using Spring Data and DynamoDB, using [this module](https://github.com/derjust/spring-data-dynamodb). An example persistence test is included in the class `ExampleDynamoPersisterTest`. This test demonstrates successful integration with the provided dynamodb instance.

DynamoDB is provided via being configured in the docker-compose.yml in the app directory.

### Spring Controller

The template includes GET and POST implementations based on an 'Example' entity. This demonstrates how Citrus can be used to test REST endpoints, and how the Spring integration works with the Dynamo integration.

### Kafka

The template includes an example of an endpoint posting a domain event to a Kafka topic. This is trigger by the POST endpoint. Also included is a test that demonstrates it works-as-intended.

Kafka is provided via being configured in the docker-compose.yml in the app directory.

Note: Remember to add `127.0.0.1 kafka-local` to your /etc/hosts file in order for tests to work-as-intended with kafka.
