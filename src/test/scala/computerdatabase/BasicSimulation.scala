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
        .baseURL("http://209.205.218.34:8080")
    .contentTypeHeader("application/json")
    .maxConnectionsPerHost(300)
    .shareConnections



//  val firstScn = scenario("load").exec(Array.fill(1000)(AdRequest.adRequest))
  val firstScn = scenario("load").exec(AdRequest.adRequest)
    .inject(
    rampUsers(500) over (10 seconds),
    nothingFor(5 seconds),
    constantUsersPerSec(5000) during (60 seconds),
    nothingFor(10 seconds),
    constantUsersPerSec(7000) during (60 seconds),
    nothingFor(10 seconds),
    constantUsersPerSec(10000) during (60 seconds)
  ).protocols(httpConf)

  setUp(
    firstScn
  )


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

  def getFeeder(limit: Int = 10000) =
    (1 to limit) map ( _ => Map("json" -> adRequests.next(), "sid" -> getRandomSource) ) toArray

  def getRandomSource = sources(r.nextInt(sourcesQtty))

}

