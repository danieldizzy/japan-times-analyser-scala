package io.github.nwtgck.japan_times_feature_vector.datatype

/**
  * Created by Jimmy on 5/12/2016.
  */
trait LabeledTrainTestMultiClassifiable[L] {
  def trainTestMultiDataset(): LabeledTrainTestMultiDataset[L]
}