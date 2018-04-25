package io.github.nwtgck.japan_times_feature_analyser.datatype

/**
  * Created by Ryo on 2017/01/29.
  */
case class LabeledDocument[L](label: L, entity: String) extends Document
