package com.company.rest

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import com.company.rest.meta.Query
import com.company.rest.model.Item
import com.company.rest.repository.{ConflictOnDelete, NothingToUpdate, RepositoryError, RepositoryItemNotFound}
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.concurrent.ExecutionContext

class RootRouter(implicit executionContext: ExecutionContext) extends JsonUtil with LazyLogging {

  val routes: Route = handleExceptions(exceptionHandler){
    extractUri { uri =>
      extractMethod { method =>
        logger.debug("{} {}", method.value, uri.toRelative.path)
        ignoreTrailingSlash {
          healthRoute ~ itemRoutes ~ collectionRoutes
        }
      }
    }
  }

  private def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: RepositoryError => complete(e.httpCode, e.response)
    case e: Exception =>
      logger.error("Exception: ", e)
      complete(StatusCodes.InternalServerError)
  }

  private def healthRoute: Route = get {
    path("health") {
      complete(StatusCodes.OK)
    }
  }

  private def collectionRoutes: Route = path("item") {
    get {
      complete (StatusCodes.OK, AppContext.itemsRepository.find(new Query[Item]))
    } ~ post {
      entity(as[Item]) {
        draft => complete(StatusCodes.Created, AppContext.itemsRepository.create(draft))
      }
    }
  }

  private def itemRoutes: Route = path("item" / LongNumber) { id =>
    get {
      AppContext.itemsRepository.findById(id) match {
        case Some(found) => complete(StatusCodes.OK, found)
        case None => complete(StatusCodes.NotFound)
      }
    } ~ put {
      entity(as[Item]) { item =>
        AppContext.itemsRepository.findById(id) match {
          case Some(existing) if existing != item =>
            complete(StatusCodes.OK, AppContext.itemsRepository.update(item))
          case Some(existing) => throw new NothingToUpdate
          case None => throw new RepositoryItemNotFound(id)
        }
      }
    } ~ delete {
      entity(as[Item]) { item =>
        AppContext.itemsRepository.findById(id) match {
          case Some(existing) if existing == item =>
            complete(StatusCodes.NoContent, AppContext.itemsRepository.delete(item))
          case Some(existing) => throw new ConflictOnDelete
          case None => throw new RepositoryItemNotFound(id)
        }
      }
    }
  }
}
