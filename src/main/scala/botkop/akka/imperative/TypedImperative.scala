package botkop.akka.imperative

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import botkop.akka.imperative.HelloWorldMain.Start

object TypedImperative extends App {

  val system: ActorSystem[Start] =
    ActorSystem(main, getClass.getName)

  val main: Behavior[Start] =
    Behaviors.setup { context ⇒
      Behaviors.receiveMessage { msg =>
        Behaviors.same
      }
    }

  case object Start

}
