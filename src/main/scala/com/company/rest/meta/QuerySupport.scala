package com.company.rest.meta

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{entity, as}
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshaller}
import com.typesafe.scalalogging.LazyLogging

import scala.reflect.runtime.universe._

abstract class QuerySupport[T <: Product : TypeTag] extends LazyLogging {
  lazy val universeMirror: Mirror = runtimeMirror(getClass.getClassLoader)
  lazy val companion: RepositorySupport[T] = universeMirror
    .reflectModule(typeOf[T].typeSymbol.companion.asModule)
    .instance
    .asInstanceOf[MetaCompanion[T]].meta

  implicit def queryUnmarshaller: FromRequestUnmarshaller[Query[T]] = Unmarshaller.strict { ctx =>
    val params = ctx.uri.query().toMap
    val page = Page(params.getOrElse("limit", "100").toInt, params.getOrElse("offset", "0").toInt)
    val sort = params.get("sort") match {
      case Some(param) =>
        companion.parseSorting(param)
      case _ => List.empty[Sort]
    }
    val filters = companion.parseFilters(ctx.uri.query().toMultiMap)
    Query(page, filters, sort)
  }

  def resourceQuery: Directive1[Query[T]] = entity(as[Query[T]])
}
