package com.twitterclone.integration

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

/**
 * Base class for all integration tests that need MongoDB
 * Ensures MongoDB container with random port is started once and reused across all tests
 */
@Testcontainers
abstract class IntegrationTestBase extends Specification {
    @Shared
    static final MongoDBContainer mongoDBContainer = MongoDBContainerInitializer.getMongoDBContainer()

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl())
        registry.add("spring.data.mongodb.auto-index-creation", () -> "true")
    }
}
