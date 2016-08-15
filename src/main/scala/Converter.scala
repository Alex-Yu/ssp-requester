import java.io.{BufferedWriter, FileWriter}

import scala.io.Source
import scala.language.reflectiveCalls

/**
  * Created by alex on 12.08.16.
  */
object Converter extends App {


  var source = None: Option[Source]
  var writer = None: Option[BufferedWriter]
  var i = 0
  try {

    source = Some(Source.fromFile("sample.log"))
    writer = Some(new BufferedWriter(new FileWriter("output.log")))

    for {
      s <- source
      w <- writer
    } yield {
      s.getLines().foreach { s =>

        if (i % 100000 == 0) println(s"$i converted")
        i += 1
        val cleaned =
          s.split('|').last
            .replaceAll("^\"", "")
            .replaceAll("\"$", "")
            .replaceAll("\\\\x22", "\"")
            .replaceAll("\\\\x[0-9A-Fa-f]{2}", "")
        w.write(cleaned + "\n")
      }
    }
  } finally {
    source.foreach(_.close())
    writer.foreach(_.close())
  }
}
