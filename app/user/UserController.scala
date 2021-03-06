package user

import javax.inject.Inject

import access.authentication.AuthenticationAPI
import access.{AuthenticatedActionCreator, JWTParamsProvider}
import play.Configuration
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.{Action, Controller, Result}
import util.TimeProvider

class UserController @Inject()(
    override val authenticationAPI: AuthenticationAPI,
    override val jWTParamsProvider: JWTParamsProvider,
    override val timeProvider: TimeProvider,
    override val configuration: Configuration,
    userAPI:UserAPI
  )
  extends Controller
  with AuthenticatedActionCreator {

  def changePassword() = AuthenticatedAction(parse.json) { request =>
    request.body.validate[ChangePasswordMessage] match {
      case successChangePasswordMessage:JsSuccess[ChangePasswordMessage] =>
        userAPI.changePassword(request.userId, successChangePasswordMessage.get)
        .toOption.fold[Result](Ok(Json.obj("status" -> "password change failed")))( user =>
          Ok(Json.obj("status" -> "success"))
        )
      case _ =>
        Ok(Json.obj("status" -> "invalid data"))
    }
  }

  def unauthorized = Action {
    Unauthorized
  }

}
