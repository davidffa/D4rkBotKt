package me.davidffa.d4rkbotkt.database

import com.mongodb.lang.Nullable
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty

data class GuildDB @BsonCreator constructor(
  @BsonId
  val id: String,
  @Nullable @BsonProperty("prefix")
  val prefix: String? = null,
  @Nullable @BsonProperty("disabledCmds")
  val disabledCmds: List<String>? = null,
  @Nullable @BsonProperty("welcomeMessagesEnabled")
  val welcomeMessagesEnabled: Boolean? = null,
  @Nullable @BsonProperty("welcomeChatID")
  val welcomeChatID: String? = null,
  @Nullable @BsonProperty("memberRemoveChatID")
  val memberRemoveChatID: String? = null,
  @Nullable @BsonProperty("memberRemoveMessagesEnabled")
  val memberRemoveMessagesEnabled: Boolean? = null,
  @Nullable @BsonProperty("autoRole")
  val autoRole: String? = null,
  @Nullable @BsonProperty("djRole")
  val djRole: String? = null
)