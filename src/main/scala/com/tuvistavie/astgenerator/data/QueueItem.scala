package com.tuvistavie.astgenerator.data

trait QueueItem[+T]
case class Item[T](index: Int, content: T) extends QueueItem[T]
case object Stop extends QueueItem[Nothing]
