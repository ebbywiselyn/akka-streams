import akka.actor.{Props, Actor, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Illustrates different kinds of Sink using an ActorMaterializer
  */
object SimpleFlow {
  implicit val system = ActorSystem("simple-log")
  implicit val materializer = ActorMaterializer()
  val source = Source (1 to 10)

  def main(array: Array[String]): Unit = {
    simpleFlow
    simpleMatValues

    sinkFold
    sinkHead
    sinkOnComplete
    sinkActorRef
    compose

    sourceIterator
  }

  def simpleFlow: Unit = {
    val source = Source(1 to 10).map(_ * 2)
    source.runWith(Sink.foreach(println))
  }

  def simpleMatValues: Unit = {
    val count: Flow[Int, Int, Unit] = Flow[Int].map(_ => 1)
    val sumSink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)

    val counterGraph: RunnableGraph[Future[Int]] = source.via(count).toMat(sumSink)(Keep.right)
    val sum: Future[Int] = counterGraph.run()
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
    val runnableGraph = source.toMat(sink)(Keep.right)
    runnableGraph.run()

    class Foo extends Actor {
      def receive = {
        case a: Any => println (s"received $a")
      }
    }
  }

  def sourceIterator: Unit = {
    val source = Source.fromIterator(() => List("hello", "world").iterator)
    val sink = Sink.head[String]
    val runnableGraph: RunnableGraph[Future[String]] = source.toMat(sink)(Keep.right)
    runnableGraph.run().foreach(println(_))
  }

  def compose: Unit = {
    source.map(t => 1).runWith(Sink.fold[Int, Int](0)(_ + _))
  }
}