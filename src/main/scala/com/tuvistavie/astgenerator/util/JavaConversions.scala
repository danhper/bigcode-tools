package com.tuvistavie.astgenerator.util

import java.util.Optional

object JavaConversions {
  implicit def toRichOptional[T](optional: Optional[T]): RichOptional[T] = new RichOptional[T](optional)
}

class RichOptional[T](private val optional: Optional[T]) {
  def toOption: Option[T] = if (optional.isPresent) Some(optional.get()) else None
}
