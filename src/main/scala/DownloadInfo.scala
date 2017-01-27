/**
  * Created by Ryo on 2017/01/27.
  */
case class DownloadInfo(storedPath: String, pageLimit: Int, pageToUrls: Seq[Int => String])
