package models

import java.sql.{Timestamp}
import java.text.{DateFormat, SimpleDateFormat}

import slick.driver.MySQLDriver.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import scala.concurrent.ExecutionContext.Implicits.global

//This example only demonstrates slick!!
//CREATED BY EDUARDO RODRIGUES

object test {
  def main(args: Array[String]): Unit = {

    val db = Database.forConfig("database")

    val customers: TableQuery[Customers] = TableQuery[Customers]
    val subscriptions: TableQuery[Subscriptions] = TableQuery[Subscriptions]
    val campaigns: TableQuery[Campaigns] = TableQuery[Campaigns]
    val products: TableQuery[Products] = TableQuery[Products]

    val populate: DBIO[Unit] = DBIO.seq(
      // Insert some suppliers
      customers += (0, "Valter", "910000000"),
      customers += (0, "Ricardo", "911111111"),
      customers += (0, "Ivo", "912222222"),
      customers += (0, "João", "913333333"),
      customers += (0, "Miguel", "914444444"),
      customers += (0, "Maria", "915555555"),
      products += (0, "Iphone6", 500),
      products += (0, "Iphone7", 700),
      products += (0, "S6", 600),
      campaigns += (0, "PrimaveraQuente", DateConversion.getTime(23, 3, 2017, 0, 0, 0), DateConversion.getTime(21, 6, 2017, 23, 59, 59), 2),
      campaigns += (0, "VeraoQuente", DateConversion.getTime(21, 6, 2017, 0, 0, 0), DateConversion.getTime(23, 3, 2017, 23, 59, 59), 3),
      campaigns += (0, "InvernoGelado", DateConversion.getTime(23, 10, 2017, 0, 0, 0), DateConversion.getTime(31, 12, 2017, 23, 59, 59), 1),
      subscriptions += (0, 1, DateConversion.getTime(30, 1, 2017, 0, 0, 0), DateConversion.getTime(30, 12, 2017, 23, 59, 59), 1),
      subscriptions += (0, 1, DateConversion.getTime(30, 1, 2017, 0, 0, 0), DateConversion.getTime(30, 12, 2017, 23, 59, 59), 2),
      subscriptions += (0, 2, DateConversion.getTime(30, 1, 2017, 0, 0, 0), DateConversion.getTime(30, 12, 2017, 23, 59, 59), 3),
      subscriptions += (0, 4, DateConversion.getTime(30, 1, 2017, 0, 0, 0), DateConversion.getTime(30, 12, 2017, 23, 59, 59), 1)
    )

    val cleanAuto: DBIO[Unit] = DBIO.seq(

      //should use this order, TRUST ME
      subscriptions.delete,
      customers.delete,
      campaigns.delete,
      products.delete,
      sqlu"""alter table campaigns AUTO_INCREMENT = 0""",
      sqlu"""alter table subscriptions AUTO_INCREMENT = 0""",
      sqlu"""alter table products AUTO_INCREMENT = 0""",
      sqlu"""alter table customers AUTO_INCREMENT = 0"""
    )

    print("Cleaning database\t")
    //if you want to clean all the tables before you start
    Await.result(db.run(cleanAuto), Duration.Inf)
    println("✓")
    print("Populating database\t")
    //if you want to populate database
    Await.result(db.run(populate), Duration.Inf)
    println("✓")


    //QUERIES AVAILABLE

    //1 - List All Customers

    val names: DBIO[Seq[(Long, String, String)]] = customers.result

    Await.result(db.run(names.map(println)), Duration.Inf)


    //2 - Sort by column
    val customersSorted: Query[Customers, (Long, String, String), Seq] = customers.sortBy(_.customer_id)

    Await.result(db.run(customersSorted.result.map(println)), Duration.Inf)

    println(customersSorted.result.statements)

    //3 - Nomes dos users cujo id é maior que 3
    val biggerCustomers: Query[Rep[String], String, Seq] = customers.filter(_.customer_id > 3L).map(_.name)

    Await.result(db.run(biggerCustomers.result.map(println)), Duration.Inf)

    println(biggerCustomers.result.statements)


    //4 - Names of users that has a subscription
    val usersWithSubs: Query[Rep[String], String, Seq] = for {
      (customer, subs) <- customers join subscriptions if customer.customer_id === subs.c_id
    } yield customer.name

    Await.result(db.run(usersWithSubs.result.map(println)), Duration.Inf)

    println(usersWithSubs.result.statements)

    //5 - Products for users


  }
}

class Customers(tag: Tag)
  extends Table[(Long, String, String)](tag, "customers") {

  def customer_id: Rep[Long] = column[Long]("customer_id", O.PrimaryKey, O.AutoInc)

  def name: Rep[String] = column[String]("name")

  def contact: Rep[String] = column[String]("contact")


  def * : ProvenShape[(Long, String, String)] =
    (customer_id, name, contact)

}

object DateConversion {
  def getTime(day: Int, month: Int, year: Int, hour: Int, min: Int, sec: Int): Timestamp = {
    val dateFormat: DateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    val date: java.util.Date = dateFormat.parse(s"$day/$month/$year $hour:$min:$sec")
    new Timestamp(date.getTime)
  }

}

class Products(tag: Tag)
  extends Table[(Long, String, Int)](tag, "Products") {

  def product_id: Rep[Long] = column[Long]("PRODUCT_ID", O.PrimaryKey, O.AutoInc)

  def name: Rep[String] = column[String]("PRODUCT_NAME")

  def price: Rep[Int] = column[Int]("PRICE")


  def * : ProvenShape[(Long, String, Int)] =
    (product_id, name, price)

}

class Subscriptions(tag: Tag)
  extends Table[(Long, Long, Timestamp, Timestamp, Long)](tag, "Subscriptions") {

  def subs_id: Rep[Long] = column[Long]("subs_id", O.PrimaryKey, O.AutoInc)

  def c_id: Rep[Long] = column[Long]("customer_id")

  def start_date: Rep[Timestamp] = column[Timestamp]("start_date")

  def expiry_date: Rep[Timestamp] = column[Timestamp]("end_date")

  def campaign_id: Rep[Long] = column[Long]("campaign_id")

  // A reified foreign key relation that can be navigated to create a join
  def customer_id: ForeignKeyQuery[Customers, (Long, String, String)] =
    foreignKey("CUSTOMER_ID", c_id, TableQuery[Customers])(_.customer_id)

  def * : ProvenShape[(Long, Long, Timestamp, Timestamp, Long)] =
    (subs_id, c_id, start_date, expiry_date, campaign_id)
}

class Campaigns(tag: Tag)
  extends Table[(Long, String, Timestamp, Timestamp, Long)](tag, "campaigns") {

  def campaign_id: Rep[Long] = column[Long]("campaign_id", O.PrimaryKey, O.AutoInc)

  def campaign_name: Rep[String] = column[String]("campaign_name")

  def start_date: Rep[Timestamp] = column[Timestamp]("camp_start_date")

  def expiry_date: Rep[Timestamp] = column[Timestamp]("camp_end_date")

  def p_id: Rep[Long] = column[Long]("product_id")

  // A reified foreign key relation that can be navigated to create a join
  def product_id: ForeignKeyQuery[Products, (Long, String, Int)] =
    foreignKey("PRODUCT_ID", p_id, TableQuery[Products])(_.product_id)

  def * : ProvenShape[(Long, String, Timestamp, Timestamp, Long)] =
    (campaign_id, campaign_name, start_date, expiry_date, p_id)

}
