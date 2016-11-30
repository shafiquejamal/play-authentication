package contact

import chat.ChatContactFacade
import db.{CrauthAutoRollback, TestDBConnection, TestScalikeJDBCSessionProvider}
import org.scalatest.LoneElement._
import org.scalatest.TryValues._
import org.scalatest._
import org.scalatest.fixture.FlatSpec
import scalikejdbc.DBSession
import user.UserFixture
import util.{TestTimeProviderImpl, TestUUIDProviderImpl}

class ChatContactFacadeATest
  extends FlatSpec
  with ShouldMatchers
  with CrauthAutoRollback
  with UserFixture
  with BeforeAndAfterEach
  with TestDBConnection {

  "Retrieving all visible contacts for a user" should "retrieve the contacts that should be visible to " +
  "the user" in { session =>
    val api = makeAPI(session)
    api.visibleContactsFor(id1).loneElement shouldEqual "bob"
  }

  "Adding a contact" should "make this contact visible to the user" in { session =>
    val api = makeAPI(session)
    api.addContact(id1, id4).success.value shouldEqual id4
  }

  it should "fail if someone tries to add oneself as a contact" in { session =>
    val api = makeAPI(session)
    api.addContact(id1, id1).failure.exception.getMessage shouldEqual "Cannot add self"
  }

  "Adding multiple contacts" should "add only the contacts that is not oneself" in { session =>
    val api = makeAPI(session)
    api.addContacts(id1, Seq(id4, id1, id3)) should contain theSameElementsAs Seq(id4, id3)
  }

  private def makeAPI(session: DBSession) = {
    new ChatContactFacade(
      new ChatContactDAOImpl(TestScalikeJDBCSessionProvider(session), dBConfig, new TestUUIDProviderImpl()),
      new TestTimeProviderImpl())
  }
}
