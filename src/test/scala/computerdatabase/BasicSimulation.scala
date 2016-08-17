package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.io.Source
import scala.util.Random
import Lib._
import com.sun.org.apache.xalan.internal.utils.XMLSecurityManager.Limit

class BasicSimulation extends Simulation {

  val httpConf = http
    .baseURL("http://127.0.0.1:8080")
    //    .baseURL("http://209.205.218.34:8080")
    .contentTypeHeader("application/json")

  val feeder = getFeeder().random

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

  private val adRequests = Source.fromFile("output_all.log").getLines()

  private val sources = Source.fromFile("sources.txt").getLines().filter(_.nonEmpty).toArray
  private val sourcesQtty = sources.length

  def getFeeder(limit: Int = 10000) =
    (1 to limit) map ( _ => Map("json" -> adRequests.next(), "sid" -> getRandomSource) ) toArray

  def getRandomSource = sources(r.nextInt(sourcesQtty))

}

