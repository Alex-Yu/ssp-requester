package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.io.Source

class BasicSimulation extends Simulation {

  val httpConf = http
    .baseURL("http://localhost:8080")
    .contentTypeHeader("application/json")

  val feeder = getFeeder("output.log").circular

  val scn = scenario("Load test")
    .feed(feeder)
    .repeat(100) (
      exec(http("sample request")
        .post("/bidder")
        .body(
          StringBody("${json}")
        )))

  setUp(scn.inject(atOnceUsers(1)).protocols(httpConf))

  def getFeeder(fileName: String): Array[Map[String, String]] = {
    val source = Source.fromFile(fileName).getLines()

    source.map( s => Map("json" -> s) ).toArray
  }

}