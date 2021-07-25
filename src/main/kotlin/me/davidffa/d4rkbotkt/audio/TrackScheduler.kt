package me.davidffa.d4rkbotkt.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import dev.minn.jda.ktx.Embed
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.Instant
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.system.exitProcess

class TrackScheduler(private val player: AudioPlayer, private val textChannel: TextChannel) : AudioEventAdapter() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    val queue: BlockingQueue<Track>
    lateinit var current: Track

    private val guild = textChannel.guild
    private var npMessage: Message? = null

    var queueLoop = false
    var trackLoop = false

    init {
        this.queue = LinkedBlockingQueue()
    }

    fun shuffle() {
        val cpy = ArrayList<Track>()
        this.queue.drainTo(cpy)
        cpy.shuffle()
        this.queue.addAll(cpy)
    }

    fun queue(audioTrack: AudioTrack, requester: Member) {
        val track = Track(audioTrack, requester)

        if (!this::current.isInitialized) {
            this.current = track
            this.player.startTrack(audioTrack, true)
        } else {
            this.queue.offer(track)
        }
    }

    fun nextTrack() {
        if (this.trackLoop) {
            this.player.startTrack(current.clone().track, false)
            return
        }

        if (this.queueLoop) {
            this.queue.offer(current.clone())
        }

        if (this.queue.isEmpty()) {
            if (Utils.hasPermissions(guild.selfMember, textChannel, listOf(Permission.MESSAGE_WRITE))) {
                this.textChannel.sendMessage(":notes: A lista de músicas acabou!").queue()
            }

            this.destroy()
            return
        }

        this.current = this.queue.poll()
        this.player.startTrack(this.current.track, false)
    }

    fun destroy() {
        if (this.npMessage != null) this.npMessage?.delete()?.queue()
        this.queue.clear()
        this.player.destroy()
        this.guild.audioManager.closeAudioConnection()

        val manager = PlayerManager.getMusicManager(guild.idLong)

        if (manager.djtableMessage != null) {
            manager.djtableMessage?.delete()?.queue()
            manager.djtableMessage = null
        }

        PlayerManager.musicManagers.remove(this.guild.idLong)
    }

    private fun getThumbnail(identifier: String, source: String): String {
        if (source == "youtube") {
            return "https://img.youtube.com/vi/$identifier/maxresdefault.jpg"
        }
        return "https://i.pinimg.com/564x/a3/a9/29/a3a929cc8d09e88815b89bc071ff4d8d.jpg"
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) nextTrack()
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        if (this.npMessage != null) {
            this.npMessage?.delete()?.queue()
            this.npMessage = null
        }

        if (!Utils.hasPermissions(
                guild.selfMember,
                textChannel,
                listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
            )
        ) return

        val requester = current.requester

        val embed = Embed {
            title = "<a:disco:803678643661832233> A tocar"
            color = Utils.randColor()
            url = track.info.uri
            thumbnail = getThumbnail(track.identifier, track.sourceManager.sourceName)
            field {
                name = ":page_with_curl: Nome:"
                value = "`${track.info.title}`"
                inline = false
            }
            field {
                name = ":technologist: Enviado por:"
                value = "`${track.info.author}`"
                inline = false
            }
            field {
                name = ":watch: Duração:"
                value = if (!track.info.isStream) "`${Utils.msToHour(track.info.length)}`" else ":infinity:"
                inline = false
            }
            footer {
                name = requester.user.asTag
                iconUrl = requester.user.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }

        textChannel.sendMessageEmbeds(embed).queue { this.npMessage = it }
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        logger.warn("Ocorreu um erro ao tocar a música ${track.info.identifier}.\nErro: ${exception.printStackTrace()}")
        if (exception.localizedMessage.contains("429")) {
            this.textChannel.sendMessage(":warning: Parece que o YouTube me impediu de tocar essa música, aguarda um momento enquanto resolvo esse problema e tenta novamente daqui a uns segundos.")
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

                D4rkBot.okHttpClient.newCall(req).enqueue(object: Callback {
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