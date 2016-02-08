import akka.actor.ActorSystem
import akka.stream.{ClosedShape, ActorMaterializer}
import akka.stream.scaladsl._

import scala.concurrent.Future

object SimpleBroadcast {
  implicit val system = ActorSystem("simple-log")
  implicit val materializer = ActorMaterializer()
  val source = Source (1 to 10)

  def main(arr: Array[String]): Unit = {
     val g = createGraph
     g.run()
    println ("done")
  }

  def createGraph = {
    RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val bcast = b.add(Broadcast[Int](2))
      source ~> bcast.in
      bcast.out(0) ~> Flow[Int].map(e => e * 2) ~> Sink.foreach[Int](println(_))
      bcast.out(1) ~> Flow[Int].map(e => e * 3) ~> Sink.foreach[Int](println(_))
      ClosedShape
    })
  }
}