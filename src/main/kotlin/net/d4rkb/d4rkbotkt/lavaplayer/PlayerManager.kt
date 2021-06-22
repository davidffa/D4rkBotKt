package net.d4rkb.d4rkbotkt.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.d4rkb.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import java.time.Instant

object PlayerManager {
    private val musicManagers: HashMap<Long, GuildMusicManager> = HashMap()
    private val audioPlayerManager = DefaultAudioPlayerManager()

    init {
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager)
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager)
    }

    fun getMusicManagersSize(): Int {
        return this.musicManagers.size
    }

    fun hasMusicManager(guild: Guild): Boolean {
        return this.musicManagers.containsKey(guild.idLong)
    }

    fun getMusicManager(guild: Guild): GuildMusicManager {
        val guildManager = this.musicManagers[guild.idLong]
        if (guildManager != null) return guildManager
        throw Error("MusicManager does not exist for guild ${guild.id}.")
    }

    private fun getMusicManager(guild: Guild, textChannel: TextChannel): GuildMusicManager {
        return this.musicManagers.computeIfAbsent(guild.idLong) {
            val guildMusicManager = GuildMusicManager(this.audioPlayerManager, textChannel, guild)

            guild.audioManager.sendingHandler = guildMusicManager.sendHandler

            return@computeIfAbsent guildMusicManager
        }
    }

    fun deleteMusicManager(guildId: Long) {
        this.musicManagers.remove(guildId)
    }

    fun loadAndPlay(requester: Member, channel: TextChannel, trackUrl: String) {
        val musicManager = this.getMusicManager(channel.guild, channel)

        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, object: AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                musicManager.scheduler.queue(track, requester)

                if (!musicManager.scheduler.queue.isEmpty())
                    channel.sendMessage(":bookmark_tabs: Adicionado à lista `${track.info.title}`").queue()
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val tracks = playlist.tracks

                if (playlist.isSearchResult) {
                    musicManager.scheduler.queue(tracks[0], requester)
                    if (!musicManager.scheduler.queue.isEmpty())
                        channel.sendMessage(":bookmark_tabs: Adicionado à lista `${tracks[0].info.title}`").queue()
                    return
                }

                tracks.forEach { musicManager.scheduler.queue(it, requester) }

                val embed = EmbedBuilder()
                    .setTitle("<a:disco:803678643661832233> Playlist Carregada", trackUrl)
                    .setColor(Utils.randColor())
                    .addField(":page_with_curl: Nome:", "`${playlist.name}`", false)
                    .addField("<a:infinity:838759634361253929> Quantidade de músicas:", "`${tracks.size}`", false)
                    .addField(":watch: Duração:", "`${Utils.msToHour(tracks.map { it.duration }.reduce { acc, l -> acc + l })}`", false)
                    .setFooter(requester.user.asTag, requester.user.effectiveAvatarUrl)
                    .setTimestamp(Instant.now())
                    .build()

                channel.sendMessageEmbeds(embed).queue()
            }

            override fun noMatches() {
                channel.sendMessage(":x: Não encontrei nenhum resultado!").queue()
            }

            override fun loadFailed(exception: FriendlyException) {
                channel.sendMessage(":x: Ocorreu um erro ao carregar a música! (`${exception.message}`)").queue()
            }
        })
    }
}