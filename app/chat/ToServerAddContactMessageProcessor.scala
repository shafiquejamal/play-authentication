package chat

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import contact.{ToServerAddContactMessage, ToServerAddContactsMessage, ToServerRequestContactsMessage}
import user.UserAPI

import scala.util.{Failure, Success}

class ToServerAddContactMessageProcessor(
    userAPI: UserAPI,
    chatContactsAPI: ChatContactAPI,
    clientId: UUID,
    toServerRequestContactsMessageActor: ActorRef)
  extends Actor
  with ActorLogging {

  override def receive = {

    case toServerAddContactMessage: ToServerAddContactMessage =>

      val maybeUserIdOfContactToAdd =
        userAPI.by(toServerAddContactMessage.usernameOfContactToAdd)
        .orElse(userAPI.findByEmailLatest(toServerAddContactMessage.usernameOfContactToAdd).flatMap(_.maybeId))
      maybeUserIdOfContactToAdd foreach { userIdOfContactToAdd =>
        chatContactsAPI.addContact(clientId, userIdOfContactToAdd) match {
          case Success(uUID) =>
            toServerRequestContactsMessageActor ! ToServerRequestContactsMessage("")
          case Failure(_) =>
        }
      }

    case toServerAddContactsMessage: ToServerAddContactsMessage =>

      val userIdsOfContactsToAdd =
        toServerAddContactsMessage.usernamesOfContactToAdd.flatMap { usernameOfContactToAdd =>
          userAPI.by(usernameOfContactToAdd)
          .orElse(userAPI.findByEmailLatest(usernameOfContactToAdd).flatMap(_.maybeId))
        }
      chatContactsAPI.addContacts(clientId, userIdsOfContactsToAdd)
      toServerRequestContactsMessageActor ! ToServerRequestContactsMessage("")

  }

}

object ToServerAddContactMessageProcessor {

  def props(
      userAPI: UserAPI,
      chatContactsAPI: ChatContactAPI,
      clientId: UUID,
      toServerRequestContactsMessageActor: ActorRef) =
    Props(new ToServerAddContactMessageProcessor(userAPI, chatContactsAPI, clientId, toServerRequestContactsMessageActor))

}