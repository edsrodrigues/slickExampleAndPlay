package model

import play.api.data.Form
import play.api.data.Forms._

import scala.concurrent.{Await, Future}
import slick.driver.MySQLDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

case class User(id: Long, name: String, contact: String)

case class UserFormData(name: String, contact: String)

object UserForm {

  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "contact" -> nonEmptyText
    )(UserFormData.apply)(UserFormData.unapply)
  )
}

class customerTableDef(tag: Tag) extends Table[User](tag, "users") {

  def id = column[Long]("customer_id", O.PrimaryKey, O.AutoInc)

  def firstName = column[String]("name")

  def contact = column[String]("contact")

  override def * =
    (id, firstName, contact) <> (User.tupled, User.unapply)
}

object Users {

  val db = Database.forConfig("database")

  val users = TableQuery[customerTableDef]

  def addUser2(name: String, contact: String) = {
    db.run(users += User(0, name, contact))
  }

  def addUser(name: String, contact: String) = {
    Await.result(db.run(users += User(0, name, contact)), Duration.Inf)
  }

  def add(name: String, contact: String): Future[String] = {
    println("here")
    val a = db.run(users += User(0, name, contact)).map(res => "User successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }
    Await.result(db.run(users.result), Duration.Inf)
    a
  }

  def delete(id: Long): Future[Int] = {
    db.run(users.filter(_.id === id).delete)
  }

  def get(id: Long): Future[Option[User]] = {
    db.run(users.filter(_.id === id).result.headOption)
  }

  def listAll: Future[Seq[User]] = {
    db.run(users.result)
  }

}
