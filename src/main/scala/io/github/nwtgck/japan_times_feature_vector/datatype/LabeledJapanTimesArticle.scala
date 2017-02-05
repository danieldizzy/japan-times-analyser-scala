package io.github.nwtgck.japan_times_feature_vector.datatype

/**
  * An article of Japan Times
  */
case class LabeledJapanTimesArticle[L](label: L, url: String, title: String, entity: String) extends Document
