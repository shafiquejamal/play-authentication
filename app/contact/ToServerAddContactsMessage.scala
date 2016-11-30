package contact

import socket.ToServerSocketMessageType.ToServerAddContacts
import socket.{SocketMessageType, ToServerAddContactOrContactsMessage}

case class ToServerAddContactsMessage(usernamesOfContactToAdd: Seq[String]) extends ToServerAddContactOrContactsMessage {

  override def socketMessageType: SocketMessageType = ToServerAddContacts

}
