package me.davidffa.d4rkbotkt.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import me.davidffa.d4rkbotkt.audio.filters.Filter
import me.davidffa.d4rkbotkt.audio.filters.FilterFactory
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import java.util.*

class GuildMusicManager(manager: AudioPlayerManager, val textChannel: TextChannel) {
    val audioPlayer: AudioPlayer = manager.createPlayer()
    val scheduler = TrackScheduler(this.audioPlayer, textChannel)
    val sendHandler = AudioPlayerSendHandler(this.audioPlayer)

    var leaveMessage: Message? = null
    var djtableMessage: Message? = null
    var leaveTimer: Timer? = null

    val filters = ArrayList<Filter>()

    init {
        this.audioPlayer.addListener(this.scheduler)
    }

    fun switchFilter(filter: Filter) {
        if (this.filters.contains(filter)) this.filters.remove(filter)
        else this.filters.add(filter)

        if (filters.isEmpty()) {
            this.clearFilters()
        }else {
            this.audioPlayer.setFilterFactory(FilterFactory(this.filters))
        }
    }

    fun clearFilters() {
        this.filters.clear()
        this.audioPlayer.setFilterFactory(null)
    }
}