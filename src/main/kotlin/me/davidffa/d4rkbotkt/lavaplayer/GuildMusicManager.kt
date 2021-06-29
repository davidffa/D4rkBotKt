package me.davidffa.d4rkbotkt.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import net.dv8tion.jda.api.entities.TextChannel

class GuildMusicManager(manager: AudioPlayerManager, textChannel: TextChannel) {
    val audioPlayer: AudioPlayer = manager.createPlayer()
    val scheduler = TrackScheduler(this.audioPlayer, textChannel)
    val sendHandler = AudioPlayerSendHandler(this.audioPlayer)

    init {
        this.audioPlayer.addListener(this.scheduler)
    }
}