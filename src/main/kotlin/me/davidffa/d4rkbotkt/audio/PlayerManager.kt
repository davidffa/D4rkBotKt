package me.davidffa.d4rkbotkt.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.EmbedBuilder
import me.davidffa.d4rkbotkt.audio.spotify.Spotify
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object PlayerManager {
    val musicManagers: HashMap<Long, GuildMusicManager> = HashMap()
    private val audioPlayerManager = DefaultAudioPlayerManager()
    private val spotify = Spotify(System.getenv("SPOTIFYID"), System.getenv("SPOTIFYSECRET"))

    init {
        AudioSourceManagers.registerRemoteSources(audioPlayerManager)
        AudioSourceManagers.registerLocalSource(audioPlayerManager)
        audioPlayerManager.configuration.isFilterHotSwapEnabled = true
    }

    fun getMusicManager(guildId: Long): GuildMusicManager {
        val guildManager = this.musicManagers[guildId]
        if (guildManager != null) return guildManager
        throw Error("MusicManager does not exist for guild ${guildId}.")
    }

    fun getMusicManager(guild: Guild, textChannel: TextChannel): GuildMusicManager {
        return this.musicManagers.computeIfAbsent(guild.idLong) {
            val guildMusicManager = GuildMusicManager(this.audioPlayerManager, textChannel)

            guild.audioManager.sendingHandler = guildMusicManager.sendHandler

            return@computeIfAbsent guildMusicManager
        }
    }

    suspend fun loadAndPlay(requester: Member, channel: TextChannel, trackURL: String) {
        val musicManager = this.getMusicManager(channel.guild, channel)

        val spotifyRegex = "(?:https://open\\.spotify\\.com/|spotify:)(.+)[/:]([A-Za-z0-9]+)".toRegex()
        val match = spotifyRegex.matchEntire(trackURL)

        if (match != null) {
            val groups = match.groupValues

            val embed = EmbedBuilder {
                color = 1947988
                timestamp = Instant.now()
                footer {
                    name = requester.user.asTag
                    iconUrl = requester.user.effectiveAvatarUrl
                }
            }

            when (groups[1]) {
                "track" -> musicManager.scheduler.queue(spotify.getTrack(groups[2], requester))
                "album" -> {
                    embed.title = "<:spotify:869245737282715689> Album carregado"
                    val album = spotify.getAlbum(groups[2], requester)
                    album.forEach { musicManager.scheduler.queue(it) }

                    embed.field {
                        name = "<a:infinity:838759634361253929> Quantidade de músicas:"
                        value = "`${album.size}`"
                        inline = false
                    }
                    embed.field {
                        name = ":watch: Duração:"
                        value = "`${Utils.msToHour(album.sumOf { it.duration })}`"
                        inline = false
                    }

                    channel.sendMessageEmbeds(embed.build()).queue()
                }
                "playlist" -> {
                    embed.title = "<:spotify:869245737282715689> Playlist carregada"
                    val playlist = spotify.getPlaylist(groups[2], requester)
                    playlist.forEach { musicManager.scheduler.queue(it) }

                    embed.field {
                        name = "<a:infinity:838759634361253929> Quantidade de músicas:"
                        value = "`${playlist.size}`"
                        inline = false
                    }
                    embed.field {
                        name = ":watch: Duração:"
                        value = "`${Utils.msToHour(playlist.sumOf { it.duration })}`"
                        inline = false
                    }

                    channel.sendMessageEmbeds(embed.build()).queue()
                }
            }

            return
        }

        this.audioPlayerManager.loadItemOrdered(musicManager, trackURL, object: AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                musicManager.scheduler.queue(track, requester)

                if (musicManager.scheduler.queue.isNotEmpty() && Utils.hasPermissions(channel.guild.selfMember, channel, listOf(Permission.MESSAGE_WRITE))) {
                    channel.sendMessage(":bookmark_tabs: Adicionado à lista `${track.info.title}`").queue()
                }
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val tracks = playlist.tracks

                if (playlist.isSearchResult) {
                    musicManager.scheduler.queue(tracks[0], requester)

                    if (musicManager.scheduler.queue.isNotEmpty() && Utils.hasPermissions(channel.guild.selfMember, channel, listOf(Permission.MESSAGE_WRITE))) {
                        channel.sendMessage(":bookmark_tabs: Adicionado à lista `${tracks[0].info.title}`").queue()
                    }
                    return
                }

                tracks.forEach { musicManager.scheduler.queue(it, requester) }

                if (Utils.hasPermissions(channel.guild.selfMember, channel, listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS))) {
                    val embed = Embed {
                        title = "<a:disco:803678643661832233> Playlist Carregada"
                        color = Utils.randColor()
                        url = trackURL
                        field {
                            name = ":page_with_curl: Nome:"
                            value = "`${playlist.name}`"
                            inline = false
                        }
                        field {
                            name = "<a:infinity:838759634361253929> Quantidade de músicas:"
                            value = "`${tracks.size}`"
                            inline = false
                        }
                        field {
                            name = ":watch: Duração:"
                            value = "`${Utils.msToHour(tracks.sumOf { it.duration })}`"
                            inline = false
                        }
                        footer {
                            name = requester.user.asTag
                            iconUrl = requester.user.effectiveAvatarUrl
                        }
                        timestamp = Instant.now()
                    }

                    channel.sendMessageEmbeds(embed).queue()
                }
            }

            override fun noMatches() {
                if (Utils.hasPermissions(channel.guild.selfMember, channel, listOf(Permission.MESSAGE_WRITE))) {
                    channel.sendMessage(":x: Não encontrei nenhum resultado!").queue()
                }
            }

            override fun loadFailed(exception: FriendlyException) {
                if (Utils.hasPermissions(channel.guild.selfMember, channel, listOf(Permission.MESSAGE_WRITE))) {
                    channel.sendMessage(":x: Ocorreu um erro ao carregar a música! (`${exception.message}`)").queue()
                }
            }
        })
    }

    fun decodeTrack(base64: String): AudioTrack {
        val b64 = Base64.getDecoder().decode(base64)
        return ByteArrayInputStream(b64).use {
            audioPlayerManager.decodeTrack(MessageInput(it)).decodedTrack
        }
    }

    fun encodeTrack(track: AudioTrack): String {
        return ByteArrayOutputStream().use {
            audioPlayerManager.encodeTrack(MessageOutput(it), track)
            Base64.getEncoder().encodeToString(it.toByteArray())
        }
    }

    suspend fun search(query: String, limit: Int? = null): List<AudioTrack> {
        return suspendCoroutine { cont ->
            val resultHandler = FunctionalResultHandler(
                { cont.resume(listOf(it)) },
                {
                    if (it.tracks.isEmpty()) {
                        cont.resumeWithException(IllegalStateException("No matches found!"))
                        return@FunctionalResultHandler
                    }
                    if (limit != null && it.isSearchResult) cont.resume(it.tracks.subList(0, limit))
                    else cont.resume(it.tracks)
                },
                { cont.resumeWithException(IllegalStateException("No matches found!")) },
                { cont.resumeWithException(it) }
            )

            audioPlayerManager.loadItem(query, resultHandler)
        }
    }
}