package net.d4rkb.d4rkbotkt

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import net.d4rkb.d4rkbotkt.database.*
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider

object Database {
    private val mongodbURI = System.getenv("MONGODBURI") ?: throw Error("MongoDBURI cannot be null!")

    private val connString = ConnectionString(mongodbURI)
    private val pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
    private val codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry)

    private val settings = MongoClientSettings.builder()
        .applyConnectionString(connString)
        .codecRegistry(codecRegistry)
        .retryWrites(true)
        .build()

    private val mongoClient = MongoClients.create(settings)
    private val db = mongoClient.getDatabase("d4rkbotkt")

    val botDB: MongoCollection<BotDB> = db.getCollection("bots", BotDB::class.java)
    val guildDB: MongoCollection<GuildDB> = db.getCollection("guilds", GuildDB::class.java)
    val userDB: MongoCollection<UserDB> = db.getCollection("users", UserDB::class.java)
}