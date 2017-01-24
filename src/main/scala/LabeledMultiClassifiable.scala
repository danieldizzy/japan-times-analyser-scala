/**
  * Created by Jimmy on 5/12/2016.
  */
trait LabeledMultiClassifiable[L] {
  def multiDataset(): LabeledMultiDataset[L]
}
