package me.davidffa.d4rkbotkt.commands.music

import com.mongodb.client.model.Updates
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.sendPaginator
import me.davidffa.d4rkbotkt.Database
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.database.Playlist
import me.davidffa.d4rkbotkt.database.UserDB
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.time.Instant

class Playlist : Command(
  "playlist",
  "Cria uma playlist, adiciona músicas a uma playlist ou adiciona à queue uma playlist.",
  category = "Music",
  botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS),
  cooldown = 5
) {
  override suspend fun run(ctx: CommandContext) {
    val manager = PlayerManager.musicManagers[ctx.guild.idLong]

    val userdata = Database.userDB.findOneById(ctx.author.id)

    if (userdata == null) {
      Database.userDB.insertOne(UserDB(ctx.author.id))
    }

    val helpEmbed = Embed {
      title = ctx.t("commands.playlist.help.title")
      description = "${
        ctx.t("commands.playlist.help.description", listOf(ctx.prefix)).split("\n").joinToString("─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
      }```"
      color = Utils.randColor()
      footer {
        name = ctx.author.asTag
        iconUrl = ctx.author.effectiveAvatarUrl
      }
      timestamp = Instant.now()
    }

    if (ctx.args.isEmpty()) {
      ctx.channel.sendMessageEmbeds(helpEmbed).queue()
      return
    }

    val playlists = userdata?.playlists

    when (ctx.args[0].lowercase()) {
      in listOf("criar", "create") -> {
        if (ctx.args.size < 2) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.usage.create", listOf(ctx.prefix))).queue()
          return
        }

        if (ctx.args[1].length > 32) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.playListNameLength")).queue()
          return
        }

        if (playlists != null) {
          if (playlists.size > 50) {
            ctx.channel.sendMessage(ctx.t("commands.playlist.errors.maxPlaylists")).queue()
            return
          }

          if (playlists.find { it.name == ctx.args[1] } != null) {
            ctx.channel.sendMessage(ctx.t("commands.playlist.sameNamePlaylist")).queue()
            return
          }

          Database.userDB.updateOneById(ctx.author.id, Updates.push("playlists", Playlist(ctx.args[1])))
        } else {
          Database.userDB.insertOne(UserDB(ctx.author.id, listOf(Playlist(ctx.args[1]))))
        }

        ctx.channel.sendMessage(ctx.t("commands.playlist.playlistCreated")).queue()
      }

      in listOf("rename", "renomear") -> {
        if (ctx.args.size < 3) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.usage.rename", listOf(ctx.prefix))).queue()
          return
        }

        if (playlists == null || playlists.isEmpty()) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.noPlaylists")).queue()
          return
        }

        if (ctx.args[2].length > 32) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.playListNameLength")).queue()
          return
        }

        val playlist = playlists.find { it.name == ctx.args[1] }

        Database.userDB.updateOneById(
          ctx.author.id,
          Updates.set("playlists.${playlists.indexOf(playlist)}.name", ctx.args[2])
        )

        ctx.channel.sendMessage(ctx.t("commands.playlist.renamed")).queue()
      }

      in listOf("deletar", "apagar", "excluir", "delete") -> {
        if (playlists == null || playlists.isEmpty()) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.noPlaylists")).queue()
          return
        }

        val playlist = playlists.find { it.name == ctx.args[1] }

        if (playlist == null) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.playlistNotFound")).queue()
          return
        }

        Database.userDB.updateOneById(ctx.author.id, Updates.pull("playlists", playlist))
        ctx.channel.sendMessage(ctx.t("commands.playlist.deleted")).queue()
      }

      in listOf("lista", "listar", "list") -> {
        if (playlists == null || playlists.isEmpty()) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.noPlaylists")).queue()
          return
        }

        val embed = Embed {
          title = ctx.t("commands.playlist.list.title")
          color = Utils.randColor()
          description = playlists.joinToString("\n") {
            if (it.tracks != null) "${it.name} - `${it.tracks.size}` ${ctx.t("commands.playlist.tracks")}"
            else "${it.name} - `0` ${ctx.t("commands.playlist.tracks")}"
          }
          footer {
            name = ctx.author.asTag
            iconUrl = ctx.author.effectiveAvatarUrl
          }
          timestamp = Instant.now()
        }

        ctx.channel.sendMessageEmbeds(embed).queue()
      }

      in listOf("detalhes", "details") -> {
        if (playlists == null || playlists.isEmpty()) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.noPlaylists")).queue()
          return
        }

        if (ctx.args.size < 2) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.usage.details", listOf(ctx.prefix))).queue()
          return
        }

        val playlist = playlists.find { it.name == ctx.args[1] }

        if (playlist == null) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.playlistNotFound")).queue()
          return
        }

        if (playlist.tracks == null || playlist.tracks.isEmpty()) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.emptyPlaylist")).queue()
          return
        }

        val header = "**${playlist.name}** - `${playlist.tracks.size}` ${ctx.t("commands.playlist.tracks")}\n\n"

        val chunkedTracks = playlist.tracks.chunked(10)

        val pages = chunkedTracks.map {
          Embed {
            title = ctx.t("commands.playlist.details.title")
            description = header +
                    it.mapIndexed { index, base64 ->
                      val track = PlayerManager.decodeTrack(base64)
                      "${index + (chunkedTracks.indexOf(it) * 10) + 1}º - [${track.info.title}](${track.info.uri})"
                    }.joinToString("\n")
            color = Utils.randColor()
            footer {
              name = ctx.t("commands.playlist.details.page", listOf((chunkedTracks.indexOf(it) + 1).toString(), chunkedTracks.size.toString()))
              iconUrl = ctx.author.effectiveAvatarUrl
            }
            timestamp = Instant.now()
          }
        }.toTypedArray()

        if (playlist.tracks.size <= 10) {
          ctx.channel.sendMessageEmbeds(pages.first()).queue()
          return
        }

        ctx.channel.sendPaginator(*pages, expireAfter = 10 * 60 * 1000L, filter = {
          if (it.user.idLong == ctx.author.idLong) return@sendPaginator true
          return@sendPaginator false
        }).queue()
      }

      in listOf("remove", "remover") -> {
        if (playlists == null || playlists.isEmpty()) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.noPlaylists")).queue()
          return
        }

        if (ctx.args.size < 3) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.usage.remove", listOf(ctx.prefix))).queue()
          return
        }

        val playlist = playlists.find { it.name == ctx.args[1] }

        if (playlist == null) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.playlistNotFound")).queue()
          return
        }

        if (playlist.tracks == null || playlist.tracks.isEmpty()) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.emptyPlaylist")).queue()
          return
        }

        val id = ctx.args[2].toIntOrNull()

        if (id == null) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.notNumber")).queue()
          return
        }

        val base64 = playlist.tracks.getOrNull(id - 1)

        if (base64 == null) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.invalidId", listOf(ctx.prefix)))
            .queue()
          return
        }

        val track = PlayerManager.decodeTrack(base64)

        Database.userDB.updateOneById(
          ctx.author.id,
          Updates.pull("playlists.${playlists.indexOf(playlist)}.tracks", base64)
        )

        ctx.channel.sendMessage(ctx.t("commands.playlist.remove", listOf(track.info.title)))
          .queue()
      }

      in listOf("add", "adicionar") -> {
        if (playlists == null || playlists.isEmpty()) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.noPlaylists")).queue()
          return
        }

        if (ctx.args.size < 2) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.usage.add", listOf(ctx.prefix))).queue()
          return
        }

        val playlist = playlists.find { it.name == ctx.args[1] }

        if (playlist == null) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.playlistNotFound")).queue()
          return
        }

        val tracks: List<AudioTrack>

        if (ctx.args.size >= 3) {
          var query = ctx.args.subList(2, ctx.args.size).joinToString(" ")

          if (!Utils.isUrl(query)) {
            query = "ytsearch:$query"
          }

          try {
            tracks = PlayerManager.search(query, 1)
          } catch (e: Exception) {
            return
          }
        } else {
          if (manager != null) {
            tracks = listOf(manager.scheduler.current.track!!)
          } else {
            return
          }
        }

        if (tracks.size + (playlist.tracks?.size ?: 0) > 70) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.maxPlaylistSize")).queue()
          return
        }

        val base64Array = tracks.map { PlayerManager.encodeTrack(it) }.filter {
          if (playlist.tracks != null) {
            !playlist.tracks.contains(it)
          }
          true
        }

        if (base64Array.isEmpty()) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.trackAlreadyExists")).queue()
          return
        }

        Database.userDB.updateOneById(
          ctx.author.id,
          Updates.pushEach(
            "playlists.${playlists.indexOf(playlist)}.tracks",
            base64Array
          )
        )

        if (tracks.size == 1) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.add.track", listOf(tracks[0].info.title)))
            .queue()
          return
        }

        ctx.channel.sendMessage(ctx.t("commands.playlist.add.tracks", listOf(base64Array.size.toString())))
          .queue()
      }

      in listOf("shuffle", "embaralhar") -> {
        if (playlists == null || playlists.isEmpty()) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.noPlaylists")).queue()
          return
        }

        if (ctx.args.size < 2) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.usage.shuffle", listOf(ctx.prefix))).queue()
          return
        }

        val playlist = playlists.find { it.name == ctx.args[1] }

        if (playlist == null) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.playlistNotFound")).queue()
          return
        }

        if (playlist.tracks == null || playlist.tracks.isEmpty()) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.emptyPlaylist")).queue()
          return
        }

        Database.userDB.updateOneById(
          ctx.author.id,
          Updates.set("playlists.${playlists.indexOf(playlist)}.tracks", playlist.tracks.shuffled())
        )

        ctx.channel.sendMessage(ctx.t("commands.playlist.shuffle")).queue()
      }

      in listOf("play", "tocar") -> {
        if (!Utils.canPlay(ctx.selfMember, ctx.member, ctx.channel)) return

        if (playlists == null || playlists.isEmpty()) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.noPlaylists")).queue()
          return
        }

        if (ctx.args.size < 2) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.usage.play", listOf(ctx.prefix))).queue()
          return
        }

        val playlist = playlists.find { it.name == ctx.args[1] }

        if (playlist == null) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.playlistNotFound")).queue()
          return
        }

        if (playlist.tracks == null || playlist.tracks.isEmpty()) {
          ctx.channel.sendMessage(ctx.t("commands.playlist.errors.emptyPlaylist")).queue()
          return
        }

        if (!ctx.selfMember.voiceState!!.inVoiceChannel()) {
          ctx.guild.audioManager.isSelfDeafened = true
          ctx.guild.audioManager.isSelfMuted = false
          ctx.guild.audioManager.openAudioConnection(ctx.member.voiceState?.channel)
        }

        val tracks = playlist.tracks.map { PlayerManager.decodeTrack(it) }

        val musicManager = PlayerManager.getMusicManager(ctx.guild, ctx.channel)
        tracks.forEach {
          musicManager.scheduler.queue(it, ctx.member)
        }

        val embed = Embed {
          title = ctx.t("commands.playlist.play.title")
          field {
            name = ctx.t("commands.playlist.play.name")
            value = "`${playlist.name}`"
            inline = false
          }
          field {
            name = ctx.t("commands.playlist.play.amount")
            value = "`${playlist.tracks.size}`"
            inline = false
          }
          field {
            name = ctx.t("commands.playlist.play.duration")
            value = "`${Utils.msToHour(tracks.sumOf { it.duration })}`"
            inline = false
          }
          color = Utils.randColor()
          footer {
            name = ctx.author.asTag
            iconUrl = ctx.author.effectiveAvatarUrl
          }
          timestamp = Instant.now()
        }

        ctx.channel.sendMessageEmbeds(embed).queue()
      }

      else -> {
        ctx.channel.sendMessageEmbeds(helpEmbed).queue()
      }
    }
  }
}