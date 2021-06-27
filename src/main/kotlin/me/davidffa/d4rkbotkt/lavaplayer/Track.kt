package me.davidffa.d4rkbotkt.lavaplayer

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Member

class Track(var track: AudioTrack, val requester: Member) {
    fun clone(): Track {
        this.track = this.track.makeClone()
        return this
    }
}