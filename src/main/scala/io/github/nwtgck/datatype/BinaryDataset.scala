package io.github.nwtgck.datatype

/**
  * Data structure for Binary Classification
  *
  * @param posDocs
  * @param negDocs
  */
case class BinaryDataset(posDocs: Seq[EngDocument], negDocs: Seq[EngDocument])
