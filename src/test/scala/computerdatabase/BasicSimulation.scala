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

  val scn = scenario("Load test").exec(AdRequest.adRequest)

  val firstScn = scn.inject(
    /*rampUsers(8000) over (10 seconds),
    nothingFor(5 seconds),*/
    constantUsersPerSec(200) during (20 seconds),
    nothingFor(5 seconds),
    constantUsersPerSec(400) during (20 seconds),
    nothingFor(5 seconds)
    /*constantUsersPerSec(6000) during (20 seconds),
    nothingFor(5 seconds),
    constantUsersPerSec(8000) during (20 seconds),
    nothingFor(5 seconds),
    constantUsersPerSec(10000) during (20 seconds)*/
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

