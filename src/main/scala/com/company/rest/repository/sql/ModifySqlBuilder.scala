package com.company.rest.repository.sql

import com.company.rest.meta._
import com.company.rest.repository.{InvalidUpdateKey, NothingToUpdate}
import com.typesafe.scalalogging.LazyLogging

import scala.reflect.runtime.universe._

object ModifySqlBuilder {
  def apply[R <: Product : TypeTag]: ModifySqlBuilder[R] = new ModifySqlBuilder[R]
}

class ModifySqlBuilder[R <: Product : TypeTag] extends LazyLogging {
  private lazy val universeMirror = runtimeMirror(getClass.getClassLoader)
  private lazy val companion = universeMirror.reflectModule(typeOf[R].typeSymbol.companion.asModule).instance.asInstanceOf[MetaCompanion[R]].meta

  def select(id: Long): BoundQuery = {
    val key = companion.columns.zipWithIndex.filter(c => companion.keyColumn.contains(c._1)).head
    val q = BoundQuery(s"SELECT * FROM ${companion.table} WHERE ${key._1}={${key._1}}", key._1, id)
    logger.debug("Select query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q
  }

  def select(item: R): BoundQuery = {
    val key = companion.columns.zipWithIndex.filter(c => companion.keyColumn.contains(c._1)).head
    val q = BoundQuery(s"SELECT * FROM ${companion.table} WHERE ${key._1}={${key._1}}", key._1, item.productElement(key._2))
    logger.debug("Select query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q
  }

  def insert(value: R): BoundQuery = {
    val columns = companion.columns.zipWithIndex.filterNot(c => companion.keys.contains(c._1))
    val bindings = columns.map("{" + _._1 + "}")

    val q = BoundQuery(s"INSERT INTO ${companion.table}") ++ BoundQuery(
      columns.map(_._1).mkString("(", ", ", ")") + " VALUES " + bindings.mkString("(", ", ", ")"),
      columns.map(c => Binding(c._1, value.productElement(c._2)))
    )
    logger.debug("Insert query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q
  }

  def update(oldValue: R, newValue: R): BoundQuery = {
    val key = companion.columns.zipWithIndex.filter(c => companion.keyColumn.contains(c._1)).head
    if (oldValue.productElement(key._2) != newValue.productElement(key._2))
      throw new InvalidUpdateKey(oldValue, newValue)

    val keyValue = newValue.productElement(key._2)

    val columns = companion.columns
      .zipWithIndex
      .filterNot(c => companion.keyColumn.contains(c._1))
      .filterNot(c => oldValue.productElement(c._2) == newValue.productElement(c._2))

    if (columns.isEmpty) throw new NothingToUpdate()

    val q = BoundQuery(s"UPDATE ${companion.table}") ++ BoundQuery(
      "SET " + columns.map(c => c._1 + "={" + c._1 + "}").mkString(","),
      columns.map(c => Binding(c._1, newValue.productElement(c._2)))
    ) ++ BoundQuery(s" WHERE ${key._1}={${key._1}}", key._1, keyValue)
    logger.debug("Update query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q
  }

  def delete(item: R): BoundQuery = {
    val key = companion.columns.zipWithIndex.filter(c => companion.keyColumn.contains(c._1)).head
    val q = BoundQuery(s"DELETE FROM ${companion.table} WHERE ${key._1} = {key}", "key", item.productElement(key._2))
    logger.debug("Delete query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q
  }
}
