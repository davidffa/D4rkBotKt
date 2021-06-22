package net.d4rkb.d4rkbotkt

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients

object Database {
    private val mongodbURI = System.getenv("MONGODBURI") ?: throw Error("MongoDBURI cannot be null!")

    private val connString = ConnectionString(mongodbURI)
    private val settings = MongoClientSettings.builder()
        .applyConnectionString(connString)
        .retryWrites(true)
        .build()

    private val mongoClient = MongoClients.create(settings)
    val db = mongoClient.getDatabase("main")
}