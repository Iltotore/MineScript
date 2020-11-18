package io.github.iltotore.script.sponge.command

import java.io.File

import io.github.iltotore.script.ScriptLoader
import io.github.iltotore.script.sponge.util._
import org.graalvm.polyglot.{Context, Language, Source}
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.data.`type`.HandTypes
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors

import scala.collection.mutable
import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.jdk.OptionConverters.RichOptional

class CommandBook(root: File, extensions: Map[Language, String], context: Context, prompt: Boolean)(implicit requests: mutable.Map[String, CommandRequest]) extends ScriptCommand(prompt) {

  private val cache: mutable.Map[String, Source] = mutable.Map.empty

  override def executeSecured(src: CommandSource, args: CommandContext): CommandResult = src match {

    case player: Player =>
      player.sendMessage(Text.builder("Running script...").color(TextColors.GREEN).build())
      val language: Language = args.getOne("language").get()
      val text = player.getItemInHand(HandTypes.MAIN_HAND)
        .toScala
        .flatMap(
          item => item.get(Keys.PLAIN_BOOK_PAGES)
            .toScala
            .orElse(
              item.get(Keys.BOOK_PAGES).toScala.mapInnerJava(_.toPlain)
            ))
        .map(_.asScala)
        .toSeq
        .flatten
      val name: Option[String] = args.getOne("name").toScala
      val source = name
        .flatMap(cache.get)
        .getOrElse(loadCode(name.getOrElse(""), language.getId, extensions(language), text))
      if(text.isEmpty) return CommandResult.empty()
      SpongeScript.bindCommand(context, src, args)
      context.eval(source)
      CommandResult.success()

    case _ =>
      src.sendMessage(Text.of("Only usable by Players holding a writtable book"))
      CommandResult.empty()
  }

  private def loadCode(name: String, lang: String, ext: String, text: Seq[String]): Source = {
    val templates = ScriptLoader.templatesFor(root, new File(root, name), ext) :+ text.mkString("", "\n", "")
    Source.create(lang, templates.mkString("\n"))
  }
}
