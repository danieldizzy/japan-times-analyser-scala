package io.github.nwtgck.japan_times_feature_vector.pycall

import java.io.Closeable
import java.net.Socket
import java.nio.ByteBuffer

import org.bson.{BasicBSONDecoder, BasicBSONEncoder, BasicBSONObject}

import scala.util.Try


class PythonCall extends Closeable{

  val host = "localhost"
  val port = 2345

  private val socket = new Socket(host, port)
  private val outputStream = socket.getOutputStream
  private val inputStream = socket.getInputStream

  def send(funcName: String, args: Object*): Try[Object] = {

    Try {


      import scala.collection.JavaConversions._

      // Create a request BSON
      val reqBson = new BasicBSONObject(Map[String, Object](
        "func_name" -> funcName,
        "arguments" -> args.toArray.asInstanceOf[Object]
      ))

      // Encode the request BSON
      val bsonEncoder = new BasicBSONEncoder()
      val bsonBytes = bsonEncoder.encode(reqBson)


      // Send the length of the request BSON to server
      val lengthBytes = ByteBuffer.allocate(4).putInt(bsonBytes.length).array()
      outputStream.write(lengthBytes)

      // Send the request BSON to the server
      outputStream.write(bsonBytes)


      val bsonDecoder = new BasicBSONDecoder()


      val resLengthBytes: Array[Byte] = {
        val _res = new Array[Byte](4)
        inputStream.read(_res)
        _res
      }

      val resLength = ByteBuffer.wrap(resLengthBytes).getInt()

      val resBson = {
        val _bson = new Array[Byte](resLength)
        inputStream.read(_bson)
        bsonDecoder.readObject(_bson)
      }

      // Return the response BSON
      val pythonError = resBson.get("error")
      if(pythonError == null){
        resBson.get("result")
      } else {
        throw new Exception(s"Python Error: ${pythonError}")
      }

    }
  }

  def close(): Unit = {
    socket.close()
  }
}