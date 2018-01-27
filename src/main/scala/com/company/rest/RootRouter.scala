package com.company.rest

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.company.rest.meta.Query
import com.company.rest.model.Item
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.concurrent.ExecutionContext

class RootRouter(implicit executionContext: ExecutionContext) extends JsonUtil with LazyLogging {

  val routes: Route = extractUri { uri =>
    extractMethod { method =>
      logger.debug("{} {}", method.value, uri.toRelative.path)
      ignoreTrailingSlash {
        get {
          path("health") {
            complete(StatusCodes.OK)
          }
        } ~ path("item" / LongNumber) { id =>
          get {
            AppContext.itemsRepository.findById(id) match {
              case Some(found) => complete(StatusCodes.OK, found)
              case None => complete(StatusCodes.NotFound)
            }
          } ~ put {
            entity(as[Item]) { item =>
              complete(StatusCodes.OK, AppContext.itemsRepository.update(item))
            }
          } ~ delete {
            entity(as[Item]) { item =>
              complete(StatusCodes.NoContent, AppContext.itemsRepository.delete(item))
            }
          }
        } ~ path("item") {
          get {
            complete(StatusCodes.OK, AppContext.itemsRepository.find(new Query[Item]))
          } ~ post {
            entity(as[Item]) { draft =>
              complete(StatusCodes.Created, AppContext.itemsRepository.create(draft))
            }
          }
        }
      }
    }
  }
}
