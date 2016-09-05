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
    .baseURL("http://209.205.218.34:8090")
    //        .baseURL("http://209.205.219.58:8080")
    //        .baseURL("http://aux-log.videe.tv")
    .contentTypeHeader("application/json")
    .maxConnectionsPerHost(15000)
    .shareConnections

  val rps = 60000

  val firstScn = scenario("load").exec(AdRequest.adRequest)
    .inject(
      rampUsersPerSec(1) to rps during (10 seconds),
      constantUsersPerSec(rps) during (240 seconds)
      /*rampUsersPerSec(1) to quartRps during (10 seconds),
      constantUsersPerSec(quartRps) during (50 seconds),
      rampUsersPerSec(quartRps) to halfRps during (10 seconds),
      constantUsersPerSec(halfRps) during (50 seconds),
      rampUsersPerSec(halfRps) to hqRps during (10 seconds),
      constantUsersPerSec(hqRps) during (50 seconds),
      rampUsersPerSec(hqRps) to rps during (10 seconds),
      constantUsersPerSec(rps) during (90 seconds),
      rampUsersPerSec(rps) to extra during (10 seconds),
      constantUsersPerSec(extra) during (90 seconds),
      nothingFor(5 seconds)*/
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



  private val sources = Source.fromFile("sources.txt").getLines().filter(_.nonEmpty).toArray
  private val sourcesQtty = sources.length

  def getFeeder(limit: Int = 1000) =
    (1 to limit).map { _ =>
      Map(
        "json" -> "",
        "query" -> s"/bidresponse"
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

