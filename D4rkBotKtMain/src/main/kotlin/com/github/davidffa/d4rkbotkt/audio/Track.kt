package com.github.davidffa.d4rkbotkt.audio

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Member

class Track(var track: AudioTrack?, val requester: Member) {
  var unresolvedTrack: UnresolvedTrack? = null

  val title: String
    get() = track?.info?.title ?: unresolvedTrack!!.title

  val duration: Long
    get() = track?.duration ?: unresolvedTrack!!.duration

  val uri: String
    get() = track?.info?.uri ?: unresolvedTrack!!.uri

  constructor(unresolvedTrack: UnresolvedTrack, requester: Member) : this(null, requester) {
    this.unresolvedTrack = unresolvedTrack
  }

  fun clone(): Track {
    this.track = this.track?.makeClone()
    return this
  }
}