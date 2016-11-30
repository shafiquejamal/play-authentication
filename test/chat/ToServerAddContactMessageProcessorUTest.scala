package chat

import akka.actor.ActorSystem
import akka.testkit.TestKit
import contact.{ToServerAddContactMessage, ToServerAddContactsMessage, ToServerRequestContactsMessage}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpecLike, ShouldMatchers}
import user.UserAPI
import util.{StopSystemAfterAll, TestUUIDProviderImpl}

import scala.util.Success

class ToServerAddContactMessageProcessorUTest
  extends TestKit(ActorSystem("testsystem"))
  with ShouldMatchers
  with FlatSpecLike
  with StopSystemAfterAll
  with MockFactory {

  val mockUserAPI = mock[UserAPI]
  val mockChatContactAPI = mock[ChatContactAPI]
  val uUIDProvider = new TestUUIDProviderImpl()
  val clientId = uUIDProvider.randomUUID()
  val toServerAddContactMessageProcessor =
    system.actorOf(ToServerAddContactMessageProcessor.props(mockUserAPI, mockChatContactAPI, clientId, testActor))
  val username = "some_user_to_add"
  val toServerAddContactMessage = ToServerAddContactMessage(username)

  val userIdOfUserToAdd = uUIDProvider.randomUUID()

  "The ToServerAddContactMessageProcessor" should "add the contact to the db and send out a request contacts message" +
  "to the request contacts message processor, if the user exists" in {
    (mockUserAPI.by(_: String)).expects(username).returning(Some(userIdOfUserToAdd))
    (mockChatContactAPI.addContact _).expects(clientId, userIdOfUserToAdd).returning(Success(userIdOfUserToAdd))


    toServerAddContactMessageProcessor ! toServerAddContactMessage

    expectMsg(ToServerRequestContactsMessage(""))
  }

  it should "send no message if the user to add does not exist" in {
    (mockUserAPI.by(_: String)).expects(username).returning(None)

    toServerAddContactMessageProcessor ! toServerAddContactMessage

    expectNoMsg()
  }

  it should "not respond to other messages" in {
    toServerAddContactMessageProcessor ! ToServerRequestContactsMessage("")

    expectNoMsg()
  }

  it should "add multiple contacts if multiple contacts were sent" in {
    val anotherUserIdOfUserToAdd = uUIDProvider.randomUUID()
    val anotherUsername = "another_username"
    (mockUserAPI.by(_: String)).expects(username).returning(Some(userIdOfUserToAdd))
    (mockUserAPI.by(_: String)).expects(anotherUsername).returning(Some(anotherUserIdOfUserToAdd))
    (mockChatContactAPI.addContacts _).expects(clientId, Seq(userIdOfUserToAdd, anotherUserIdOfUserToAdd))
      .returning(Seq(userIdOfUserToAdd, anotherUserIdOfUserToAdd))

    toServerAddContactMessageProcessor ! ToServerAddContactsMessage(Seq(username, anotherUsername))

    expectMsg(ToServerRequestContactsMessage(""))
  }

}
