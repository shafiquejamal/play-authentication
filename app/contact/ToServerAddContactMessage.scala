package contact

import socket.ToServerSocketMessageType.ToServerAddContact
import socket.{SocketMessageType, ToServerAddContactOrContactsMessage}

case class ToServerAddContactMessage(usernameOfContactToAdd: String) extends ToServerAddContactOrContactsMessage {

  override def socketMessageType: SocketMessageType = ToServerAddContact

}
