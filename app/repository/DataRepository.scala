package repository

import model.User
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRepository @Inject()(mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[User](
    collectionName = "githubUsers",
    mongoComponent = mongoComponent,
    domainFormat = User.formats,
    indexes = Seq(IndexModel(
      Indexes.ascending("username")
    )),
    replaceIndexes = false
  ) {

  private def byLogin(login: String): Bson =
    Filters.and(
      Filters.equal("login", login)
    )

  def create(user: User): Future[Either[Result, User]] = {
    collection.insertOne(user).headOption().flatMap{
      case Some(result) => Future(Right(user))
      case None => Future(Left(InternalServerError("ERROR: Could not create an item.")))
    }
  }

  def read(login: String): Future[Either[Result, User]] = {
    collection.find(byLogin(login)).headOption flatMap{
      case Some(value) => Future(Right(value))
      case None => Future(Left(InternalServerError("ERROR: Could not find item")))
    }
  }

  def update(login: String, user: User): Future[Either[Result, UpdateResult]] = {
    collection.replaceOne(
      filter = byLogin(login),
      replacement = user,
      options = new ReplaceOptions().upsert(true)
    ).headOption flatMap {
      case Some(result) => Future(Right(result))
      case None => Future(Left(InternalServerError("ERROR: Couldn't update database item.")))
    }
  }

  def delete(login: String): Future[Either[Result, DeleteResult]] = {
    collection.deleteOne(filter = byLogin(login)).headOption.flatMap{
      case Some(result) => Future(Right(result))
      case None => Future(Left(InternalServerError("ERROR: Couldn't delete database item.")))
    }
  }
}
