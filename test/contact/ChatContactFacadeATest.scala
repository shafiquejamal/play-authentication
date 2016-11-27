package contact

import chat.ChatContactFacade
import db.{TestScalikeJDBCSessionProvider, TestDBConnection, CrauthAutoRollback}
import org.scalatest._
import org.scalatest.fixture.FlatSpec
import scalikejdbc.DBSession
import user.UserFixture
import util.{TestUUIDProviderImpl, TestTimeProviderImpl}
import LoneElement._
import TryValues._

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

  private def makeAPI(session: DBSession) = {
    new ChatContactFacade(
      new ChatContactDAOImpl(TestScalikeJDBCSessionProvider(session), dBConfig, new TestUUIDProviderImpl()),
      new TestTimeProviderImpl())
  }
}
