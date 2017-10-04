package com.tuvistavie.astgenerator.util
import resource.managed
import java.io._

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

import scala.reflect.ClassTag

object Serializer {
  def dumpINDArrayToFile(obj: INDArray, filepath: String): Unit = {
    managed(new FileOutputStream(filepath)).foreach(Nd4j.write(_, obj))
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

  def loadINDArrayFromFile(filepath: String): INDArray = {
    managed(new FileInputStream(filepath)).acquireAndGet(Nd4j.read)
  }
}
