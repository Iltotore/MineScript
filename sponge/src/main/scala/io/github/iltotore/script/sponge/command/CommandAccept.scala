package io.github.iltotore.script.sponge.command

import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.source.ConsoleSource
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors

import scala.collection.mutable
import scala.jdk.OptionConverters.RichOptional

class CommandAccept(requests: mutable.Map[String, CommandRequest]) extends CommandExecutor {
  override def execute(src: CommandSource, args: CommandContext): CommandResult = src match {

    case _: ConsoleSource =>
      val request: CommandRequest = args.getOne("name")
        .toScala
        .map(requests.apply)
        .get

      requests.remove(args.getOne("name").get())
      val accept: Boolean = args.getOne("response").get()

      if(accept) request.accept() else CommandResult.success()

    case _ => src.sendMessage(Text.builder("Only usable by the console source.").color(TextColors.RED).build())
      CommandResult.empty()
  }
}
