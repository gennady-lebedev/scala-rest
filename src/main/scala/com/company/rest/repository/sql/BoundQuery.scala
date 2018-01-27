package com.company.rest.repository.sql

import scalikejdbc.{DBSession, NoExtractor, SQL, SQLBatch, WrappedResultSet}

class Binding(val name: String, val value: Any)
object Binding {
  def apply(name: String, value: Any): Binding = new Binding(
    name,
    value
  )
}

case class BoundQuery(sql: String, bindings: Seq[Binding]) {
  def ++(that: BoundQuery): BoundQuery = BoundQuery(
    this.sql + " " + that.sql,
    this.bindings ++ that.bindings
  )

  def toScalike: SQL[Nothing, NoExtractor] = {
    SQL(sql).bindByName(bindings.map(b => (Symbol(b.name), b.value)) :_*)
  }

  def map[T](f: WrappedResultSet => T)(implicit session: DBSession): Seq[T] = toScalike.map(f).list().apply()

  def single[T](f: WrappedResultSet => T)(implicit session: DBSession): Option[T] = toScalike.map(f).single().apply()

  def insert[T](draft: T)(implicit session: DBSession): Long = toScalike.updateAndReturnGeneratedKey("id").apply()

  def execute(implicit session: DBSession): Unit = toScalike.execute().apply()
}

object BoundQuery {
  val empty = BoundQuery("", Seq.empty)

  def apply(sql: String): BoundQuery = new BoundQuery(sql, Seq.empty)
  def apply(sql: String, binding: Binding): BoundQuery = new BoundQuery(sql, Seq(binding))
  def apply(sql: String, param: String, value: Any): BoundQuery = new BoundQuery(sql, Seq(Binding(param, value)))
}

case class BatchQuery(sql: String, bindings: Seq[Seq[Binding]]) {
  def toScalike: SQLBatch = {
    SQL(sql).batchByName(bindings.map(s => s.map(b => (Symbol(b.name), b.value))):_*)
  }
}