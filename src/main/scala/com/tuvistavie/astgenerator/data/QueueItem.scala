package com.tuvistavie.astgenerator.data

trait QueueItem[+T]
case class Item[T](content: T) extends QueueItem[T]
case object Stop extends QueueItem[Nothing]
