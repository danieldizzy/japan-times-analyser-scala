package io.github.nwtgck.japan_times_feature_vector.datatype

/**
  * A type which is binary classifiable
  */
trait BinaryClassifiable {
  def binaryDataset(): BinaryDataset
}
