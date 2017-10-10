package com.tuvistavie.bigcode.asttools.data

trait QueueItem[+T]
case class Item[T](index: Int, content: T) extends QueueItem[T]
case object Stop extends QueueItem[Nothing]
