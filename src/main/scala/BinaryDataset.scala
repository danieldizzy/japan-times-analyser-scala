/**
  * Created by Ryo on 2016/11/25.
  */
/**
  * Data structure for Binary Classification
  *
  * @param posDocs
  * @param negDocs
  */
case class BinaryDataset(posDocs: Seq[EngDocument], negDocs: Seq[EngDocument])
