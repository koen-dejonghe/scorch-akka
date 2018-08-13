package botkop.akka.imperative

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

object HelloTypedWorld extends App {
  val system: ActorSystem[HelloWorldMain.Start] =
    ActorSystem(HelloWorldMain.main, "hello")

  system ! HelloWorldMain.Start("World")
  system ! HelloWorldMain.Start("Akka")
}

object HelloWorldMain {
  final case class Start(name: String)

  val main: Behavior[Start] =
    Behaviors.setup { context ⇒
      val greeter = context.spawn(HelloWorld.greeter, "greeter")

      Behaviors.receiveMessage { msg ⇒
        val replyTo = context.spawn(HelloWorldBot.bot(greetingCounter = 0, max = 3), msg.name)
        greeter ! HelloWorld.Greet(msg.name, replyTo)
        Behaviors.same
      }
    }
}

object HelloWorldBot {
  def bot(greetingCounter: Int, max: Int): Behavior[HelloWorld.Greeted] =
    Behaviors.receive { (ctx, msg) ⇒
      val n = greetingCounter + 1
      ctx.log.info("Greeting {} for {}", n, msg.whom)
      if (n == max) {
        Behaviors.stopped
      } else {
        msg.from ! HelloWorld.Greet(msg.whom, ctx.self)
        bot(n, max)
      }
    }
}

object HelloWorld {
  final case class Greet(whom: String, replyTo: ActorRef[Greeted])
  final case class Greeted(whom: String, from: ActorRef[Greet])

  val greeter: Behavior[Greet] = Behaviors.receive { (ctx, msg) ⇒
    ctx.log.info("Hello {}!", msg.whom)
    msg.replyTo ! Greeted(msg.whom, ctx.self)
    Behaviors.same
  }
}
