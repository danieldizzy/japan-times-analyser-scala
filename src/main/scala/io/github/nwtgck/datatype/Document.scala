package io.github.nwtgck.datatype

/**
  * Created by Ryo on 2017/01/24.
  */
abstract class Document {
  val entity: String

  /**
    * Contained words in this document
    */
  lazy val wordsSet: Set[Word] =
    this.entity.split(" ").map(Word(_)).toSet


  /**
    * Get a frequency of contained words
    * CAUTION) default value is 0
    *
    * @return
    */
  lazy val wordFreq: Map[Word, Int] =
  this.entity
    //      .replaceAll("['.\"!@\\-~+#$^\\(\\)?,]", " ") if not use this replacement, less error rate
    .split("\\s")
    .map(Word(_))
    .groupBy(identity(_))
    .map{case (k, v) =>(k, v.length)}
    .withDefaultValue(0)

}
