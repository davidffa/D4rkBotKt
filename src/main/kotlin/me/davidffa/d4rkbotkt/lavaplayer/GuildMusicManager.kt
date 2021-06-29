package me.davidffa.d4rkbotkt.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import java.util.*

class GuildMusicManager(manager: AudioPlayerManager, val textChannel: TextChannel) {
    val audioPlayer: AudioPlayer = manager.createPlayer()
    val scheduler = TrackScheduler(this.audioPlayer, textChannel)
    val sendHandler = AudioPlayerSendHandler(this.audioPlayer)
    var leaveMessage: Message? = null
    var leaveTimer: Timer? = null

    init {
        this.audioPlayer.addListener(this.scheduler)
    }
}