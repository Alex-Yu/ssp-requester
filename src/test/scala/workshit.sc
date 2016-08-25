import scala.io.Source
import scala.util.Random

object Lib {

  private val r = Random

  private val adRequests = Source.fromFile("/home/alex/projects/ssp-requester/output_all.log").getLines()

  private val sources = Source.fromFile("/home/alex/projects/ssp-requester/sources.txt").getLines().filter(_.nonEmpty).toArray
  private val sourcesQtty = sources.length

  def getFeeder(limit: Int = 1000) =
    (1 to limit).map { _ =>
      if (isRtb)
        Map(
          "json" -> adRequests.next(),
          "query" -> s"/bidder?sid=$getRandomSource"
        )
      else
        Map(
          "json" -> "",
          "query" -> s"/bidder?&sid=$getRandomSource&pubId=15&pubName=test&domain=google.com&ua=Mozilla/6.0%20(Macintosh;%20Intel%20Mac%20OS%20X%2010_10_4)%20AppleWebKit/600.7.12%20(KHTML,%20like%20Gecko)%20Version/8.0.7%20Safari/600.7.12&ip=190.93.245.15&h=70&w=300&maxd=30&floor=0.1&os=Mac%20OS%20X%2010_10_4&aid=123&pid=12"
        )
    }.toArray

  def getRandomSource = sources(r.nextInt(sourcesQtty))

  private def isRtb = r.nextBoolean()

}

Lib.getFeeder(10).foreach(println)