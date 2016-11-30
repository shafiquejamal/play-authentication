package chat

import java.util.UUID

import com.google.inject.Inject
import contact.ChatContactDAO
import util.TimeProvider

import scala.util.{Failure, Try}

trait ChatContactAPI {

  def visibleContactsFor(userId: UUID): Seq[String]

  def addContact(forUserId: UUID, contactUserId: UUID): Try[UUID]

  def addContacts(forUserId: UUID, contactUserIds: Seq[UUID]): Seq[UUID]

}

class ChatContactFacade @Inject()(
    contactDAO: ChatContactDAO,
    timeProvider: TimeProvider) extends ChatContactAPI {

  override def visibleContactsFor(userId: UUID): Seq[String] =
    contactDAO.visibleContactsFor(userId).map(_.contactUsername)

  override def addContact(forUserId: UUID, contactUserId: UUID): Try[UUID] = {
    if (forUserId == contactUserId)
      Failure(new Exception("Cannot add self"))
    else
      contactDAO.addContact(forUserId, contactUserId, timeProvider.now())
  }

  override def addContacts(forUserId: UUID, contactUserIds: Seq[UUID]): Seq[UUID] =
    contactUserIds
    .filterNot(_ == forUserId)
    .flatMap { contactUserId => contactDAO.addContact(forUserId, contactUserId, timeProvider.now()).toOption }

}
