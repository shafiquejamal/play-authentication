package communication

import java.net.URLEncoder._

import com.google.inject.Inject
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import user.UserMessage

class LinkSenderImpl @Inject()(
    emailer: Emailer,
    configuration:Configuration,
    override val messagesApi: MessagesApi)
  extends LinkSender
  with I18nSupport {

  def send(
      user:UserMessage,
      host:String,
      code:String,
      route:String,
      subjectMessageKey:String,
      bodyTextMessageKey:String):Unit = {

    val protocolAndHost = configuration.getString("crauth.protocol").getOrElse("http") + "://" + host
    val urlEncodedEmailAddress = encode(user.email, "UTF-8")
    val link = s"$protocolAndHost/#/$route?email=$urlEncodedEmailAddress&code=$code"

    val from = configuration.getString("crauth.emailFrom").getOrElse("")
    val subject = Messages(subjectMessageKey)
    val bodyText = Messages(bodyTextMessageKey:String, link)

    emailer.sendEmail(
      subject,
      from,
      Seq(s"${user.username} <${user.email}>"),
      Some(bodyText))

  }
  
}
