package com.twitterclone.integration

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

/**
 * MongoDB TestContainer initializer that uses random ports
 * This creates a shared MongoDB container for all integration tests
 */
class MongoDBContainerInitializer {
    private static final Logger log = LoggerFactory.getLogger(MongoDBContainerInitializer.class)

    private static final MongoDBContainer MONGO_DB_CONTAINER

    static {
        log.info("Initializing MongoDB TestContainer with random port")

        MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:6.0.9"))
                .withCommand("--replSet rs0")

        MONGO_DB_CONTAINER.start()

        Integer mappedPort = MONGO_DB_CONTAINER.getMappedPort(27017)
        log.info("MongoDB container started on port: {}", mappedPort)

        try {
            MONGO_DB_CONTAINER.execInContainer(
                    "mongosh", "--eval",
                    "rs.initiate({_id: 'rs0', members: [{_id: 0, host: 'localhost:27017'}]})"
            )
            log.info("MongoDB replica set initialized successfully")
        } catch (Exception e) {
            log.warn("Error initializing MongoDB replica set: {}", e.getMessage())
        }

        String connectionString = MONGO_DB_CONTAINER.getReplicaSetUrl()
        log.info("MongoDB connection string: {}", connectionString)
        System.setProperty("spring.data.mongodb.uri", connectionString)
    }

    static MongoDBContainer getMongoDBContainer() {
        return MONGO_DB_CONTAINER
    }
}
