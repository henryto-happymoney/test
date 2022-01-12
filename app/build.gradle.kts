import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage

plugins {
    java
    id("org.springframework.boot") version ("2.5.3")
    id("io.spring.dependency-management") version ("1.0.11.RELEASE")
    id("com.avast.gradle.docker-compose") version "0.14.11"
    id("com.bmuschko.docker-remote-api") version "7.1.0"
    id("com.github.spotbugs") version "5.0.2"
    id("com.diffplug.spotless") version "6.0.4"
    id("net.rdrei.android.buildtimetracker") version "0.11.0"
    id("com.github.davidmc24.gradle.plugin.avro") version ("1.3.0")
    id("org.sonarqube") version("3.3")
    jacoco
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2020.0.4")
    }
}

val inCodeBuild = !System.getenv("CODEBUILD_BUILD_ID").isNullOrEmpty()

avro {
    fieldVisibility.set("PRIVATE")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

// https://nvd.nist.gov/vuln/detail/CVE-2021-44228
ext["log4j2.version"] = "2.16.0"

spotbugs {
    toolVersion.set("4.2.2")
    effort.set(com.github.spotbugs.snom.Effort.MAX)
}

buildtimetracker {
    reporters {
        register("csv") {
            options.run {
                put("output", "$buildDir/times.csv")
                put("append", "true")
                put("header", "false")
            }
        }

        register("summary") {
            options.run {
                put("ordered", "false")
                put("threshold", "50")
                put("header", "false")
            }
        }

        register("csvSummary") {
            options.run {
                put("csv", "$buildDir/times.csv")
            }
        }
    }
}

sourceSets {
    val main by getting
    val test by getting

    val integrationTest by creating {
        compileClasspath += main.output + test.output
        runtimeClasspath += main.output + test.output
    }
}

configurations {
    val testImplementation by getting
    val testRuntimeOnly by getting
    "integrationTestImplementation" { extendsFrom(testImplementation) }
    "integrationTestRuntimeOnly" { extendsFrom(testRuntimeOnly) }
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

dependencies {

    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.4.2.Final")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-oauth2-jose:5.6.0")
    implementation("org.springframework.security:spring-security-oauth2-resource-server:5.6.0")
    implementation("org.springframework.security:spring-security-oauth2-client:5.6.0")

    implementation("com.fasterxml.jackson.core:jackson-core:2.13.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.avro:avro:1.11.0")
    implementation("io.confluent:kafka-avro-serializer:6.2.2")
    implementation("io.springfox:springfox-swagger-ui:3.0.0")
    implementation("io.springfox:springfox-boot-starter:3.0.0")
    implementation("com.github.derjust:spring-data-dynamodb:5.1.0")
    implementation("com.amazonaws:aws-dynamodb-encryption-java:2.0.3")
    implementation("org.mapstruct:mapstruct:1.4.2.Final")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testImplementation("org.apache.kafka:kafka-clients:3.0.0")
    testImplementation("org.mockito:mockito-core:4.1.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.1.0")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.assertj:assertj-core:3.21.0")
    testImplementation("com.consol.citrus:citrus-core:3.1.0")
    testImplementation("com.consol.citrus:citrus-junit5:3.1.0")
    testImplementation("com.consol.citrus:citrus-http:3.1.0")
    testImplementation("com.consol.citrus:citrus-java-dsl:3.1.0")
    testImplementation("com.consol.citrus:citrus-validation-json:3.1.0")
    testImplementation("org.springframework.boot:spring-boot-test")

}

tasks {

    val prepareDocker by creating(Copy::class) {
        dependsOn(assemble)
        from(
            "${buildDir.path}/libs/app.jar",
            "${projectDir}/src/main/resources",
            "$projectDir/Dockerfile"
        )
        destinationDir = file("$buildDir/docker")
    }

    val dockerBuildImage by creating(DockerBuildImage::class) {
        dependsOn(prepareDocker)

        images.add("happymoney/cookiecutter-service:latest")
    }

    val composeLogs by getting

    val composeDown by getting {
        dependsOn(composeLogs)
    }

    val composeUp by getting {
        dependsOn(dockerBuildImage)
    }

    compileJava {
        options.compilerArgs = listOf(
                "-Amapstruct.defaultComponentModel=spring",
                "-Amapstruct.unmappedTargetPolicy=ERROR"
        )
    }

    bootRun {
        environment("spring_profiles_active", "local")
    }

    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }

    val integrationTest by creating(Test::class) {
        dependsOn(composeUp)
        inputs.files(dockerBuildImage.outputs)
        finalizedBy(composeLogs)
        if(inCodeBuild) {
            finalizedBy(composeDown)
        }

        description = "Runs integration tests."
        group = "verification"

        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath
        useJUnitPlatform()
        shouldRunAfter("test")
    }

    check { dependsOn(integrationTest) }

    dockerCompose.exposeAsEnvironment(integrationTest)

    withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
        reports.maybeCreate("xml").isEnabled = false
        reports.maybeCreate("html").isEnabled = true
    }

    jacocoTestReport {
        dependsOn(test)
        finalizedBy(jacocoTestCoverageVerification)
        classDirectories.setFrom(
            sourceSets.main.get().output.asFileTree.matching {
                // File Patterns to exclude from testing & code coverage metrics
                exclude(
                    "com/happymoney/cookiecutterservice/domain/**",
                    "com/happymoney/cookiecutterservice/entity/*MapperImpl**",
                    "com/happymoney/cookiecutterservice/entity/dynamo/**",
                    "com/happymoney/cookiecutterservice/entity/event/**",
                    "com/happymoney/cookiecutterservice/entity/web/**",
                    "com/happymoney/cookiecutterservice/response/**",
                    "com/happymoney/cookiecutterservice/Application**")
            }
        )
    }

    jacocoTestCoverageVerification {
        violationRules {
            rule {
                classDirectories.setFrom(sourceSets.main.get().output.asFileTree.matching {
                    // File Patterns to exclude from testing & code coverage metrics
                    exclude(
                        "com/happymoney/cookiecutterservice/domain/**",
                        "com/happymoney/cookiecutterservice/entity/*MapperImpl**",
                        "com/happymoney/cookiecutterservice/entity/dynamo/**",
                        "com/happymoney/cookiecutterservice/entity/event/**",
                        "com/happymoney/cookiecutterservice/entity/web/**",
                        "com/happymoney/cookiecutterservice/response/**",
                        "com/happymoney/cookiecutterservice/Application**")
                })
                limit {
                    // Minimum code coverage % for the build to pass
                    minimum = "0.0".toBigDecimal()  //TODO: Raise this value
                }
            }
        }
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "HappyMoneyInc_cookiecutter-service")
        property("sonar.projectName", "cookiecutter-service")
        property("sonar.organization", "happymoneyinc")
        property("sonar.host.url", "https://sonarcloud.io")
        property ("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/jacocoTestReport.xml")
    }
}
