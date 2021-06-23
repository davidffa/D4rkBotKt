package net.d4rkb.d4rkbotkt.command

import net.d4rkb.d4rkbotkt.commands.*
import net.d4rkb.d4rkbotkt.commands.dev.*
import net.d4rkb.d4rkbotkt.commands.info.*
import net.d4rkb.d4rkbotkt.commands.music.*
import net.d4rkb.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.util.*
import javax.annotation.Nullable
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.timerTask

class CommandManager {
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
        this.addCommand(Play())
        this.addCommand(Stop())
        this.addCommand(Skip())

        /* ** OTHERS ** */
        this.addCommand(Help(this))

        for (command in this.commands) {
            cmdList.add(command.name)

            for (alias in command.aliases) {
                cmdList.add(alias)
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

    @Nullable
    fun getCommand(search: String): Command? {
        val searchLower = search.lowercase()

        for (cmd in this.commands) {
            if (cmd.name == searchLower || cmd.aliases.contains(searchLower)) {
                return cmd
            }
        }
        return null
    }

    fun handle(event: GuildMessageReceivedEvent) {
        val split = event.message.contentRaw
            .replaceFirst("dk.", "")
            .split("\\s+".toRegex())

        val invoke = split[0].lowercase()
        val cmd = this.getCommand(invoke)

        if (cmd != null) {
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

            val member = event.member

            if (member != null) {
                val userMissingPermissions =
                    cmd.userPermissions.filter { member.getPermissions(event.channel).contains(it) }

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

            if (!this.cooldowns.containsKey(cmd.name)) {
                this.cooldowns[cmd.name] = HashMap()
            }

            val now = Date().time
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
                event.channel.sendMessage(":x: Argumentos em falta! **Usa:** `dk.${cmd.name} ${cmd.usage}`").queue()
                return
            }

            if (timestamps != null) {
                timestamps[event.author.id] = now

                Timer().schedule(timerTask {
                    timestamps.remove(event.author.id)
                }, cooldownAmount.toLong())
            }

            val ctx = CommandContext(event, args)
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

            val msg = event.channel.sendMessage(":x: Eu não tenho esse comando.\n:thinking: Querias dizer `dk.$suggest`?").complete()

            Timer().schedule(timerTask {
                msg.delete().queue()
            }, 7000)
        }
    }
}