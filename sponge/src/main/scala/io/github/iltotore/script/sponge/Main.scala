package io.github.iltotore.script.sponge

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.logging.{Handler, LogRecord}

import com.google.inject.Inject
import io.github.iltotore.script.Fun.sideEffect
import io.github.iltotore.script.sponge.command._
import io.github.iltotore.script.{Script, ScriptLoader}
import org.graalvm.polyglot._
import org.simpleyaml.configuration.ConfigurationSection
import org.simpleyaml.configuration.file.YamlConfiguration
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.state.GameStartingServerEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.text.Text

import scala.collection.mutable
import scala.jdk.CollectionConverters.IterableHasAsJava

@Plugin(id = "scripting-mc")
class Main @Inject()(logger: Logger) {

  private implicit val engine: Engine = Engine.create()
  private implicit val requests: mutable.Map[String, CommandRequest] = mutable.Map.empty
  private var fileContext: Context = _
  private var bookContext: Context = _
  private val configFolder = Paths.get(System.getProperty("user.dir"), "mods/plugins/MineScript")
  private var scripts: Iterable[Script] = Nil
  private var languages: Map[Language, String] = Map.empty

  private def extractResource(name: String, overwrite: Boolean = false, copyIfAbsent: Boolean = true): Unit = {
    val path = configFolder.resolve(name)
    Files.createDirectories(path.getParent)
    Sponge.getAssetManager.getAsset(this, name).get()
      .copyToFile(path, overwrite, copyIfAbsent)
  }

  @Listener
  def onStart(event: GameStartingServerEvent): Unit = {

    Files.createDirectories(configFolder)

    extractResource("config.yml")
    extractResource("languages.properties")
    extractResource("script/template.py")
    extractResource("script/sponge/template.py")
    extractResource("script/sponge/hello.py")

    load()
  }

  def load(): Unit = {
    logger.info("Loading config...")
    val configFile = configFolder.resolve("config.yml").toFile
    val config = YamlConfiguration.loadConfiguration(configFile)
    logger.info("Loading scripts...")
    loadScripts(config.getConfigurationSection("permissions"))
    logger.info(s"Loaded ${scripts.size} scripts")
    loadCommands(config)
  }

  def loadScripts(config: ConfigurationSection): Unit = {
    val scriptDir = configFolder.resolve("script").toFile
    val langFile = configFolder.resolve("languages.properties").toFile
    languages = ScriptLoader.extractLanguages(langFile)
    scripts = languages
      .map(sideEffect(entry => logger.info(s"Registering language ${entry._1.getName} with extension ${entry._2}")))
      .map(entry => new ScriptLoader(scriptDir, entry._2, entry._1))
      .flatMap(_.load())

    val langIds = languages.keys.map(_.getId).toSeq
    fileContext = buildFromYaml(config.getConfigurationSection("file"), langIds)
    bookContext = buildFromYaml(config.getConfigurationSection("minecraft"), langIds)
  }

  def loadCommands(config: ConfigurationSection): Unit = {

    val promptedCommands = config.getList("console-prompt")

    val run = CommandSpec.builder()
      .permission("script.run")
      .description(Text.of("Run a loaded script"))
      .executor(new CommandRun(fileContext, promptedCommands.contains("run")))
      .arguments(GenericArguments.choices(
        Text.of("script"),
        () => scripts.map(_.name).asJavaCollection,
        (name: String) => scripts.find(_.name equals name).orNull
      ))
      .build()

    val reload = CommandSpec.builder()
      .permission("script.reload")
      .description(Text.of("Reload scripts"))
      .executor(new CommandReload(this, config.getConfigurationSection("permissions"), promptedCommands.contains("reload")))
      .build()

    val book = CommandSpec.builder()
      .permission("script.book")
      .description(Text.of("Run the script wrote in the held book"))
      .executor(new CommandBook(
        new File(configFolder.toFile, "script"), languages, bookContext, promptedCommands.contains("book")
      ))
      .arguments(
        GenericArguments.choices(
          Text.of("language"),
          () => languages.keys.map(_.getId).asJavaCollection,
          (id: String) => languages.keys.find(_.getId equals id).orNull
        ),
        GenericArguments.optional(GenericArguments.string(Text.of("name")))
      )
      .build()

    val accept = CommandSpec.builder()
      .permission("script.accept")
      .description(Text.of("Accept or deny a command request"))
      .executor(new CommandAccept(requests))
      .arguments(GenericArguments.seq(
        GenericArguments.string(Text.of("name")),
        GenericArguments.bool(Text.of("response"))
      ))
      .build()

    Sponge.getCommandManager.register(
      this,
      CommandSpec.builder()
        .child(run, "run", "execute", "exe")
        .child(reload, "reload", "rl")
        .child(book, "book", "shell")
        .child(accept, "accept", "request", "re")
        .build(),
      "script"
    )
  }

  private def buildFromYaml(config: ConfigurationSection, languages: Seq[String]): Context = {
    val builder = Context.newBuilder(languages: _*)
      .engine(engine)
      .logHandler(new Handler {
        override def publish(record: LogRecord): Unit = logger.info(record.getMessage)

        override def flush(): Unit = {}

        override def close(): Unit = {}
      })

    builder.allowHostAccess(HostAccess.ALL)
    builder.allowPolyglotAccess(PolyglotAccess.ALL)

    assignIfPresent(config, "all")(builder.allowAllAccess)
    assignIfPresent(config, "create-process")(builder.allowCreateProcess)
    assignIfPresent(config, "create-thread")(builder.allowCreateThread)
    assignIfPresent(config, "experimental-options")(builder.allowExperimentalOptions)
    assignIfPresent(config, "class-loading")(builder.allowHostClassLoading)
    assignIfPresent(config, "io")(builder.allowIO)
    assignIfPresent(config, "native")(builder.allowNativeAccess)
    builder.build()
  }

  private def assignIfPresent(config: ConfigurationSection, key: String)(fun: Boolean => Unit): Unit = {
    if (!config.isSet(key)) return
    fun(config.getBoolean(key))
  }
}
