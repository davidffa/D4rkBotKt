package net.d4rkb.d4rkbotkt.database

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty

data class BotDB @BsonCreator constructor(
    @BsonId
    val id: String,
    @BsonProperty("commands")
    val commands: Int,
    @BsonProperty("lockedCmds")
    val lockedCmds: List<String>,
    @BsonProperty("blacklist")
    val blacklist: List<String>
)