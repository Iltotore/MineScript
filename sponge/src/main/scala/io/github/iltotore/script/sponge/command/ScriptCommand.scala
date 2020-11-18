package io.github.iltotore.script.sponge.command

import java.util.UUID

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.source.ConsoleSource
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors

import scala.collection.mutable

abstract class ScriptCommand(prompt: Boolean)(implicit requests: mutable.Map[String, CommandRequest]) extends CommandExecutor {
  override def execute(src: CommandSource, args: CommandContext): CommandResult = src match {

    case _: ConsoleSource =>
      executeSecured(src, args)

    case _ =>
      src.sendMessage(Text.builder("Command request sent to the console.").color(TextColors.GREEN).build())
      Sponge.getServer.getConsole.sendMessage(
        Text.builder(
          s"""
             |${src.getName} tried to execute a prompt-required command.
             |Type /script accept ${src.getName} <true/false>""".stripMargin)
          .color(TextColors.YELLOW)
          .build()
      )
      requests.put(src.getName, CommandRequest(src, args, this))
      CommandResult.empty()
  }

  def executeSecured(src: CommandSource, args: CommandContext): CommandResult
}