package io.github.iltotore.script.sponge.command

import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.command.args.CommandContext

case class CommandRequest(src: CommandSource, context: CommandContext, executor: ScriptCommand){

  def accept(): CommandResult = executor.executeSecured(src, context)
}
