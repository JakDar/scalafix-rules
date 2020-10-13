/*
rule =OwnExample
 */
package test

import reactivemongo.api.DefaultDB
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}

class OwnExample(db: DefaultDB)(implicit ec: ExecutionContext) {
  private val users = db.collection[BSONCollection]("users")

  def register(olaId: String, alaId: String, userId: String): Future[Unit] =
    users
      .update(
        BSONDocument("olaId"        -> olaId, "alaId" -> alaId, "userId" -> userId),
        BSONDocument("$setOnInsert" -> BSONDocument( /*"createdAt" -> new Date(),*/ "instructionStatus" -> "exposed")),
        upsert = true
      )
      .map(_ => ())

  def exist(olaId: String, alaId: String, userId: String): Future[Boolean] =
    users
      .find(BSONDocument("olaId" -> olaId, "alaId" -> alaId, "userId" -> userId))
      .one[BSONDocument]
      .map(_.isDefined)

  def goodFind(olaId: String, alaId: String, userId: String): Future[Boolean] =
    users
      .find(BSONDocument("olaId" -> olaId, "alaId" -> alaId, "userId" -> userId), projection = Option.empty[BSONDocument])
      .one[BSONDocument]
      .map(_.isDefined)

  def remove(olaId: String, alaId: String, userId: String): Future[Unit] =
    users.remove(BSONDocument("olaId" -> olaId, "alaId" -> alaId, "userId" -> userId)).map(_ => ())

  def goodRemove(olaId: String, alaId: String, userId: String): Future[Unit] =
    users.delete.one(BSONDocument("olaId" -> olaId, "alaId" -> alaId, "userId" -> userId)).map(_ => ())

  def marketingStatus(olaId: String, alaId: String, userId: String): Future[Unit] =
    users
      .update(
        BSONDocument("olaId" -> olaId, "alaId" -> alaId, "userId" -> userId),
        BSONDocument("$set"  -> BSONDocument("marketingStatus" -> ""))
      )
      .map(_ => ())

  def goodUpdate(olaId: String, alaId: String, userId: String): Future[Unit] =
    users.update
      .one(
        BSONDocument("olaId" -> olaId, "alaId" -> alaId, "userId" -> userId),
        BSONDocument("$set"  -> BSONDocument("marketingStatus" -> ""))
      )
      .map(_ => ())

  def statementStatus(olaId: String, alaId: String, userId: String): Future[Unit] =
    users
      .update(
        BSONDocument("olaId" -> olaId, "alaId" -> alaId, "userId" -> userId),
        BSONDocument("$set"  -> BSONDocument("statementStatus" -> "ala")),
        multi = true
      )
      .map(_ => ())

  def insertMethod(olaId: String, alaId: String, userId: String): Future[Unit] =
    users
      .insert(
        BSONDocument("olaId" -> olaId, "alaId" -> alaId, "userId" -> userId)
      )
      .map(_ => ())
}

object OwnExample {
  import scala.util.Try
  import java.time.Month

  import java.util.Date
  import reactivemongo.api.bson.{BSONDateTime, BSONHandler, BSONValue}

  implicit object BSONDateTimeHandler extends BSONHandler[Date] {

    override def readTry(bson: BSONValue): Try[java.util.Date] = bson match {
      case dt: BSONDateTime => Try(new Date(dt.value))
      case other            => scala.util.Failure(new IllegalArgumentException(s"invalid bson for date $other"))
    }

    override def writeTry(t: java.util.Date): Try[BSONValue] = Try(BSONDateTime(t.getTime))
  }

  import reactivemongo.api.bson.{BSONDocumentReader, BSONDocumentWriter}
  implicit val menuItemHandler = new BSONDocumentReader[Month] with BSONDocumentWriter[Month] {
    override def writeTry(t: Month) =
      Try(t match {
        case Month.MAY   => throw new IllegalStateException("May not supported")
        case Month.APRIL => BSONDocument("type" -> "url", "caption" -> "ala", "url" -> "https://example.com")
        case _ =>
          BSONDocument("type" -> "other.months")
      })

    override def readDocument(bson: BSONDocument) =
      Try(bson.getAsOpt[String]("type").get match {
        case "url"    => Month.APRIL
        case "plugin" => Month.MAY
        case other    => throw new IllegalStateException(s"Menu item $other is not supported")
      })
  }
}
