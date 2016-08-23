package computerdatabase

import computerdatabase.Lib._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.io.Source
import scala.util.Random

class BasicSimulation extends Simulation {

  val httpConf = http
    //    .baseURL("http://127.0.0.1:8080")
//    .baseURL("http://209.205.218.34:8080")
//    .baseURL("http://209.205.219.218:8080")
    .baseURL("http://209.205.219.186:80")
    .contentTypeHeader("application/json")
    .maxConnectionsPerHost(300)
    .shareConnections

  val rps = 26000
  val extra = rps + 1000
  val halfRps = rps / 2
  val quartRps = rps / 4
  val hqRps = halfRps + quartRps

  //  val firstScn = scenario("load").exec(Array.fill(1000)(AdRequest.adRequest))
  val firstScn = scenario("load").exec(AdRequest.adRequest)
    .inject(
      rampUsersPerSec(1) to quartRps during (10 seconds),
      constantUsersPerSec(quartRps) during (50 seconds),
      rampUsersPerSec(quartRps) to halfRps during (10 seconds),
      constantUsersPerSec(halfRps) during (50 seconds),
      rampUsersPerSec(halfRps) to hqRps during (10 seconds),
      constantUsersPerSec(hqRps) during (50 seconds),
      rampUsersPerSec(hqRps) to rps during (10 seconds),
      constantUsersPerSec(rps) during (50 seconds),
      rampUsersPerSec(rps) to extra during (10 seconds),
      constantUsersPerSec(extra) during (20 seconds),
      nothingFor(5 seconds)
    ).protocols(httpConf)

  setUp(
    firstScn
  )/*.throttle(
    reachRps(rps) in (10 seconds),
    holdFor(50 seconds)
  )*/


}

object AdRequest {
  val feeder = getFeeder().random

  val adRequest = feed(feeder)
    .exec(http("sample request")
      .post("/bidder?sid=${sid}")
      .body(
        StringBody("${json}")
      ))
}

object Lib {

  private val r = Random

  private val adRequests = Source.fromFile("output_all.log").getLines()

  private val sources = Source.fromFile("sources.txt").getLines().filter(_.nonEmpty).toArray
  private val sourcesQtty = sources.length

  def getFeeder(limit: Int = 1000) =
    (1 to limit) map ( _ => Map("json" -> adRequests.next(), "sid" -> getRandomSource) ) toArray

  def getRandomSource = sources(r.nextInt(sourcesQtty))

}

