package io.github.nwtgck.japan_times_feature_analyser.datatype

/**
  * An article of Japan Times
  */
case class JapanTimesArticle(url: String, title: String, entity: String) extends Document
