package me.davidffa.d4rkbotkt.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import dev.minn.jda.ktx.Embed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Translator
import me.davidffa.d4rkbotkt.audio.receive.ReceiverManager
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildMessageChannel
import net.dv8tion.jda.api.entities.Member
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.Instant
import kotlin.system.exitProcess

class TrackScheduler(private val player: AudioPlayer, private val textChannel: GuildMessageChannel) :
  AudioEventAdapter() {
  private val logger = LoggerFactory.getLogger(this::class.java)

  val queue = mutableListOf<Track>()
  lateinit var current: Track

  private val guild = textChannel.guild
  var npMessage: Long? = null

  var queueLoop = false
  var trackLoop = false

  fun t(path: String, placeholders: List<String>? = null): String {
    return Translator.t(path, D4rkBot.guildCache[guild.idLong]!!.locale, placeholders)
  }

  fun queue(audioTrack: AudioTrack, requester: Member) {
    val track = Track(audioTrack, requester)

    if (!this::current.isInitialized) {
      this.current = track
      this.player.startTrack(audioTrack, true)
    } else {
      this.queue.add(track)
    }
  }

  fun queue(unresolvedTrack: UnresolvedTrack) {
    val track = Track(unresolvedTrack, unresolvedTrack.requester)

    if (!this::current.isInitialized) {
      current = track
      CoroutineScope(Dispatchers.IO).launch {
        val buildedTrack = current.unresolvedTrack!!.build()

        if (buildedTrack == null) {
          nextTrack()
          return@launch
        }

        buildedTrack.unresolvedTrack = null
        current = buildedTrack

        player.startTrack(current.track, true)
      }
    } else {
      this.queue.add(track)
    }
  }

  fun nextTrack() {
    if (this.trackLoop) {
      this.player.startTrack(current.clone().track, false)
      return
    }

    if (this.queueLoop) {
      this.queue.add(current.clone())
    }

    if (this.queue.isEmpty()) {
      if (Utils.hasPermissions(guild.selfMember, textChannel, listOf(Permission.MESSAGE_SEND))) {
        this.textChannel.sendMessage(t("music.queueEnd")).queue()
      }

      this.destroy()
      return
    }

    this.current = this.queue.removeFirst()

    if (this.current.unresolvedTrack != null) {
      CoroutineScope(Dispatchers.IO).launch {
        val buildedTrack = current.unresolvedTrack!!.build()

        if (buildedTrack == null) {
          nextTrack()
          return@launch
        }

        buildedTrack.unresolvedTrack = null
        current = buildedTrack

        player.startTrack(current.track, false)
      }
      return
    }
    this.player.startTrack(this.current.track, false)
  }

  fun destroy() {
    if (this.npMessage != null) {
      this.textChannel.deleteMessageById(this.npMessage!!).queue()
      this.npMessage = null
    }

    this.queue.clear()
    this.player.destroy()

    if (ReceiverManager.receiveManagers.contains(guild.idLong)) {
      this.guild.audioManager.sendingHandler = null
    } else this.guild.audioManager.closeAudioConnection()

    val manager = PlayerManager.getMusicManager(guild.idLong)

    if (manager.djtableMessage != null) {
      this.textChannel.deleteMessageById(manager.djtableMessage!!).queue()
      manager.djtableMessage = null
    }

    PlayerManager.musicManagers.remove(this.guild.idLong)
  }

  override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
    if (endReason.mayStartNext) nextTrack()
  }

  override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
    if (this.npMessage != null) {
      this.textChannel.deleteMessageById(this.npMessage!!).queue()
      this.npMessage = null
    }

    if (!Utils.hasPermissions(
        guild.selfMember,
        textChannel,
        listOf(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS)
      )
    ) return

    val requester = current.requester

    val embed = Embed {
      title = t("music.playing.title")
      color = Utils.randColor()
      url = track.info.uri
      thumbnail = track.info.artworkUrl
      field {
        name = t("music.playing.name")
        value = "`${track.info.title}`"
        inline = false
      }
      field {
        name = t("music.playing.author")
        value = "`${track.info.author}`"
        inline = false
      }
      field {
        name = t("music.playing.duration")
        value = if (!track.info.isStream) "`${Utils.msToHour(track.info.length)}`" else ":infinity:"
        inline = false
      }
      footer {
        name = requester.user.asTag
        iconUrl = requester.user.effectiveAvatarUrl
      }
      timestamp = Instant.now()
    }

    textChannel.sendMessageEmbeds(embed).queue { this.npMessage = it.idLong }
  }

  override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
    logger.warn("Ocorreu um erro ao tocar a música ${track.info.identifier}.\nErro: ${exception.printStackTrace()}")
    if (exception.localizedMessage.contains("429")) {
      this.textChannel.sendMessage(t("music.ratelimit"))
        .queue()
      this.destroy()

      val appName = System.getenv("APPNAME")

      if (appName != null) {
        val req = Request.Builder()
          .url("https://api.heroku.com/apps/${appName}/dynos")
          .addHeader("Accept", "application/vnd.heroku+json; version=3")
          .addHeader("Authorization", "Bearer ${System.getenv("HEROKUTOKEN")}")
          .delete()
          .build()

        D4rkBot.okHttpClient.newCall(req).enqueue(object : Callback {
          override fun onFailure(call: Call, e: IOException) {
            exitProcess(1)
          }

          override fun onResponse(call: Call, response: Response) {
            if (response.code() != 202) exitProcess(1)
          }
        })
      }
    }
  }

  override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
    logger.warn("Música ${track.info.identifier} presa.\nThreshold: ${thresholdMs}ms")
  }

  override fun onTrackStuck(
    player: AudioPlayer,
    track: AudioTrack,
    thresholdMs: Long,
    stackTrace: Array<out StackTraceElement>
  ) {
    logger.warn("Música ${track.info.identifier} presa.\nThreshold: ${thresholdMs}ms\nErro: $stackTrace")
  }
}