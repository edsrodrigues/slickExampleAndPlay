package service

import model.{User, Users}
import scala.concurrent.Future

//not used

object UserService {

  def addUser(user: User): Future[String] = {
    Users.add(user.name, user.contact)
  }

  def deleteUser(id: Long): Future[Int] = {
    Users.delete(id)
  }

  def getUser(id: Long): Future[Option[User]] = {
    Users.get(id)
  }

  def listAllUsers: Future[Seq[User]] = {
    Users.listAll
  }
}
