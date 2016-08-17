package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.io.Source
import scala.util.Random
import Lib._

class BasicSimulation extends Simulation {

  val httpConf = http
    .baseURL("http://127.0.0.1:8080")
//    .baseURL("http://209.205.218.34:8080")
    .contentTypeHeader("application/json")

  val feeder = getFeeder.circular

  val scn = scenario("Load test")
    .feed(feeder)
    .repeat(1)(
      exec(http("sample request")
        .post("/bidder?sid=${sid}")
        .body(
          StringBody("${json}")
        )))

  setUp(scn.inject(atOnceUsers(100)).protocols(httpConf))


}

object Lib {

  private val r = Random

  private val adRequests = Source.fromFile("output.log").getLines()

  private val sources = Source.fromFile("sources.txt").getLines().filter(_.nonEmpty).toArray
  private val sourcesQtty = sources.length

  def getFeeder = adRequests.map( s => Map("json" -> s, "sid" -> getRandomSource) ).toArray

  def getRandomSource = sources(r.nextInt(sourcesQtty))

}