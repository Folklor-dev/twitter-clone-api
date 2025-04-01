package com.twitterclone.integration

import com.mongodb.client.MongoClients
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory

@TestConfiguration
class TestConfig {

    @Bean
    MongoTemplate mongoTemplate() {
        String connectionString = "mongodb://localhost:27017/test"
        MongoDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(
                MongoClients.create(connectionString), "test"
        )
        return new MongoTemplate(factory)
    }
}
