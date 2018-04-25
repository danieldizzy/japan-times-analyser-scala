package io.github.nwtgck.japan_times_feature_analyser.datatype

/**
  * Document which has two different aspects between train and test
  *
  * @param trainDocument
  * @param testDocument
  */
case class LabeledTrainTestDocument[L](label: L, trainDocument: EngDocument, testDocument: EngDocument)
