package com.company.rest.meta

import java.sql.{Date, Time, Timestamp}

import scalikejdbc.WrappedResultSet

import scala.reflect.runtime._
import scala.reflect.runtime.universe._

trait DbSupport[T <: Product] extends MetaSupport[T] {
  def table: String = typeOf[T].typeSymbol.name.toString.trim

  def sqlStatement: String = s"SELECT * FROM $table"

  def columns: Seq[String] = fieldNames.map(camelToSnake)

  def fieldsToColumns: Map[String, String] = fieldNames.zip(columns).toMap

  def keyColumn: String = fieldsToColumns(keys.head)

  // do not use until naming in spark fixed
  def camelToSnake(s: String): String = {
    val draft = "[A-Z]".r.replaceAllIn(s, { m => "_" + m.group(0).toLowerCase() })
    if(draft.startsWith("_"))
      draft.drop(1)
    else
      draft
  }

  def parse(rs: WrappedResultSet): T = {
    val args = fieldNames.map { name =>
      val (t, required) = fieldTypeMap(name)
      val column = fieldsToColumns(name)

      if (t =:= typeOf[String]) if(required) rs.string(column) else  rs.stringOpt(column)
      else if (t =:= typeOf[Int]) if(required) rs.int(column) else  rs.intOpt(column)
      else if (t =:= typeOf[Long]) if(required) rs.long(column) else  rs.longOpt(column)
      else if (t =:= typeOf[Double]) if(required) rs.double(column) else  rs.doubleOpt(column)
      else if (t =:= typeOf[BigDecimal]) if(required) rs.bigDecimal(column) else  rs.bigDecimalOpt(column)
      else if (t =:= typeOf[Date]) if(required) rs.date(column) else  rs.dateOpt(column)
      else if (t =:= typeOf[Time]) if(required) rs.time(column) else  rs.timeOpt(column)
      else if (t =:= typeOf[Timestamp]) if(required) rs.timestamp(column) else  rs.timestampOpt(column)
      else throw new RuntimeException(s"Unsupported type ${typeOf[T]} of field $name")
    }

    val tt = typeTag[T]

    currentMirror.reflectClass(tt.tpe.typeSymbol.asClass).reflectConstructor(
      tt.tpe.members.filter(m =>
        m.isMethod && m.asMethod.isConstructor
      ).iterator.next.asMethod
    )(args:_*).asInstanceOf[T]
  }
}

abstract class RepositorySupport[T <: Product : TypeTag] extends DbSupport[T] with Filterable[T] with Sortable[T] {
  override val ttag: TypeTag[T] = typeTag[T]
}

trait MetaCompanion[T <: Product] {
  def meta: RepositorySupport[T]
}