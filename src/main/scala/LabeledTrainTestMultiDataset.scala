/**
  * Created by Ryo on 2016/11/25.
  */
/**
  * Data structure for Multi Classification
  *
  * @param trainTestDocs
  */
case class LabeledTrainTestMultiDataset[L](trainTestDocs: Seq[LabeledTrainTestDocument[L]], classNum: Int)
