package com.tuvistavie.astgenerator.util
import resource.managed
import java.io._

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

  def loadFromFile[A](filepath: String): A = {
    managed(new FileInputStream(filepath))
      .flatMap(fis => managed(new ScalaObjectInputStream(fis)))
      .map(ois => ois.readObject().asInstanceOf[A])
      .acquireAndGet(identity)
  }
}
