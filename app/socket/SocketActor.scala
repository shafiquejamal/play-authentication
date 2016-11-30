package socket

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import chat._
import user.UserAPI
import util.{TimeProvider, UUIDProvider}

class SocketActor(
    client: ActorRef,
    userAPI: UserAPI,
    chatMessageAPI: ChatMessageAPI,
    chatContactsAPI: ChatContactAPI,
    clientId: UUID,
    clientUsername: String,
    timeProvider: TimeProvider,
    uUIDProvider: UUIDProvider)
  extends Actor
  with ActorLogging {

  import play.api.libs.json.JsValue

  val toServerMessageRouter =
    context.actorOf(
      ToServerMessageRouter.props(
        client, userAPI, chatMessageAPI, chatContactsAPI, clientId, clientUsername, timeProvider, uUIDProvider))

  override def receive = {

    case msg: JsValue =>
      val messageType = (msg \ "messageType").validate[String].getOrElse("")
      val socketMessage = ToServerSocketMessageType.from(messageType).socketMessage(msg)
      socketMessage.send(client, toServerMessageRouter)

    case toClientSocketMessage : ToClientSocketMessage =>

      client ! toClientSocketMessage.toJson

  }
}

object SocketActor {

  def props(
      client: ActorRef,
      userAPI: UserAPI,
      chatMessageAPI: ChatMessageAPI,
      chatContactsAPI: ChatContactAPI,
      clientId: UUID,
      clientUsername: String,
      timeProvider: TimeProvider,
      uUIDProvider: UUIDProvider) =
    Props(new SocketActor(client, userAPI, chatMessageAPI, chatContactsAPI, clientId, clientUsername, timeProvider, uUIDProvider))

}

