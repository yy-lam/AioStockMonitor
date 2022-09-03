package app

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations

import scala.collection.JavaConverters.asScalaBufferConverter

class SentimentAnalyzer(pipeline: StanfordCoreNLP) {

  import Sentiment._

  def extractSentiments(text: String): List[Sentiment] = {
    val annotation: Annotation = pipeline.process(text)
    val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation]).asScala
    sentences
      .map(sentence => (sentence, sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])))
      .map { case (_, tree) => Sentiment.toSentiment(RNNCoreAnnotations.getPredictedClass(tree)) }
      .toList
  }
}
