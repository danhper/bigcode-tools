package com.tuvistavie.astgenerator.visualizers


import com.tuvistavie.astgenerator.ast.VocabularyGenerator
import com.tuvistavie.astgenerator.models.{VisualizeEmbeddingsConfig, Vocabulary}
import com.tuvistavie.astgenerator.util.Serializer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dimensionalityreduction.PCA
import plotly.element.ScatterMode
import plotly.layout.Layout
import plotly.{Plotly, Scatter}

import scala.reflect.io.{File, Path}


class EmbeddingVisualizer(vocabulary: Vocabulary, embeddings: INDArray) {
  def visualize(dimensions: Int, normalize: Boolean = true, name: String = "Embeddings"): Scatter = {
    val lowDimensionalEmbeddings = PCA.pca(embeddings, dimensions, normalize)
    val xColumn = lowDimensionalEmbeddings.getColumn(0)
    val yColumn = lowDimensionalEmbeddings.getColumn(1)
    val x = (0 until xColumn.rows()).map(i => xColumn.getFloat(i))
    val y = (0 until yColumn.rows()).map(i => yColumn.getFloat(i))
    val text = (0 until xColumn.rows()).map(i => vocabulary.items(i).subgraph.toString)
    Scatter(
      x.seq,
      y.seq,
      name = name,
      text = text,
      mode = ScatterMode(ScatterMode.Markers)
    )
  }
}

object EmbeddingVisualizer {
  def visualizeEmbeddings(config: VisualizeEmbeddingsConfig): Unit = {
    val embeddings = Serializer.loadINDArrayFromFile(config.embeddingsPath)
    val vocabulary = VocabularyGenerator.loadFromFile(config.vocabularyPath)
    val visualizer = new EmbeddingVisualizer(vocabulary, embeddings)
    val plot = visualizer.visualize(config.dimensions)
    val layout = Layout(title=config.title)
    if (config.replace) {
      File(Path(config.output)).delete()
    }
    Plotly.plot(
      config.output,
      Seq(plot),
      layout,
      addSuffixIfExists = !config.replace,
      openInBrowser = config.openBrowser
    )
  }
}
