package me.davidffa.d4rkbotkt.command

import dev.minn.jda.ktx.await
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.commands.*
import me.davidffa.d4rkbotkt.commands.dev.*
import me.davidffa.d4rkbotkt.commands.info.*
import me.davidffa.d4rkbotkt.commands.music.*
import me.davidffa.d4rkbotkt.commands.music.Queue
import me.davidffa.d4rkbotkt.commands.others.*
import me.davidffa.d4rkbotkt.commands.settings.*
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.timerTask

object CommandManager {
    val commands = ArrayList<Command>()
    private val cmdList: ArrayList<String> = ArrayList()
    private val cooldowns = HashMap<String, HashMap<String, Long>>()

    init {
        /* ** DEV ** */
        this.addCommand(Eval())

        /* ** INFO ** */
        this.addCommand(Avatar())
        this.addCommand(Botinfo())
        this.addCommand(Ping())
        this.addCommand(Uptime())

        /* ** MUSIC ** */
        this.addCommand(Loop())
        this.addCommand(Nowplaying())
        this.addCommand(Pause())
        this.addCommand(Play())
        //this.addCommand(Playlist())
        this.addCommand(Queue())
        this.addCommand(Resume())
        this.addCommand(Shuffle())
        this.addCommand(Skip())
        this.addCommand(Stop())
        this.addCommand(Volume())

        /* ** OTHERS ** */
        this.addCommand(Help())
        this.addCommand(Render())

        /* ** SETTINGS ** */
        this.addCommand(Setprefix())

        for (command in this.commands) {
            cmdList.add(command.name)

            if (command.aliases != null) {
                for (alias in command.aliases) {
                    cmdList.add(alias)
                }
            }
        }
    }

    private fun addCommand(cmd: Command) {
        val nameFound = this.commands.stream().anyMatch { it.name.equals(cmd.name, true) }

        if (nameFound) {
            throw IllegalArgumentException("Command already loaded!")
        }

        commands.add(cmd)
    }

    fun getCommand(search: String): Command? {
        val searchLower = search.lowercase()

        for (cmd in this.commands) {
            if (cmd.name == searchLower || (cmd.aliases != null && cmd.aliases.contains(searchLower))) {
                return cmd
            }
        }
        return null
    }

    suspend fun handle(event: GuildMessageReceivedEvent) {
        val prefix = D4rkBot.guildCache[event.guild.idLong]!!.prefix
        val split = event.message.contentRaw
            .replaceFirst(prefix, "")
            .split("\\s+".toRegex())

        val invoke = split[0].lowercase()
        val cmd = this.getCommand(invoke)

        if (cmd != null) {
            if (cmd.botPermissions != null) {
                val botMissingPermissions = cmd.botPermissions.filter { !event.guild.selfMember.getPermissions(event.channel).contains(it) }

                if (botMissingPermissions.isNotEmpty()) {
                    if (botMissingPermissions.contains(Permission.MESSAGE_WRITE)) return

                    if (botMissingPermissions.size > 1) {
                        event.channel.sendMessage(
                            ":x: Preciso das permissões `${
                                Utils.translatePermissions(
                                    botMissingPermissions
                                ).joinToString(", ")
                            }` para executar esse comando!"
                        ).queue()
                    } else {
                        event.channel.sendMessage(
                            ":x: Preciso da permissão `${
                                Utils.translatePermission(
                                    botMissingPermissions[0]
                                )
                            }` para executar esse comando!"
                        ).queue()
                    }
                    return
                }
            }

            val member = event.member

            if (member != null && member.id != "334054158879686657") {
                if (cmd.userPermissions != null) {
                    val userMissingPermissions =
                        cmd.userPermissions.filter { !member.getPermissions(event.channel).contains(it) }

                    if (userMissingPermissions.isNotEmpty()) {
                        if (userMissingPermissions.size > 1) {
                            event.channel.sendMessage(
                                ":x: Precisas das permissões `${
                                    Utils.translatePermissions(
                                        userMissingPermissions
                                    ).joinToString(", ")
                                }` para executar esse comando!"
                            ).queue()
                        } else {
                            event.channel.sendMessage(
                                ":x: Precisas da permissão `${
                                    Utils.translatePermission(
                                        userMissingPermissions[0]
                                    )
                                }` para executar esse comando!"
                            ).queue()
                        }
                        return
                    }
                }
            }

            if (!this.cooldowns.containsKey(cmd.name)) {
                this.cooldowns[cmd.name] = HashMap()
            }

            val now = Instant.now().toEpochMilli()
            val timestamps = this.cooldowns[cmd.name]
            val cooldownAmount = cmd.cooldown * 1e3

            if (timestamps != null) {
                val expirationTime = timestamps[event.author.id]?.plus(cooldownAmount)

                if (expirationTime != null && now < expirationTime) {
                    val timeLeft = (expirationTime - now) / 1e3
                    event.channel.sendMessage(":clock1: Espera mais `${"%.1f".format(timeLeft)}` segundos para voltares a usar o comando `${cmd.name}`")
                        .queue()
                    return
                }
            }

            val args = split.subList(1, split.size)

            if (cmd.args > args.size) {
                event.channel.sendMessage(":x: Argumentos em falta! **Usa:** `$prefix${cmd.name} ${cmd.usage}`").queue()
                return
            }

            if (timestamps != null && event.author.id != "334054158879686657") {
                timestamps[event.author.id] = now

                Timer().schedule(timerTask {
                    timestamps.remove(event.author.id)
                }, cooldownAmount.toLong())
            }

            D4rkBot.commandsUsed++
            val ctx = CommandContext(event, args, prefix)
            cmd.run(ctx)
        }else {
            lateinit var suggest: String
            var minDistance = Int.MAX_VALUE

            for (command in this.cmdList) {
                val distance = Utils.levenshteinDistance(invoke, command)
                if (distance < minDistance) {
                    minDistance = distance
                    suggest = command
                }
            }

            val msg = event.channel.sendMessage(":x: Eu não tenho esse comando.\n:thinking: Querias dizer `$prefix$suggest`?").await()

            Timer().schedule(timerTask {
                msg.delete().queue()
            }, 7000)
        }
    }
}