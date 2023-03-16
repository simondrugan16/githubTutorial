package service

import model.User
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import play.api.mvc.Result
import repository.DataRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class RepositoryService @Inject()(dataRepository: DataRepository){

  def create(user: User): Future[Either[Result, User]] =
    dataRepository.create(user)

  def read(login: String): Future[Either[Result, User]] =
    dataRepository.read(login)

  def update(login: String, user: User): Future[Either[Result, UpdateResult]] =
    dataRepository.update(login, user)

  def delete(login: String): Future[Either[Result, DeleteResult]] =
    dataRepository.delete(login)

}
