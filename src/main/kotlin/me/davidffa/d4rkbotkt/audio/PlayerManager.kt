package me.davidffa.d4rkbotkt.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.EmbedBuilder
import me.davidffa.d4rkbotkt.audio.sources.Deezer
import me.davidffa.d4rkbotkt.audio.sources.Spotify
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

object PlayerManager {
  val musicManagers: HashMap<Long, GuildMusicManager> = HashMap()
  private val audioPlayerManager = DefaultAudioPlayerManager()
  private val spotify = Spotify(System.getenv("SPOTIFYID"), System.getenv("SPOTIFYSECRET"))
  private val deezer = Deezer()

  init {
    AudioSourceManagers.registerRemoteSources(audioPlayerManager)
    audioPlayerManager.configuration.isFilterHotSwapEnabled = true

    YoutubeHttpContextFilter.setPAPISID("i1ooiMWn1UvtoNmR/AfneNyM7MDMd7YWgx")
    YoutubeHttpContextFilter.setPSID("EwjkPUR-jT50R3vAmVLcPetI0oMcrZPDCxmXgYeazDkoiRm-Ly_CaM2FGPEom6W_OLOVSQ.")
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

    val spotifyRegex = "^(?:https?://(?:open\\.)?spotify\\.com|spotify)[/:](track|album|playlist)[/:]([a-zA-Z0-9]+)".toRegex()
    val deezerRegex = "^(?:https?://|)?(?:www\\.)?deezer\\.com/(?:\\w{2}/)?(track|album|playlist)/(\\d+)".toRegex()

    val spotifyMatch = spotifyRegex.find(trackURL)

    if (spotifyMatch != null) {
      val groups = spotifyMatch.groupValues

      val embed = EmbedBuilder {
        color = 1947988
        timestamp = Instant.now()
        footer {
          name = requester.user.asTag
          iconUrl = requester.user.effectiveAvatarUrl
        }
      }

      when (groups[1]) {
        "track" -> {
          val track = spotify.getTrack(groups[2], requester)
          musicManager.scheduler.queue(track)

          if (musicManager.scheduler.queue.isNotEmpty()) {
            embed.title = musicManager.scheduler.t("music.spotify.trackTitle")

            embed.field {
              name = musicManager.scheduler.t("music.title")
              value = "`${track.title}`"
              inline = false
            }

            embed.field {
              name = musicManager.scheduler.t("music.artist")
              value = "`${track.artist}`"
              inline = false
            }
            embed.field {
              name = musicManager.scheduler.t("music.duration")
              value = "`${Utils.msToHour(track.duration)}`"
              inline = false
            }
          } else return
        }
        "album" -> {
          embed.title = musicManager.scheduler.t("music.spotify.albumTitle")
          val album = spotify.getAlbum(groups[2], requester)
          album.forEach { musicManager.scheduler.queue(it) }

          embed.field {
            name = musicManager.scheduler.t("music.size")
            value = "`${album.size}`"
            inline = false
          }
          embed.field {
            name = musicManager.scheduler.t("music.duration")
            value = "`${Utils.msToHour(album.sumOf { it.duration })}`"
            inline = false
          }
        }
        "playlist" -> {
          embed.title = musicManager.scheduler.t("music.spotify.playlistTitle")
          val playlist = spotify.getPlaylist(groups[2], requester)
          playlist.forEach { musicManager.scheduler.queue(it) }

          embed.field {
            name = musicManager.scheduler.t("music.size")
            value = "`${playlist.size}`"
            inline = false
          }
          embed.field {
            name = musicManager.scheduler.t("music.duration")
            value = "`${Utils.msToHour(playlist.sumOf { it.duration })}`"
            inline = false
          }
        }
      }
      channel.sendMessageEmbeds(embed.build()).queue()
      return
    }

    val deezerMatch = deezerRegex.find(trackURL)

    if (deezerMatch != null) {
      val groups = deezerMatch.groupValues

      val embed = EmbedBuilder {
        color = 1973790
        timestamp = Instant.now()
        footer {
          name = requester.user.asTag
          iconUrl = requester.user.effectiveAvatarUrl
        }
      }

      when (groups[1]) {
        "track" -> {
          val track = deezer.getTrack(groups[2], requester)
          musicManager.scheduler.queue(track)

          if (musicManager.scheduler.queue.isNotEmpty()) {
            embed.title = musicManager.scheduler.t("music.deezer.trackTitle")

            embed.field {
              name = musicManager.scheduler.t("music.title")
              value = "`${track.title}`"
              inline = false
            }

            embed.field {
              name = musicManager.scheduler.t("music.artist")
              value = "`${track.artist}`"
              inline = false
            }
            embed.field {
              name = musicManager.scheduler.t("music.duration")
              value = "`${Utils.msToHour(track.duration)}`"
              inline = false
            }
          } else return
        }
        "album" -> {
          embed.title = musicManager.scheduler.t("music.deezer.albumTitle")
          val album = deezer.getAlbum(groups[2], requester)
          album.forEach { musicManager.scheduler.queue(it) }

          embed.field {
            name = musicManager.scheduler.t("music.size")
            value = "`${album.size}`"
            inline = false
          }
          embed.field {
            name = musicManager.scheduler.t("music.duration")
            value = "`${Utils.msToHour(album.sumOf { it.duration })}`"
            inline = false
          }
        }
        "playlist" -> {
          embed.title = musicManager.scheduler.t("music.deezer.playlistTitle")
          val playlist = deezer.getPlaylist(groups[2], requester)
          playlist.forEach { musicManager.scheduler.queue(it) }

          embed.field {
            name = musicManager.scheduler.t("music.size")
            value = "`${playlist.size}`"
            inline = false
          }
          embed.field {
            name = musicManager.scheduler.t("music.duration")
            value = "`${Utils.msToHour(playlist.sumOf { it.duration })}`"
            inline = false
          }
        }
      }
      channel.sendMessageEmbeds(embed.build()).queue()
      return
    }

    val url = if (!Utils.isUrl(trackURL)) "ytsearch:$trackURL" else trackURL

    this.audioPlayerManager.loadItemOrdered(musicManager, url, object : AudioLoadResultHandler {
      override fun trackLoaded(track: AudioTrack) {
        musicManager.scheduler.queue(track, requester)

        if (musicManager.scheduler.queue.isNotEmpty() && Utils.hasPermissions(
            channel.guild.selfMember,
            channel,
            listOf(Permission.MESSAGE_SEND)
          )
        ) {
          channel.sendMessage(musicManager.scheduler.t("music.queued", listOf(track.info.title))).queue()
        }
      }

      override fun playlistLoaded(playlist: AudioPlaylist) {
        val tracks = playlist.tracks

        if (playlist.isSearchResult) {
          musicManager.scheduler.queue(tracks[0], requester)

          if (musicManager.scheduler.queue.isNotEmpty() && Utils.hasPermissions(
              channel.guild.selfMember,
              channel,
              listOf(Permission.MESSAGE_SEND)
            )
          ) {
            channel.sendMessage(musicManager.scheduler.t("music.queued", listOf(tracks[0].info.title))).queue()
          }
          return
        }

        tracks.forEach { musicManager.scheduler.queue(it, requester) }

        if (Utils.hasPermissions(
            channel.guild.selfMember,
            channel,
            listOf(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS)
          )
        ) {
          val embed = Embed {
            title = musicManager.scheduler.t("music.playlistLoadedTitle")
            color = Utils.randColor()
            this.url = url
            field {
              name = musicManager.scheduler.t("music.playing.name")
              value = "`${playlist.name}`"
              inline = false
            }
            field {
              name = musicManager.scheduler.t("music.size")
              value = "`${tracks.size}`"
              inline = false
            }
            field {
              name = musicManager.scheduler.t("music.duration")
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
        if (Utils.hasPermissions(channel.guild.selfMember, channel, listOf(Permission.MESSAGE_SEND))) {
          channel.sendMessage(musicManager.scheduler.t("music.noMatches")).queue()
        }
      }

      override fun loadFailed(exception: FriendlyException) {
        if (Utils.hasPermissions(channel.guild.selfMember, channel, listOf(Permission.MESSAGE_SEND))) {
          channel.sendMessage(musicManager.scheduler.t("music.error", listOf(exception.message.toString()))).queue()
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
          if (limit != null && it.isSearchResult) cont.resume(it.tracks.slice(0 until min(limit, it.tracks.size)))
          else cont.resume(it.tracks)
        },
        { cont.resumeWithException(IllegalStateException("No matches found!")) },
        { cont.resumeWithException(it) }
      )

      audioPlayerManager.loadItem(query, resultHandler)
    }
  }
}