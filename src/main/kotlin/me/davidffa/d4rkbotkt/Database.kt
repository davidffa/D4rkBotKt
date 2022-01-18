package me.davidffa.d4rkbotkt

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import me.davidffa.d4rkbotkt.database.BotDB
import me.davidffa.d4rkbotkt.database.GuildDB
import me.davidffa.d4rkbotkt.database.UserDB
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class Database {
  companion object {
    lateinit var botDB: CoroutineCollection<BotDB>
    lateinit var guildDB: CoroutineCollection<GuildDB>
    lateinit var userDB: CoroutineCollection<UserDB>
  }

  private val connString = ConnectionString(Credentials.MONGODBURI)
  private val pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
  private val codecRegistry =
    CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry)

  private val settings = MongoClientSettings.builder()
    .applyConnectionString(connString)
    .codecRegistry(codecRegistry)
    .retryWrites(true)
    .build()

  private val client = KMongo.createClient(settings).coroutine
  private val db = client.getDatabase("d4rkbotkt")

  init {
    botDB = db.getCollection("bots")
    guildDB = db.getCollection("guilds")
    userDB = db.getCollection("users")
  }
}