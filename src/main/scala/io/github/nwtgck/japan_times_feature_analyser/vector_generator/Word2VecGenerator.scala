package io.github.nwtgck.japan_times_feature_analyser.vector_generator

import java.io.File

import io.github.nwtgck.japan_times_feature_analyser.datatype.Document
import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}

/**
  * Created by Jimmy on 24/01/2017.
  */
object Word2VecGenerator {
  def calcOrGetCacheModel(sparkContext: SparkContext, vectorSize: Int, jptimesFilePath: String) = {

    /** [[jptimesFilePath]] file is required */
    require(new File(jptimesFilePath).exists())

    val corpusPath       = "./data/cache-word2vec-courpus"
    val modelPath        = "./data/cache-word2vec-model"

    if(!new File(corpusPath).exists()){
      sparkContext.textFile(jptimesFilePath).saveAsTextFile(corpusPath)
    }

    val input = sparkContext.textFile(corpusPath).map(_.split(" ").toVector)


    val model =
      if(new File(modelPath).exists()){
        Word2VecModel.load(sparkContext, modelPath)
      } else {
        val _model = new Word2Vec()
          .setVectorSize(vectorSize)
          //          .setNumPartitions(20)
          //          .setMinCount(1)
          .fit(input)

        _model.save(sparkContext, modelPath)
        _model
      }
    model
  }


  def calcOrGetCacheModelByAllDocs(sparkContext: SparkContext, vectorSize: Int, numIterations: Int, allDocuments: Seq[Document], additionalDocsForWord2vec: Seq[Document] = Seq.empty) = {


    val allWordsSize = allDocuments.flatMap(_.wordsSet).size
    val corpusPath       = s"./data/cache-word2vec-courpus-wordsize${allWordsSize}"
    val modelPath        = s"./data/cache-word2vec-model-wordsize${allWordsSize}-dem${vectorSize}-iternum${numIterations}"

    if(!new File(corpusPath).exists()){
//      sparkContext.textFile("dummy").saveAsTextFile(corpusPath)
      (sparkContext.makeRDD(allDocuments).map(_.entity) ++ sparkContext.makeRDD(additionalDocsForWord2vec).map(_.entity))
        .saveAsTextFile(corpusPath)

    }

    val input = sparkContext.textFile(corpusPath).map(s => s.split(" ").toVector)

    val model =
      if(new File(modelPath).exists()){
        Word2VecModel.load(sparkContext, modelPath)
      } else {
        val _model = new Word2Vec()
          .setVectorSize(vectorSize)
          //          .setNumPartitions(20)
          //          .setMinCount(1)
          .setNumIterations(numIterations)
          .fit(input)

        _model.save(sparkContext, modelPath)
        _model
      }
    model
  }
}
