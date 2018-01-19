package com.company.rest.repository.sql

import com.company.rest.meta.MetaCompanion
import com.typesafe.scalalogging.LazyLogging

import scala.reflect.runtime.universe._

object BatchSqlBuilder {
  def apply[R <: Product : TypeTag]: BatchSqlBuilder[R] = new BatchSqlBuilder[R]
}

class BatchSqlBuilder[R <: Product : TypeTag]() extends LazyLogging {

  private lazy val universeMirror = runtimeMirror(getClass.getClassLoader)
  private lazy val companion = universeMirror.reflectModule(typeOf[R].typeSymbol.companion.asModule).instance.asInstanceOf[MetaCompanion[R]].meta

//  def insert(values: List[R]): BatchQuery = {
//    val columns = companion.columns.zipWithIndex
//    val bindings = columns.map("{" + _._1 + "}")
//
//    val q = BatchQuery(
//      s"INSERT INTO ${companion.table} " +
//      columns.map(_._1).mkString("(", ", ", ")") +
//      " VALUES " +
//      bindings.mkString("(", ", ", ")"),
//      values.map(v => columns.map(c => Binding(v.productElement(c._2))))
//    )
//    logger.debug("Insert query generated: {} with bindings\n{}", q.sql, q.bindings.map(s => s.map(b => b.value).mkString(", ")).mkString("\n"))
//    q
//  }
//
//  def truncate(): BoundQuery = {
//    val q = BoundQuery(s"TRUNCATE TABLE ${companion.table}")
//    logger.debug("Truncate query generated: {}", q.sql)
//    q
//  }
}
