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

class TrackScheduler(val player: AudioPlayer, private val guild: Guild): AudioEventAdapter() {
    val queue: BlockingQueue<Track>
    private var currentTrack: Track? = null
    private var lastMessageSent: Message? = null

    init {
        this.queue = LinkedBlockingQueue()
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
        if (this.queue.isEmpty()) {
            val textChannel = PlayerManager.getMusicManager(this.guild).textChannel

            textChannel.sendMessage(":notes: A lista de músicas acabou!").queue()
            this.destroy(guild.idLong)
            return
        }

        this.currentTrack = this.queue.poll()
        this.player.startTrack(currentTrack?.track, false)
    }

    private fun destroy(guildId: Long) {
        this.lastMessageSent!!.delete().queue()
        this.player.destroy()
        this.guild.audioManager.closeAudioConnection()
        PlayerManager.deleteMusicManager(guildId)
    }

    private fun getThumbnail(identifier: String, source: String): String {
        if (source == "youtube") {
            return "https://img.youtube.com/vi/$identifier/maxresdefault.jpg"
        }
        return ""
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        val textChannel = PlayerManager.getMusicManager(this.guild).textChannel

        if (endReason == AudioTrackEndReason.STOPPED) {
            textChannel.sendMessage(":stop_button: Parei a música!").queue()
            this.destroy(this.guild.idLong)
            return
        }

        if (endReason.mayStartNext) {
            nextTrack()
        }
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        if (this.lastMessageSent != null)
            this.lastMessageSent!!.delete().queue()

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

        textChannel.sendMessageEmbeds(embed).queue{ this.lastMessageSent = it }
    }
}