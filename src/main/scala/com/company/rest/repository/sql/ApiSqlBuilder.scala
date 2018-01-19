package com.company.rest.repository.sql

import com.company.rest.meta._
import com.typesafe.scalalogging.LazyLogging

import scala.reflect.runtime.universe._

object ApiSqlBuilder {
  def apply[T <: Product : TypeTag](table: String) = new ApiSqlBuilder[T](BoundQuery(table), Query(Page(1,0)))
  def apply[T <: Product : TypeTag](table: String, query: Query[T]) = new ApiSqlBuilder[T](BoundQuery(table), query)
}

class ApiSqlBuilder[R <: Product : TypeTag](from: BoundQuery, query: Query[R]) extends LazyLogging {
  private val page: Page = query.page
  private val filters: Seq[Filter] = query.filter
  private val sort: Seq[Sort] = query.sort

  private lazy val universeMirror = runtimeMirror(getClass.getClassLoader)
  private lazy val companion = universeMirror.reflectModule(typeOf[R].typeSymbol.companion.asModule).instance.asInstanceOf[MetaCompanion[R]].meta
  private lazy val fieldsToColumns = companion.fieldsToColumns

  def select(): BoundQuery = {
    val select = buildSelect()
    val where = buildWhere()
    val orderBy = buildOrderBy()

    val q = BoundQuery(
      s"${(select ++ where ++ orderBy).sql} LIMIT {limit} OFFSET {offset}",
      (select ++ where ++ orderBy).bindings ++ List(Binding("limit", page.limit), Binding("offset", page.offset))
    )
    logger.debug("Select query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q
  }

  def count(): BoundQuery = {
    val q = buildCount() ++ buildWhere()
    logger.debug("Count query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q
  }

  private def buildSelect(): BoundQuery = BoundQuery(s"SELECT * FROM") ++ from
  private def buildCount(): BoundQuery = BoundQuery(s"SELECT count(1) total FROM") ++ from

  private def buildWhere(): BoundQuery = {
    if(filters.nonEmpty) {
      val parts = filters.map(wherePart)
      BoundQuery("WHERE " + parts.map(_.sql).mkString(" AND "), parts.flatMap(_.bindings))
    } else BoundQuery.empty
  }

  private def wherePart(f: Filter): BoundQuery = {
    val column = fieldsToColumns.getOrElse(f.field, throw new RuntimeException(s"Undefined field in $f"))
    val c = column
    f.condition match {
      case IsNull => BoundQuery(s"$c IS NULL")
      case NotNull => BoundQuery(s"$c IS NOT NULL")
      case Equals(b) => BoundQuery(s"$c = {$c}", c, b)
      case NotEquals(b) => BoundQuery(s"$c <> {$c}", c, b)
      case Greater(b) => BoundQuery(s"$c > {$c}", c, b)
      case GreaterOrEquals(b) => BoundQuery(s"$c >= {$c}", c, b)
      case Lesser(b) => BoundQuery(s"$c < {$c}", c, b)
      case LesserOrEquals(b) => BoundQuery(s"$c <= {$c}", c, b)
      case Between(a, b) =>
        BoundQuery(
          s"$c BETWEEN {${c + "_from"}} AND {${c + "_to"}}",
          Seq(
            Binding(c + "_from", a),
            Binding(c + "_to", b)
          )
        )
      case In(list) =>
        val indexed = list.zipWithIndex.map { l => (column + l._2, l._1)}.toMap
        BoundQuery(s"$c IN (${indexed.keys.map("{" + _ + "}").mkString(", ")})", indexed.map(i => Binding(i._1, i._2)).toList)
    }
  }

  private def buildOrderBy(): BoundQuery = {
    if(sort.nonEmpty)
      BoundQuery("ORDER BY " + sort.map(orderByPart).mkString(", "))
    else
      BoundQuery.empty
  }

  private def orderByPart(s: Sort): String = {
    val column = fieldsToColumns.getOrElse(s.field, throw new RuntimeException(s"Undefined field in $s"))
    if(s.order == Asc)
      column
    else
      s"$column DESC"
  }
}

