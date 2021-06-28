package me.davidffa.d4rkbotkt.database

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId

data class UserDB @BsonCreator constructor(
    @BsonId
    val id: String
    //TODO
)