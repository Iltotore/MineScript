package io.github.iltotore.script

import org.graalvm.polyglot.{Context, Source, Value}

case class Script(name: String, source: Source){

  def execute(context: Context): Unit = context.eval(source)
}