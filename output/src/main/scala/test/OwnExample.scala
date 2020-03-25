package test

import reactivemongo.api.DefaultDB
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}

class OwnExample(db: DefaultDB)(implicit ec: ExecutionContext) {
  private val users = db.collection[BSONCollection]("users")

  def register(olaId: String, alaId: String, userId: String): Future[Unit] =
    users
      .update.one(
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
      .update.one(
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
      .update.one(
        BSONDocument("olaId" -> olaId, "alaId" -> alaId, "userId" -> userId),
        BSONDocument("$set"  -> BSONDocument("statementStatus" -> "ala")),
        multi = true
      )
      .map(_ => ())

}
