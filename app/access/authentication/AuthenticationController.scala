package access.authentication

import access.authentication.AuthenticationMessage._
import access.authentication.EmailMessage._
import access.authentication.ResetPasswordMessage._
import access.{AuthenticatedActionCreator, JWTParamsProvider}
import akka.actor.{PoisonPill, ActorSystem}
import akka.stream.Materializer
import com.google.inject.Inject
import pdi.jwt.JwtJson
import play.Configuration
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._
import user.UserAPI
import user.UserStatus.{Active, Unverified}
import util.{TimeProvider, UUIDProvider}

import scala.util.{Failure, Success}

class AuthenticationController @Inject() (
    override val authenticationAPI: AuthenticationAPI,
    userAPI: UserAPI,
    override val jWTParamsProvider: JWTParamsProvider,
    uUIDProvider: UUIDProvider,
    passwordResetCodeSender: PasswordResetCodeSender,
    override val timeProvider: TimeProvider,
    override val configuration: Configuration,
    system: ActorSystem,
    materializer: Materializer)
  extends Controller
  with AuthenticatedActionCreator {

  def authenticate = Action(parse.json) { request =>
    request.body.validate[AuthenticationMessage] match {
      case success: JsSuccess[AuthenticationMessage] =>
        authenticationAPI.user(success.get).fold(Ok(Json.obj("status" -> "authentication failed"))) { user =>
          val claim =
            Json.obj(
              "userId" -> user.maybeId.getOrElse(uUIDProvider.randomUUID()).toString,
              "iat" -> timeProvider.now())
          val jWT = JwtJson.encode(claim, jWTParamsProvider.secretKey, jWTParamsProvider.algorithm)
          Ok(Json.obj("token" -> jWT, "email" -> user.email, "username" -> user.username))
        }
      case error: JsError =>
        Ok(Json.obj("status" -> "invalid data"))
    }
  }

  def logoutAllDevices = AuthenticatedAction(parse.json) { request =>
    authenticationAPI.logoutAllDevices(request.userId) match {
      case Success(_) =>
        system.actorSelection(s"/user/${request.userId}*") ! PoisonPill
        Ok(Json.obj("status" -> "success"))
      case Failure(failure) =>
        Ok(Json.obj("status" -> failure.getMessage))
    }
  }

  def sendPasswordResetLink() = Action(parse.json) { request =>
    request.body.validate[EmailMessage] match {
      case success:JsSuccess[EmailMessage] =>
        val maybeUser = userAPI.findByEmailLatest(success.get.email)
        maybeUser.fold[Unit](){ user =>
          user.userStatus match {
            case Active =>
              passwordResetCodeSender.send(user, request.host)
            case Unverified =>
            case _ =>
          }
        }
        Ok
      case error: JsError =>
       BadRequest
    }
  }

  def resetPassword() = Action(parse.json) { request =>
    request.body.validate[ResetPasswordMessage] match {
      case success:JsSuccess[ResetPasswordMessage] =>
        authenticationAPI.resetPassword(success.get.email, success.get.code, success.get.newPassword) match {
          case Success(user) =>
            Ok
          case _ =>
            BadRequest
        }
      case _ =>
        BadRequest
    }
  }
}
