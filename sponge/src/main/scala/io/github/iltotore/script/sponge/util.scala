package io.github.iltotore.script.sponge

object util {

  implicit class OptionInner[A](base: Option[Iterable[A]]) {

    def mapInner[B](mapper: A => B): Option[Iterable[B]] = base.map(seq => {
      seq.map(mapper)
    })
  }

  implicit class OptionInnerJava[A](base: Option[java.lang.Iterable[A]]) {

    def mapInnerJava[B](mapper: A => B): Option[java.lang.Iterable[B]] = base.map(seq => {
      val list = new java.util.ArrayList[B]()
      seq.forEach(element => list.add(mapper(element)))
      list
    })
  }
}
