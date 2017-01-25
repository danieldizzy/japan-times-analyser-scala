package io.github.nwtgck.datatype

/**
  * An article of Japan Times
  */
case class LabeledJapanTimesArticle[L](label: L, url: String, title: String, entity: String) extends Document
