package com.github.davidffa.d4rkbotkt.audio

import net.dv8tion.jda.api.entities.Member

class UnresolvedTrack(
  val title: String,
  val artist: String,
  val duration: Long,
  val uri: String,
  val requester: Member
) {
  suspend fun build(): Track? {
    val track = try {
      PlayerManager.search("ytmsearch:$title - $artist", 1).firstOrNull()
    } catch (e: Exception) {
      null
    } ?: return null

    return Track(track, requester)
  }
}