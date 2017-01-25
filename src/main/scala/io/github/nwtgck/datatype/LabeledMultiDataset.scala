package io.github.nwtgck.datatype

/**
  * Data structure for Multi Classification
  *
  * @param docs
  */
case class LabeledMultiDataset[L](docs: Seq[LabeledJapanTimesArticle[L]])
