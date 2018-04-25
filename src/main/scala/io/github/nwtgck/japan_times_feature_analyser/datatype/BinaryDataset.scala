package io.github.nwtgck.japan_times_feature_analyser.datatype

/**
  * Data structure for Binary Classification
  *
  * @param posDocs
  * @param negDocs
  */
case class BinaryDataset(posDocs: Seq[EngDocument], negDocs: Seq[EngDocument])
