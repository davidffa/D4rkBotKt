package com.github.davidffa.d4rkbotkt.database

import com.mongodb.lang.Nullable
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty

data class UserDB @BsonCreator constructor(
  @BsonId
  val id: String,
  @Nullable @BsonProperty("playlists")
  val playlists: List<Playlist> = listOf()
)

data class Playlist @BsonCreator constructor(
  @BsonProperty("name")
  val name: String,
  @Nullable @BsonProperty("tracks")
  val tracks: List<String>? = null
)