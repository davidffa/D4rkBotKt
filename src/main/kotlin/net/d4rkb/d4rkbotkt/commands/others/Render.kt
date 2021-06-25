package net.d4rkb.d4rkbotkt.commands.others

import com.fasterxml.jackson.databind.ObjectMapper
import net.d4rkb.d4rkbotkt.command.Command
import net.d4rkb.d4rkbotkt.command.CommandContext
import net.d4rkb.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URISyntaxException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
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
    override fun run(ctx: CommandContext) {
        if (!ctx.channel.isNSFW && ctx.author.id != "334054158879686657") {
            ctx.channel.sendMessage(":x: Só podes usar este comando em um canal NSFW.").queue()
            return
        }

        val url: URI

        try {
            url = URI(if (ctx.args[0].startsWith("http")) ctx.args[0] else "http://${ctx.args[0]}")
        }catch (e: URISyntaxException) {
            ctx.channel.sendMessage(":x: URL inválido!").queue()
            return
        }

        val msg = ctx.channel.sendMessage("<a:loading2:805088089319407667> A verificar se o URL é válido...").complete()

        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder(url).GET().timeout(Duration.ofMillis(5000)).build()

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        }catch (e: Exception) {
            msg.editMessage(":x: Site offline!").queue()
            return
        }

        msg.editMessage("<a:loading2:805088089319407667> A renderizar a página...").queue()

        val map = HashMap<String, String>()
        map["url"] = url.toString()
        val body = ObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(map)

        val renderReq = HttpRequest.newBuilder(URI(System.getenv("RENDERAPIURL")))
            .header("Content-Type", "application/json")
            .header("Authorization", System.getenv("RENDERAPITOKEN"))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val res = client.send(renderReq, HttpResponse.BodyHandlers.ofByteArray())

        // Decompress image buffer
        val inflater = Inflater()
        inflater.setInput(res.body())

        val outputStream = ByteArrayOutputStream(res.body().size)
        val buffer = ByteArray(1024)

        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }

        outputStream.close()
        val output = outputStream.toByteArray()

        msg.delete().queue()

        val embed = EmbedBuilder()
            .setTitle("Render", url.toString())
            .setImage("attachment://render.png")
            .setColor(Utils.randColor())
            .setFooter(ctx.author.asTag, ctx.author.effectiveAvatarUrl)
            .setTimestamp(Instant.now())
            .build()

        ctx.channel.sendMessageEmbeds(embed).addFile(output, "render.png").queue()
    }
}