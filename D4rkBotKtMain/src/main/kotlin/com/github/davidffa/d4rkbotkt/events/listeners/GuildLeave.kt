package com.github.davidffa.d4rkbotkt.events.listeners

import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.coroutines.await
import com.github.davidffa.d4rkbotkt.D4rkBot
import com.github.davidffa.d4rkbotkt.Database
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import java.time.Instant

suspend fun onGuildLeave(event: GuildLeaveEvent) {
  D4rkBot.guildCache.remove(event.guild.idLong)
  Database.guildDB.deleteOneById(event.guild.id)

  val embed = Embed {
    title = ":frowning2: Saí de um servidor"
    color = Utils.randColor()
    field {
      name = "Nome"
      value = "`${event.guild.name}`"
      inline = false
    }
    field {
      name = ":crown: Dono"
      value = "`${event.guild.owner?.user?.name}`"
      inline = false
    }
    field {
      name = ":closed_book: ID"
      value = "`${event.guild.id}`"
      inline = false
    }
    field {
      name = ":man: Membros"
      value = "`${event.guild.memberCount}`"
      inline = false
    }
    if (event.guild.iconUrl != null) thumbnail = event.guild.iconUrl
    timestamp = Instant.now()
  }

  val owner = event.jda.userCache.getElementById(334054158879686657) ?: return
  val channel = owner.openPrivateChannel().await() ?: return

  channel.sendMessageEmbeds(embed).queue()
}