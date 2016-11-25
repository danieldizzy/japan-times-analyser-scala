/**
  * Created by Ryo Ota on 2016/11/07.
  */
case class EngDocument(entity: String){

  /**
    * Contained words in this document
    */
  lazy val wordsSet: Set[Word] =
    this.entity.split(" ").map(Word(_)).toSet


  /**
    * Get a frequency of contained words
    * CAUTION) default value is 0
    * @return
    */
  lazy val wordFreq: Map[Word, Int] =
    this.entity.split("\\s")
      .map(Word(_))
      .groupBy(identity(_))
      .map{case (k, v) =>(k, v.length)}
      .withDefaultValue(0)

}
