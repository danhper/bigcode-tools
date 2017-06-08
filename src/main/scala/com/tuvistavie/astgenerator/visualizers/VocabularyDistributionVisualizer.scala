package com.tuvistavie.astgenerator.visualizers

import com.tuvistavie.astgenerator.ast.VocabularyGenerator
import com.tuvistavie.astgenerator.models.{VisualizeVocabularyDistributionConfig, Vocabulary}
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
    val uniqueLettersCount = vocabulary.size
    val lettersCount = vocabulary.items.values.map(_.count).sum
    f"${baseTitle }<br>$uniqueLettersCount unique letters<br>$lettersCount letters in total"
  }
}

object VocabularyDistributionVisualizer {
  def visualizeVocabularyDistribution(config: VisualizeVocabularyDistributionConfig): Unit = {
    val vocabulary = VocabularyGenerator.loadFromFile(config.vocabularyPath)
    val visualizer = new VocabularyDistributionVisualizer(vocabulary)
    val plot = visualizer.visualize(config.breakpoints)
    if (config.replace) {
      File(Path(config.output)).delete()
    }
    val layout = Layout(title=visualizer.makeTitle(config.title))
    Plotly.plot(
      config.output,
      Seq(plot),
      layout,
      addSuffixIfExists = !config.replace,
      openInBrowser = config.openBrowser
    )
  }
}
