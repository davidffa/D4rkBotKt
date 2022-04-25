package me.davidffa.d4rkbotkt.commands.others

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.davidffa.d4rkbotkt.Credentials
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.time.Instant
import java.util.zip.Inflater

class Render : Command(
  "render",
  listOf("renderizar", "webrender"),
  "Others",
  botPermissions = listOf(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES),
  args = 1,
  cooldown = 5
) {
  override suspend fun run(ctx: CommandContext) {
    if (((ctx.channel !is TextChannel) || !(ctx.channel as TextChannel).isNSFW) && ctx.author.id != "334054158879686657") {
      ctx.channel.sendMessage(ctx.t("commands.render.nsfwOnly")).queue()
      return
    }

    val url = if (ctx.args[0].startsWith("http")) ctx.args[0] else "http://${ctx.args[0]}"

    val msg = ctx.channel.sendMessage(ctx.t("commands.render.checkUrl")).await()

    val request = Request.Builder()
      .url(url)
      .build()

    try {
      D4rkBot.okHttpClient.newCall(request).await().close()
    } catch (e: Exception) {
      msg.editMessage(ctx.t("commands.render.offline")).queue()
      return
    }

    msg.editMessage(ctx.t("commands.render.rendering")).queue()

    val renderURL =
      withContext(Dispatchers.IO) { "${Credentials.RENDERAPIURL}?url=${URLEncoder.encode(url, "utf-8")}" }

    val renderReq = Request.Builder()
      .url(renderURL)
      .addHeader("Authorization", Credentials.RENDERAPITOKEN)
      .build()

    val renderRes = D4rkBot.okHttpClient.newCall(renderReq).await().body ?: return

    val compressedBuffer = withContext(Dispatchers.IO) { renderRes.bytes() }

    renderRes.close()

    // Decompress image buffer
    val inflater = Inflater()
    inflater.setInput(compressedBuffer)

    val outputStream = ByteArrayOutputStream(compressedBuffer.size)
    val buffer = ByteArray(1024)

    while (!inflater.finished()) {
      val count = inflater.inflate(buffer)
      withContext(Dispatchers.IO) { outputStream.write(buffer, 0, count) }
    }

    withContext(Dispatchers.IO) { outputStream.close() }
    val output = outputStream.toByteArray()

    msg.delete().queue()

    val embed = Embed {
      title = "Render"
      color = Utils.randColor()
      image = "attachment://render.png"
      footer {
        name = ctx.author.asTag
        iconUrl = ctx.author.effectiveAvatarUrl
      }
      timestamp = Instant.now()
    }

    ctx.channel.sendMessageEmbeds(embed).addFile(output, "render.png").queue()
  }
}