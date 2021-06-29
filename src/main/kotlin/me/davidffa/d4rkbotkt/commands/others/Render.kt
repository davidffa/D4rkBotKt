package me.davidffa.d4rkbotkt.commands.others

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import ru.gildor.coroutines.okhttp.await
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.zip.Inflater

class Render : Command(
    "render",
    "Renderiza uma página web.",
    listOf("renderizar", "webrender"),
    "<URL>",
    "Others",
    botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES),
    args = 1,
    cooldown = 5
) {
    override suspend fun run(ctx: CommandContext) {
        if (!ctx.channel.isNSFW && ctx.author.id != "334054158879686657") {
            ctx.channel.sendMessage(":x: Só podes usar este comando em um canal NSFW.").queue()
            return
        }

        val url = if (ctx.args[0].startsWith("http")) ctx.args[0] else "http://${ctx.args[0]}"

        val msg = ctx.channel.sendMessage("<a:loading2:805088089319407667> A verificar se o URL é válido...").await()

        val request = Request.Builder()
            .url(url)
            .build()

        try {
            D4rkBot.okHttpClient.newCall(request).await().close()
        }catch (e: Exception) {
            msg.editMessage(":x: Site offline!").queue()
            return
        }

        msg.editMessage("<a:loading2:805088089319407667> A renderizar a página...").queue()

        val json = "{\"url\":\"${url}\"}"
        val body = RequestBody.create(MediaType.parse("application/json"), json)

        val renderReq = Request.Builder()
            .url(System.getenv("RENDERAPIURL"))
            .addHeader("Authorization", System.getenv("RENDERAPITOKEN"))
            .post(body)
            .build()

        val renderRes = D4rkBot.okHttpClient.newCall(renderReq).await().body() ?: return

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