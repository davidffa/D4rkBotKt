package me.davidffa.d4rkbotkt.audio

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.davidffa.d4rkbotkt.audio.spotify.SpotifyTrack
import net.dv8tion.jda.api.entities.Member

class Track(var track: AudioTrack?, val requester: Member) {
  var spotifyTrack: SpotifyTrack? = null

  val title: String
    get() = track?.info?.title ?: spotifyTrack!!.title

  val duration: Long
    get() = track?.duration ?: spotifyTrack!!.duration

  val uri: String
    get() = track?.info?.uri ?: spotifyTrack!!.uri

  constructor(spotifyTrack: SpotifyTrack, requester: Member) : this(null, requester) {
    this.spotifyTrack = spotifyTrack
  }

  fun clone(): Track {
    this.track = this.track?.makeClone()
    return this
  }
}