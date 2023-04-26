package com.github.davidffa.d4rkbotkt.events.listeners

import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.coroutines.await
import com.github.davidffa.d4rkbotkt.D4rkBot
import com.github.davidffa.d4rkbotkt.database.GuildCache
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import java.time.Instant

suspend fun onGuildJoin(event: GuildJoinEvent) {
  D4rkBot.guildCache[event.guild.idLong] = GuildCache("dk.")

  val embed = Embed {
    title = "<:badgebooster:803666384373809233> Entrei num novo servidor"
    color = Utils.randColor()
    field {
      name = "Nome"
      value = "`${event.guild.name}`"
      inline = false
    }
    field {
      name = ":crown: Dono"
      value = "`${event.guild.owner?.user?.asTag}`"
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