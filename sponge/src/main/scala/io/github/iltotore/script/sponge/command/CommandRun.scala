package io.github.iltotore.script.sponge.command

import io.github.iltotore.script.Script
import org.graalvm.polyglot.{Context, Engine}
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor

import scala.collection.mutable
import scala.jdk.OptionConverters.RichOptional

class CommandRun(context: Context, prompt: Boolean)(implicit requests: mutable.Map[String, CommandRequest]) extends ScriptCommand(prompt) {
  override def executeSecured(src: CommandSource, args: CommandContext): CommandResult = {
    args.getOne[Script]("script").toScala match {

      case Some(script) =>
        SpongeScript.bindCommand(context, src, args)
        script.execute(context)
        CommandResult.success()

      case None => CommandResult.empty()
    }
  }
}
