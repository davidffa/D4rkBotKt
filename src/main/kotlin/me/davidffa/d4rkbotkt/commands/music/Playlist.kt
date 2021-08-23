package me.davidffa.d4rkbotkt.commands.music

import com.mongodb.client.model.Updates
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.sendPaginator
import me.davidffa.d4rkbotkt.Database
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.audio.receive.ReceiverManager
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
    val prefix = ctx.prefix
    val manager = PlayerManager.musicManagers[ctx.guild.idLong]

    val userdata = Database.userDB.findOneById(ctx.author.id)

    if (userdata == null) {
      Database.userDB.insertOne(UserDB(ctx.author.id))
    }

    val helpEmbed = Embed {
      title = "Ajuda do comando PlayList"
      description = "${
        listOf(
          "```md\n# ${prefix}playlist criar <Nome> - Cria uma playlist\n",
          "# ${prefix}playlist apagar <Nome> - Apaga uma playlist\n",
          "# ${prefix}playlist renomear <Nome Antigo> <Nome Novo> - Renomeia uma playlist\n",
          "# ${prefix}playlist detalhes <Nome> - Lista todas as músicas de uma playlist\n",
          "# ${prefix}playlist shuffle <Nome> - Embaralha as músicas de uma playlist\n",
          "# ${prefix}playlist listar - Lista de todas as tuas playlists\n",
          "# ${prefix}playlist adicionar <Nome> [Nome da música] - Adiciona a uma playlist a música que está a tocar ou uma música específica\n",
          "# ${prefix}playlist remover <Nome> <Número da música> - Remove uma música da playlist\n",
          "# ${prefix}playlist tocar <Nome> - Adiciona à queue todas as músicas de uma playlist"
        ).joinToString("─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
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
          ctx.channel.sendMessage(":x: **Usa:** ${prefix}playlist criar <Nome da PlayList>").queue()
          return
        }

        if (ctx.args[1].length > 32) {
          ctx.channel.sendMessage(":x: O nome da playlist não pode ter mais do que 32 caracteres.").queue()
          return
        }

        if (playlists != null) {
          if (playlists.size > 50) {
            ctx.channel.sendMessage(":x: Não podes ter mais de 50 playlists").queue()
            return
          }

          if (playlists.find { it.name == ctx.args[1] } != null) {
            ctx.channel.sendMessage(":x: Já tens uma playlist com esse nome!").queue()
            return
          }

          Database.userDB.updateOneById(ctx.author.id, Updates.push("playlists", Playlist(ctx.args[1])))
        } else {
          Database.userDB.insertOne(UserDB(ctx.author.id, listOf(Playlist(ctx.args[1]))))
        }

        ctx.channel.sendMessage("<a:disco:803678643661832233> Playlist criada com sucesso!").queue()
      }

      in listOf("rename", "renomear") -> {
        if (ctx.args.size < 3) {
          ctx.channel.sendMessage(":x: **Usa:** ${prefix}playlist renomear <Nome Antigo> <Nome Novo>").queue()
          return
        }

        if (playlists == null || playlists.isEmpty()) {
          ctx.channel.sendMessage(":x: Não tens nenhuma playlist!").queue()
          return
        }

        if (ctx.args[2].length > 32) {
          ctx.channel.sendMessage(":x: O nome da playlist não pode ter mais de 32 caracteres!").queue()
          return
        }

        val playlist = playlists.find { it.name == ctx.args[1] }

        Database.userDB.updateOneById(
          ctx.author.id,
          Updates.set("playlists.${playlists.indexOf(playlist)}.name", ctx.args[2])
        )

        ctx.channel.sendMessage(":bookmark: Playlist renomeada com sucesso!").queue()
      }

      in listOf("deletar", "apagar", "excluir", "delete") -> {
        if (playlists == null || playlists.isEmpty()) {
          ctx.channel.sendMessage(":x: Não tens nenhuma playlist!").queue()
          return
        }

        val playlist = playlists.find { it.name == ctx.args[1] }

        if (playlist == null) {
          ctx.channel.sendMessage(":x: Não tens nenhuma playlist com esse nome!").queue()
          return
        }

        Database.userDB.updateOneById(ctx.author.id, Updates.pull("playlists", playlist))
        ctx.channel.sendMessage(":wastebasket: Playlist apagada com sucesso!").queue()
      }

      in listOf("lista", "listar", "list") -> {
        if (playlists == null || playlists.isEmpty()) {
          ctx.channel.sendMessage(":x: Não tens nenhuma playlist!").queue()
          return
        }

        val embed = Embed {
          title = "<a:disco:803678643661832233> Lista de Playlists"
          color = Utils.randColor()
          description = playlists.joinToString("\n") {
            if (it.tracks != null) "${it.name} - `${it.tracks.size}` músicas"
            else "${it.name} - `0` músicas"
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
          ctx.channel.sendMessage(":x: Não tens nenhuma playlist!").queue()
          return
        }

        if (ctx.args.size < 2) {
          ctx.channel.sendMessage(":x: **Usa:** ${prefix}playlist detalhes <Nome da playlist>").queue()
          return
        }

        val playlist = playlists.find { it.name == ctx.args[1] }

        if (playlist == null) {
          ctx.channel.sendMessage(":x: Não tens nenhuma playlist com esse nome!").queue()
          return
        }

        if (playlist.tracks == null || playlist.tracks.isEmpty()) {
          ctx.channel.sendMessage(":x: Essa playlist não tem músicas!").queue()
          return
        }

        val header = "**${playlist.name}** - `${playlist.tracks.size}` músicas\n\n"

        val chunkedTracks = playlist.tracks.chunked(10)

        val pages = chunkedTracks.map {
          Embed {
            title = "<a:disco:803678643661832233> Lista de Músicas"
            description = header +
                    it.mapIndexed { index, base64 ->
                      val track = PlayerManager.decodeTrack(base64)
                      "${index + (chunkedTracks.indexOf(it) * 10) + 1}º - [${track.info.title}](${track.info.uri})"
                    }.joinToString("\n")
            color = Utils.randColor()
            footer {
              name = "Página ${chunkedTracks.indexOf(it) + 1} de ${chunkedTracks.size}"
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
          ctx.channel.sendMessage(":x: Não tens nenhuma playlist!").queue()
          return
        }

        if (ctx.args.size < 3) {
          ctx.channel.sendMessage(":x: **Usa:** ${prefix}playlist remover <Nome da playlist> <ID>").queue()
          return
        }

        val playlist = playlists.find { it.name == ctx.args[1] }

        if (playlist == null) {
          ctx.channel.sendMessage(":x: Não tens nenhuma playlist com esse nome!").queue()
          return
        }

        if (playlist.tracks == null || playlist.tracks.isEmpty()) {
          ctx.channel.sendMessage(":x: Essa playlist não tem nenhuma música!").queue()
          return
        }

        val id = ctx.args[2].toIntOrNull()

        if (id == null) {
          ctx.channel.sendMessage(":x: O ID da música é um número!").queue()
          return
        }

        val base64 = playlist.tracks.getOrNull(id - 1)

        if (base64 == null) {
          ctx.channel.sendMessage(":x: ID da música inválido!\n**Usa:** ${prefix}playlist detalhes <Nome> para ver o id da música a remover.")
            .queue()
          return
        }

        val track = PlayerManager.decodeTrack(base64)

        Database.userDB.updateOneById(
          ctx.author.id,
          Updates.pull("playlists.${playlists.indexOf(playlist)}.tracks", base64)
        )

        ctx.channel.sendMessage("<a:verificado:803678585008816198> Removeste a música `${track.info.title}` da playlist!")
          .queue()
      }

      in listOf("add", "adicionar") -> {
        if (playlists == null || playlists.isEmpty()) {
          ctx.channel.sendMessage(":x: Não tens nenhuma playlist!").queue()
          return
        }

        if (ctx.args.size < 2) {
          ctx.channel.sendMessage(":x: **Usa:** ${prefix}playlist add <Nome da playlist> [Música]").queue()
          return
        }

        val playlist = playlists.find { it.name == ctx.args[1] }

        if (playlist == null) {
          ctx.channel.sendMessage(":x: Não tens nenhuma playlist com esse nome!").queue()
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
          ctx.channel.sendMessage(":x: A playlist pode ter no máximo 70 músicas!").queue()
          return
        }

        val base64Array = tracks.map { PlayerManager.encodeTrack(it) }.filter {
          if (playlist.tracks != null) {
            !playlist.tracks.contains(it)
          }
          true
        }

        if (base64Array.isEmpty()) {
          ctx.channel.sendMessage(":x: Essa música já está na playlist!").queue()
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
          ctx.channel.sendMessage("<a:disco:803678643661832233> Música `${tracks[0].info.title}` adicionada à playlist!")
            .queue()
          return
        }

        ctx.channel.sendMessage("<a:disco:803678643661832233> `${base64Array.size}` músicas adicionadas à playlist")
          .queue()
      }

      in listOf("shuffle", "embaralhar") -> {
        if (playlists == null || playlists.isEmpty()) {
          ctx.channel.sendMessage(":x: Não tens nenhuma playlist!").queue()
          return
        }

        if (ctx.args.size < 2) {
          ctx.channel.sendMessage(":x: **Usa:** ${prefix}playlist shuffle <Nome da playlist>").queue()
          return
        }

        val playlist = playlists.find { it.name == ctx.args[1] }

        if (playlist == null) {
          ctx.channel.sendMessage(":x: Não tens nenhuma playlist com esse nome!").queue()
          return
        }

        if (playlist.tracks == null || playlist.tracks.isEmpty()) {
          ctx.channel.sendMessage(":x: Essa playlist não tem músicas!").queue()
          return
        }

        Database.userDB.updateOneById(
          ctx.author.id,
          Updates.set("playlists.${playlists.indexOf(playlist)}.tracks", playlist.tracks.shuffled())
        )

        ctx.channel.sendMessage("<a:verificado:803678585008816198> Playlist embaralhada com sucesso!").queue()
      }

      in listOf("play", "tocar") -> {
        if (!Utils.canPlay(ctx.selfMember, ctx.member, ctx.channel)) return

        if (playlists == null || playlists.isEmpty()) {
          ctx.channel.sendMessage(":x: Não tens nenhuma playlist!").queue()
          return
        }

        if (ctx.args.size < 2) {
          ctx.channel.sendMessage(":x: **Usa:** ${prefix}playlist play <Nome da playlist>").queue()
          return
        }

        val playlist = playlists.find { it.name == ctx.args[1] }

        if (playlist == null) {
          ctx.channel.sendMessage(":x: Não tens nenhuma playlist com esse nome!").queue()
          return
        }

        if (playlist.tracks == null || playlist.tracks.isEmpty()) {
          ctx.channel.sendMessage(":x: Essa playlist não tem músicas!").queue()
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
          title = "<a:disco:803678643661832233> Playlist Carregada"
          field {
            name = ":page_with_curl: Nome:"
            value = "`${playlist.name}`"
            inline = false
          }
          field {
            name = "<a:infinity:838759634361253929> Quantidade de músicas:"
            value = "`${playlist.tracks.size}`"
            inline = false
          }
          field {
            name = ":watch: Duração:"
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