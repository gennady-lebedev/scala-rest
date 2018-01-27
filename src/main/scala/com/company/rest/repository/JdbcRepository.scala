package com.company.rest.repository

import com.company.rest.meta.{MetaCompanion, Query, RepositorySupport, Result}
import com.company.rest.repository.sql.{ApiSqlBuilder, ModifySqlBuilder}
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc.{AutoSession, DBSession}

import scala.reflect.runtime.universe._

object JdbcRepository {
  def apply[T <: Product : TypeTag](implicit session: DBSession = AutoSession): JdbcRepository[T] =
    new JdbcRepository[T]()
}

class JdbcRepository[T <: Product : TypeTag](implicit session: DBSession = AutoSession)
  extends RwRepository[T, Long] with LazyLogging {

  lazy val universeMirror: Mirror = runtimeMirror(getClass.getClassLoader)
  lazy val companion: RepositorySupport[T] = universeMirror
    .reflectModule(typeOf[T].typeSymbol.companion.asModule)
    .instance
    .asInstanceOf[MetaCompanion[T]].meta

  override def find(query: Query[T]): Result[T] = {
    val b = ApiSqlBuilder(companion.table, query)
    val result = b.select().map(rs => companion.parse(rs))
    val total = b.count().single(rs => rs.long("total")).get
    Result(result, query, total)
  }

  override def count(query: Query[T]): Long = {
    val b = ApiSqlBuilder(companion.table, query)
    b.count().single(_.long("total")).get
  }

  override def findById(id: Long): Option[T] =
    ModifySqlBuilder[T].select(id).single(companion.parse)

  override def get(id: Long): T = findById(id) match {
    case Some(v) => v
    case None => throw new RepositoryItemNotFound(id)
  }

  override def create(draft: T): T = {
    val id = ModifySqlBuilder[T]
      .insert(draft).insert(draft)
    get(id)
  }


  override def update(entity: T): T = {
    ModifySqlBuilder[T].select(entity).single(companion.parse) match {
      case Some(old) if old == entity => throw new NothingToUpdate
      case Some(old) =>
        ModifySqlBuilder[T].update(old, entity).execute
        ModifySqlBuilder[T].select(entity).single(companion.parse).get
      case None => throw new RepositoryItemNotFound(entity)
    }
  }

  override def delete(entity: T): Unit = {
    ModifySqlBuilder[T].select(entity).single(companion.parse) match {
      case Some(old) if old == entity => ModifySqlBuilder[T].delete(entity).execute
      case Some(old) => throw new ConflictOnDelete
      case None => throw new RepositoryItemNotFound(entity)
    }
  }
}
