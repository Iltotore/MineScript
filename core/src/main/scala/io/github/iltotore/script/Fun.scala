package io.github.iltotore.script

object Fun {

  def sideEffect[T](f: T => Unit): T => T = value => {
    f(value)
    value
  }

  def filterOr[T](cond: T => Boolean)(orElse: T => Unit): T => Option[T] = value => if (cond(value)) {
    Some(value)
  } else {
    orElse(value)
    None
  }
}
