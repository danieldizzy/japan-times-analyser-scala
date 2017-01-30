/**
  * Created by Jimmy on 5/12/2016.
  */
trait LabeledTrainTestMultiClassifiable[L] {
  def trainTestMultiDataset(): LabeledTrainTestMultiDataset[L]
}
