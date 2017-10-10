package com.tuvistavie.astgenerator.util

import java.util.Optional

object JavaConversions {
  implicit class RichOptional[T](val optional: Optional[T]) extends AnyVal {
    def toOption: Option[T] = if (optional.isPresent) Some(optional.get()) else None
  }
}
