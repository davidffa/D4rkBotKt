package me.davidffa.d4rkbotkt.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.Embed
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import java.time.Instant

object PlayerManager {
    val musicManagers: HashMap<Long, GuildMusicManager> = HashMap()
    private val audioPlayerManager = DefaultAudioPlayerManager()

    init {
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager)
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager)
    }

    fun getMusicManager(guildId: Long): GuildMusicManager {
        val guildManager = this.musicManagers[guildId]
        if (guildManager != null) return guildManager
        throw Error("MusicManager does not exist for guild ${guildId}.")
    }

    private fun getMusicManager(guild: Guild, textChannel: TextChannel): GuildMusicManager {
        return this.musicManagers.computeIfAbsent(guild.idLong) {
            val guildMusicManager = GuildMusicManager(this.audioPlayerManager, textChannel)

            guild.audioManager.sendingHandler = guildMusicManager.sendHandler

            return@computeIfAbsent guildMusicManager
        }
    }

    fun loadAndPlay(requester: Member, channel: TextChannel, trackURL: String) {
        val musicManager = this.getMusicManager(channel.guild, channel)

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
}