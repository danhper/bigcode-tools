package com.tuvistavie.astgenerator.util
import resource.managed
import java.io._

import scala.reflect.ClassTag

object Serializer {
  def dumpToFile(obj: Any, filepath: String): Unit = {
    for {
      fos <- managed(new FileOutputStream(filepath))
      oos <- managed(new ObjectOutputStream(fos))
    } {
      oos.writeObject(obj)
    }
  }

  private class ScalaObjectInputStream(in: InputStream) extends ObjectInputStream(in) {
    override def resolveClass(desc: java.io.ObjectStreamClass): Class[_] = {
      try {
        Class.forName(desc.getName, false, getClass.getClassLoader)
      } catch {
        case _: ClassNotFoundException => super.resolveClass(desc)
      }
    }
  }

  def loadFromFile[A](filepath: String)(implicit Tag: ClassTag[A]): A = {
    managed(new FileInputStream(filepath))
      .flatMap(fis => managed(new ScalaObjectInputStream(fis)))
      .map(ois => ois.readObject())
      .acquireAndGet {
        case Tag(x) => x
        case _ => throw new RuntimeException(s"could not cast to ${Tag.runtimeClass.getName}")
      }
  }
}
