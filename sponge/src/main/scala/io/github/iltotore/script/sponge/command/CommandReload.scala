package io.github.iltotore.script.sponge.command

import io.github.iltotore.script.sponge.Main
import org.simpleyaml.configuration.ConfigurationSection
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors

import scala.collection.mutable

class CommandReload(plugin: Main, section: ConfigurationSection, prompt: Boolean)(implicit requests: mutable.Map[String, CommandRequest]) extends ScriptCommand(prompt) {
  override def executeSecured(src: CommandSource, args: CommandContext): CommandResult = {
    src.sendMessage(Text.builder("Reloading scripts...").color(TextColors.GREEN).build())
    plugin.loadScripts(section)
    src.sendMessage(Text.builder("Done !").color(TextColors.GREEN).build())
    CommandResult.success()
  }
}
