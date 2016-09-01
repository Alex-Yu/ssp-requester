package computerdatabase

import computerdatabase.Lib._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.io.Source
import scala.util.Random

class BasicSimulation extends Simulation {

  val httpConf = http
//    .baseURL("http://localhost:8080")
//        .baseURL("http://209.205.219.186:80")
//        .baseURL("http://209.205.219.58:8080")
        .baseURL("http://aux-log.videe.tv")
    .contentTypeHeader("application/json")
    .maxConnectionsPerHost(600)
    .shareConnections

  val rps = 38000
  val extra = rps * 1.1
  val halfRps = rps / 2
  val quartRps = rps / 4
  val hqRps = halfRps + quartRps

  val firstScn = scenario("load").exec(AdRequest.adRequest)
    .inject(
      rampUsersPerSec(1) to quartRps during (10 seconds),
      constantUsersPerSec(quartRps) during (50 seconds),
      rampUsersPerSec(quartRps) to halfRps during (10 seconds),
      constantUsersPerSec(halfRps) during (50 seconds),
      rampUsersPerSec(halfRps) to hqRps during (10 seconds),
      constantUsersPerSec(hqRps) during (50 seconds),
      rampUsersPerSec(hqRps) to rps during (10 seconds),
      constantUsersPerSec(rps) during (90 seconds),
      rampUsersPerSec(rps) to extra during (10 seconds),
      constantUsersPerSec(extra) during (90 seconds),
      nothingFor(5 seconds)
    ).protocols(httpConf)

  setUp(
    firstScn
  )

}

object AdRequest {
  val feeder = getFeeder().random

  val adRequest =
    feed(feeder)
      .doIfEqualsOrElse("${json}", "") {
        exec(
          http(s"non-Rtb")
            .get("${query}")
        )
      } {
        exec(
        http(s"Rtb")
          .post("${query}")
          .body(StringBody("${json}"))
        )
      }
}

object Lib {

  private val r = Random

  private val adRequests = Source.fromFile("output_all.log").getLines()

  private val sources = Source.fromFile("sources.txt").getLines().filter(_.nonEmpty).toArray
  private val sourcesQtty = sources.length

  def getFeeder(limit: Int = 1000) =
    (1 to limit).map { _ =>
      if (isRtb(0))
        Map(
          "json" -> adRequests.next(),
          "query" -> s"/bidder?sid=$getRandomSource"
        )
      else
        Map(
          "json" -> "",
          "query" -> s"/bidder?&sid=$getRandomSource&pubId=15&pubName=test&domain=google.com&ua=Mozilla/6.0%20(Macintosh;%20Intel%20Mac%20OS%20X%2010_10_4)%20AppleWebKit/600.7.12%20(KHTML,%20like%20Gecko)%20Version/8.0.7%20Safari/600.7.12&ip=$getRandomIp&h=70&w=300&maxd=30&floor=0.1&os=Mac%20OS%20X%2010_10_4&aid=123&pid=12&adid=100500"
        )
    }.toArray

  def getRandomSource = sources(r.nextInt(sourcesQtty))

  def getRandomIp = {
    def q = r.nextInt(253) + 1
    s"$q.$q.$q.$q"
  }

  private def isRtb(share: Int = 50) =
    share > 0 && r.nextInt(101) <= share

}

