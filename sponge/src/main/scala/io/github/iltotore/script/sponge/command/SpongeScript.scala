package io.github.iltotore.script.sponge.command

import org.graalvm.polyglot.Context
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext

object SpongeScript {

  def bindCommand(context: Context, src: CommandSource, args: CommandContext): Unit = {
    context.getPolyglotBindings.putMember("src", src)
    context.getPolyglotBindings.putMember("args", args)
  }
}
