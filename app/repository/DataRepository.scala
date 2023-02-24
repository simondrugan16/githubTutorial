package repository

import model.User
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.Inject
import scala.concurrent.ExecutionContext

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

}
