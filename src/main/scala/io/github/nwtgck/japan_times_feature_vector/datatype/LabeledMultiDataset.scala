package io.github.nwtgck.japan_times_feature_vector.datatype

/**
  * Data structure for Multi Classification
  *
  * @param docs
  */
case class LabeledMultiDataset[L](docs: Seq[LabeledDocument[L]], classNum: Int)
