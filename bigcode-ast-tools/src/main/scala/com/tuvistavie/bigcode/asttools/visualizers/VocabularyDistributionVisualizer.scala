package com.tuvistavie.bigcode.asttools.visualizers

import com.tuvistavie.bigcode.asttools.models.{VisualizeVocabularyDistributionConfig, Vocabulary}
import com.tuvistavie.bigcode.asttools.ast.VocabularyGenerator
import plotly.layout.Layout
import plotly.{Bar, Plotly}

import scala.reflect.io.{File, Path}

class VocabularyDistributionVisualizer(vocabulary: Vocabulary) {
  def visualize(breakpoints: Seq[Int]): Bar = {
    val textRanges = breakpoints.zip(breakpoints.tail).map { case (l, u) => f"$l-$u" }
    val text = f"-${breakpoints.head}" +: textRanges :+ f"${breakpoints.last}-"
    val breakpointsWithMax = breakpoints :+ Int.MaxValue
    val buckets = Array.fill(breakpointsWithMax.size)(0)
    vocabulary.items.values.foreach(item => {
      val bucket = breakpointsWithMax.zipWithIndex.dropWhile { case (v, _) => item.count > v }.head._2
      buckets(bucket) += 1
    })
    Bar(
      text,
      buckets.toSeq
    )
  }

  def makeTitle(baseTitle: String): String = {
    f"${baseTitle }<br>${vocabulary.size} unique letters<br>${vocabulary.totalLettersCount} letters in total"
  }
}

object VocabularyDistributionVisualizer {
  def visualizeVocabularyDistribution(config: VisualizeVocabularyDistributionConfig): Unit = {
    val vocabulary = VocabularyGenerator.loadFromFile(config.vocabularyPath)
    val visualizer = new VocabularyDistributionVisualizer(vocabulary)
    val plot = visualizer.visualize(config.breakpoints)
    if (config.replace) {
      File(Path(config.fileOutput)).delete()
    }
    val layout = Layout(title=visualizer.makeTitle(config.title))
    Plotly.plot(
      config.fileOutput,
      Seq(plot),
      layout,
      addSuffixIfExists = !config.replace,
      openInBrowser = config.openBrowser
    )
  }
}
