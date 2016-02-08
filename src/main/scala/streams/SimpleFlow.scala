import akka.actor.{Props, Actor, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Illustrates different kinds of Sink using a ActorMaterializer
  */
object SimpleFlow {
  implicit val system = ActorSystem("simple-log")
  implicit val materializer = ActorMaterializer()
  val source = Source (1 to 10)

  def main(array: Array[String]): Unit = {
    simpleFlow
    sinkFold
    sinkHead
    sinkOnComplete
    foo
  }

  def simpleFlow: Unit = {
    val source = Source(1 to 10).map(_ * 2)
    source.runWith(Sink.foreach(println))
  }

  def sinkFold: Unit = {
    val sink = Sink.fold[Int, Int](0)(_ + _)
    val runnableGraph = source.toMat(sink)(Keep.right)
    runnableGraph.run().foreach(println(_))
  }

  def sinkHead: Unit = {
    val sink = Sink.head[Int]
    val runnableGraph: RunnableGraph[Future[Int]] = source.toMat(sink)(Keep.right)
    runnableGraph.run().foreach(println(_))
  }

  def sinkOnComplete: Unit = {
    val sink = Sink.onComplete(e => println("done"))
    val runnableGraph:RunnableGraph[Unit] = source.toMat(sink)(Keep.right)
    runnableGraph.run()
  }

  def sinkActorRef: Unit = {
    val actor = system.actorOf(Props[Foo], "foo")
    val sink = Sink.actorRef(actor, "done")
    Sink.
    val runnableGraph = source.toMat(sink)(Keep.right)
    runnableGraph.run()

    class Foo extends Actor {
      def receive = {
        case a => println (s"received $a")
      }
    }
  }






}