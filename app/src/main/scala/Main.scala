import core.Actor.{DispatcherActor, ParticularActor, TemperatureActor}
import core.Stream.{FakeUdpStream, StreamObserver}
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.apache.pekko.actor.{ActorRef, ActorSystem, Props}


@main def main(): Unit = {
  FakeUdpStream.start()

  val system = ActorSystem("iot")
  val temperatureActor = system.actorOf(TemperatureActor.props(), "temp")
  val particularActor = system.actorOf(ParticularActor.props(),"particular")
  val dispatcherActor = system.actorOf(DispatcherActor.props(temperatureActor,particularActor), "filter")

  val source= FakeUdpStream.subject
  source.subscribe(StreamObserver(dispatcherActor))

  Thread.sleep(5000)
  FakeUdpStream.stop()
  system.terminate()
}

