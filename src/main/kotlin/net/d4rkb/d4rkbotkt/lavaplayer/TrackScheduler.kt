package net.d4rkb.d4rkbotkt.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.d4rkb.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import java.time.Instant
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(private val player: AudioPlayer, private val guild: Guild): AudioEventAdapter() {
    val queue: BlockingQueue<Track>
    var currentTrack: Track? = null
    private var lastMessageSent: Message? = null
    var queueLoop = false
    var trackLoop = false

    init {
        this.queue = LinkedBlockingQueue()
    }

    fun shuffle() {
        val arr = ArrayList<Track>()
        this.queue.drainTo(arr)
        arr.shuffle()
        this.queue.addAll(arr)
    }

    fun queue(track: AudioTrack, requester: Member) {
        if (this.queue.isEmpty()) {
            this.currentTrack = Track(track, requester)
        }

        if (!this.player.startTrack(track, true)) {
            this.queue.offer(Track(track, requester))
        }
    }

    fun nextTrack() {
        if (this.trackLoop) {
            this.player.startTrack(currentTrack!!.track.makeClone(), false)
            return
        }

        if (this.queueLoop) {
            this.queue(currentTrack!!.track.makeClone(), currentTrack!!.requester)
        }

        if (this.queue.isEmpty()) {
            val textChannel = PlayerManager.getMusicManager(this.guild).textChannel

            textChannel.sendMessage(":notes: A lista de músicas acabou!").queue()
            this.destroy()
            return
        }

        this.currentTrack = this.queue.poll()
        this.player.startTrack(currentTrack?.track, false)
    }

    fun destroy() {
        if (this.lastMessageSent != null) this.lastMessageSent?.delete()?.queue()
        this.queue.clear()
        this.player.destroy()
        this.guild.audioManager.closeAudioConnection()
        PlayerManager.deleteMusicManager(guild.idLong)
    }

    private fun getThumbnail(identifier: String, source: String): String {
        if (source == "youtube") {
            return "https://img.youtube.com/vi/$identifier/maxresdefault.jpg"
        }
        return ""
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        if (this.lastMessageSent != null) {
            this.lastMessageSent!!.delete().queue()
            this.lastMessageSent = null
        }

        val requester = currentTrack!!.requester
        val textChannel = PlayerManager.getMusicManager(this.guild).textChannel

        val embed = EmbedBuilder()
            .setTitle("<a:disco:803678643661832233> A tocar", track.info.uri)
            .setColor(Utils.randColor())
            .addField(":page_with_curl: Nome:", "`${track.info.title}`", false)
            .addField(":technologist: Enviado por:", "`${track.info.author}`", false)
            .addField(":watch: Duração:", if (!track.info.isStream) "`${Utils.msToHour(track.info.length)}`" else ":infinity:", false)
            .setThumbnail(getThumbnail(track.info.identifier, track.sourceManager.sourceName))
            .setFooter(requester.user.asTag, requester.user.effectiveAvatarUrl)
            .setTimestamp(Instant.now())
            .build()

        textChannel.sendMessageEmbeds(embed).complete().also { this.lastMessageSent = it }
    }
}