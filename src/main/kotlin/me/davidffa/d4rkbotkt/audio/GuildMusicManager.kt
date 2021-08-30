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

  var leaveMessage: Long? = null
  var djtableMessage: Long? = null
  var leaveTimer: Timer? = null

  val filters = mutableListOf<Filter>()

  var volume = 1f
    set(value) {
      field = value
      if (value != 1f) {
        this.audioPlayer.setFilterFactory(FilterFactory(this.filters, this.volume))
      } else {
        if (this.filters.isEmpty()) {
          this.audioPlayer.setFilterFactory(null)
        } else {
          this.audioPlayer.setFilterFactory(FilterFactory(this.filters, this.volume))
        }
      }
    }

  init {
    this.audioPlayer.addListener(this.scheduler)
  }

  fun switchFilter(filter: Filter) {
    if (this.filters.contains(filter)) this.filters.remove(filter)
    else this.filters.add(filter)

    if (filters.isEmpty()) {
      this.clearFilters()
    } else {
      this.audioPlayer.setFilterFactory(FilterFactory(this.filters, this.volume))
    }
  }

  fun clearFilters() {
    this.filters.clear()
    if (this.volume == 1f) {
      this.audioPlayer.setFilterFactory(null)
      return
    }
    this.audioPlayer.setFilterFactory(FilterFactory(listOf(), this.volume))
  }
}