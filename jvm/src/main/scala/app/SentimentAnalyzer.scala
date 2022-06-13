package app

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations

import scala.collection.JavaConverters.asScalaBufferConverter

class SentimentAnalyzer(pipeline: StanfordCoreNLP) {

  import Sentiment._

  //  def mainSentiment(input: String): Sentiment = Option(input) match {
  //    case Some(text) if text.nonEmpty => extractSentiment(text)
  //    case _ => throw new IllegalArgumentException("input can't be null or empty")
  //  }
  //
  //  private def extractSentiment(text: String): Sentiment = {
  //    val (_, sentiment) = extractSentiments(text)
  //      .maxBy { case (sentence, _) => sentence.length }
  //    sentiment
  //  }
  //
  //  def extractSentiments(text: String): List[(String, Sentiment)] = {
  //    val annotation: Annotation = pipeline.process(text)
  //    val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])
  //    sentences
  //      .map(sentence => (sentence, sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])))
  //      .map { case (sentence, tree) => (sentence.toString,Sentiment.toSentiment(RNNCoreAnnotations.getPredictedClass(tree))) }
  //      .toList
  //  }
  def extractSentiments(text: String): List[Sentiment] = {
    val annotation: Annotation = pipeline.process(text)
    val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation]).asScala
    sentences
      .map(sentence => (sentence, sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])))
      .map { case (_, tree) => Sentiment.toSentiment(RNNCoreAnnotations.getPredictedClass(tree)) }
      .toList
  }
}
