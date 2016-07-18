package access.registration

import java.io.File

import access.{JWTParamsProvider, TestJWTParamsProviderImpl}
import com.typesafe.config.ConfigFactory
import db._
import org.scalatest._
import pdi.jwt.JwtJson
import play.api.Configuration
import play.api.http.HeaderNames
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._
import scalikejdbc.NamedAutoSession
import util.{PlayConfigParamsProvider, TestUUIDProviderImpl, UUIDProvider}

class RegistrationControllerATest
  extends FlatSpec
  with ShouldMatchers
  with OneAppPerTestWithOverrides
  with BeforeAndAfterEach
  with InitialMigration {

  override def overrideModules =
    Seq(bind[DBConfig].to[ScalikeJDBCTestDBConfig],
        bind[JWTParamsProvider].to[TestJWTParamsProviderImpl],
        bind[UUIDProvider].to[TestUUIDProviderImpl]
        )

  val dBConfig = new ScalikeJDBCTestDBConfig(new PlayConfigParamsProvider(new Configuration(ConfigFactory.parseFile(new File("conf/application.conf")))))

  override def beforeEach() {
    implicit val session = NamedAutoSession(Symbol(dBConfig.dBName))
    dBConfig.setUpAllDB()
    migrate(dBConfig)
    super.beforeEach()
  }

  override def afterEach() {
    dBConfig.closeAll()
    super.afterEach()
  }

  "Checking whether a username is available" should "return true if the username is available" in {
    val result = route(app, FakeRequest(GET, "/username/available")).get
    val content = contentAsJson(result)
    (content \ "status").asOpt[Boolean] should contain(true)
  }

  "Checking whether an email address is available" should "return true if the email is available" in {
    val result = route(app, FakeRequest(GET, "/email/available")).get
    val content = contentAsJson(result)
    (content \ "status").asOpt[Boolean] should contain(true)
  }

  "Registering a new user" should "result a token if the username and email address are available and these " +
  "and the password are valid" in {
    val registration = Json.toJson(Map("username" -> "newuser", "email" -> "new@user.com", "password" -> "pass"))
    val result = route(app, FakeRequest(POST, "/register")
      .withJsonBody(registration)
      .withHeaders(HeaderNames.CONTENT_TYPE -> "application/json"))
      .get
    val content = contentAsJson(result)
    val uUIDProvider = new TestUUIDProviderImpl()
    uUIDProvider.index = 0
    val uUID = uUIDProvider.randomUUID()
    val claim = Json.obj("userId" -> uUID)
    val jWTParamsProvider = new TestJWTParamsProviderImpl()
    val expectedJWT = JwtJson.encode(claim, jWTParamsProvider.secretKey, jWTParamsProvider.algorithm)

    (content \ "token").asOpt[String] should contain(expectedJWT)

    val checkEmailAvailable = contentAsJson(route(app, FakeRequest(GET, "/email/new@user.com")).get)
    (checkEmailAvailable \ "status").asOpt[Boolean] should contain(false)

    val checkUsernameAvailable = contentAsJson(route(app, FakeRequest(GET, "/username/newuser")).get)
    (checkUsernameAvailable \ "status").asOpt[Boolean] should contain(false)
  }

}
