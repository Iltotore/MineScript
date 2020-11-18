package io.github.iltotore.script

import java.io.File

import org.graalvm.polyglot.{Engine, Language, Source}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.{Source => IOSource}
import scala.util.control.Breaks.{break, breakable}

class ScriptLoader(root: File, ext: String, language: Language) {

  def load(directory: File = root, parents: Seq[String] = Seq.empty): ArrayBuffer[Script] = {
    val scripts: ArrayBuffer[Script] = ArrayBuffer.empty
    val tempFile = new File(directory, s"template.$ext")

    val templates = parents :+ (if (tempFile.exists()) {
      val src = IOSource.fromFile(tempFile)
      try src.mkString finally src.close()
    } else "")

    for (file <- directory.listFiles()) breakable {
      if (file.isDirectory) {
        scripts ++= load(file, templates)
        break()
      }
      if (!file.getName.endsWith(s".$ext")) break()
      val name = root.toPath.relativize(file.toPath).toString
      var code = if (file.getName.endsWith(s"template.$ext")) {
        parents.mkString("", "\n", "\n")
      } else templates.mkString("", "\n", "\n")
      code += {
        val src = IOSource.fromFile(file)
        try src.mkString finally src.close()
      }
      scripts += Script(
        name = name,
        source = Source.newBuilder(language.getId, code, null)
          .cached(true)
          .build()
      )
    }
    scripts
  }
}

object ScriptLoader {

  def extractLanguages(file: File)(implicit engine: Engine): Map[Language, String] = {
    val langProp = IOSource.fromFile(file)
    val languages = try langProp.mkString finally langProp.close()
    languages
      .split("\\n")
      .filterNot(_.startsWith("#"))
      .map(_.split("="))
      .filter(tpl => engine.getLanguages.containsKey(tpl(0)))
      .map(array => (engine.getLanguages.get(array(0)), array(1).trim.stripLineEnd))
      .toMap
  }


  def templatesFor(root: File, file: File, ext: String, stack: mutable.ArrayBuffer[String] = mutable.ArrayBuffer.empty): mutable.ArrayBuffer[String] = {
    if(file.isFile) return templatesFor(root, file.getParentFile, ext, stack)
    val template = new File(file, s"template.$ext")
    if(template.exists()) {
      val templateSource = IOSource.fromFile(template)
      stack prepend (try templateSource.mkString finally templateSource.close())
    }
    if(file equals root) stack else templatesFor(root, file.getParentFile, ext, stack)
  }
}