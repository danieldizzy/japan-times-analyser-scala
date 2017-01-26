/**
  * Created by Ryo on 2016/11/25.
  */
/**
  * Data structure for Multi Classification
  *
  * @param docs
  */
case class LabeledMultiDataset[L](docs: Seq[LabeledJapanTimesArticle[L]], classNum: Int)
