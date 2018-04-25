package io.github.nwtgck.japan_times_feature_analyser.datatype

/**
  * Data structure for Multi Classification
  *
  * @param trainTestDocs
  */
case class LabeledTrainTestMultiDataset[L](trainTestDocs: Seq[LabeledTrainTestDocument[L]], classNum: Int)
