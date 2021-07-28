package me.davidffa.d4rkbotkt.audio.spotify

import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.audio.Track
import net.dv8tion.jda.api.entities.Member

class SpotifyTrack(
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