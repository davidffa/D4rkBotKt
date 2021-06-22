package net.d4rkb.d4rkbotkt.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel

class GuildMusicManager(manager: AudioPlayerManager, val textChannel: TextChannel, guild: Guild) {
    private val audioPlayer = manager.createPlayer()
    val scheduler = TrackScheduler(this.audioPlayer, guild)
    val sendHandler = AudioPlayerSendHandler(this.audioPlayer)

    init {
        this.audioPlayer.addListener(this.scheduler)
    }
}