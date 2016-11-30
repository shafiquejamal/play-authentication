package socket

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import chat._
import contact.ToServerRequestContactsMessage
import user.UserAPI
import util.{TimeProvider, UUIDProvider}

class ToServerMessageRouter(
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

  val toServerChatMessageActor =
    context.actorOf(
      ToServerChatMessageProcessor.props(
        client, userAPI, chatMessageAPI, clientId, clientUsername, timeProvider, uUIDProvider))

  val toServerRequestContactsMessageActor =
    context.actorOf(ToServerRequestContactsMessageProcessor.props(chatContactsAPI, clientId))

  val toServerAddContactMessageActor =
    context.actorOf(
      ToServerAddContactMessageProcessor.props(userAPI, chatContactsAPI, clientId, toServerRequestContactsMessageActor))

  val toServerRequestMessagesMessageActor =
    context.actorOf(ToServerRequestMessagesMessageProcessor.props(client, chatMessageAPI, clientId))

  override def receive = {

    case toServerChatMessage: ToServerChatMessage =>

      toServerChatMessageActor ! toServerChatMessage

    case toServerRequestMessagesMessage: ToServerRequestMessagesMessage =>

      toServerRequestMessagesMessageActor ! toServerRequestMessagesMessage

    case toServerRequestContactsMessage: ToServerRequestContactsMessage =>

      toServerRequestContactsMessageActor ! toServerRequestContactsMessage

    case toServerAddContactOrContactsMessage: ToServerAddContactOrContactsMessage =>

      toServerAddContactMessageActor ! toServerAddContactOrContactsMessage

  }

}

object ToServerMessageRouter {

  def props(
    client: ActorRef,
    userAPI: UserAPI,
    chatMessageAPI: ChatMessageAPI,
    chatContactsAPI: ChatContactAPI,
    clientId: UUID,
    clientUsername: String,
    timeProvider: TimeProvider,
    uUIDProvider: UUIDProvider) =
      Props(
        new ToServerMessageRouter(
          client, userAPI, chatMessageAPI, chatContactsAPI, clientId, clientUsername, timeProvider, uUIDProvider))

}