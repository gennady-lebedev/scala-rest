package com.company.rest.meta

import java.sql.{Date, Time, Timestamp}

import scala.reflect.runtime.universe._

trait Filterable[T <: Product] extends MetaSupport[T] {
  def parseFilters(params: Map[String, List[String]]): Seq[Filter] = {
    fieldTypeMap.flatMap {
      case (name, (t, _)) if params.contains(name) =>
        val values =
          if (t =:= typeOf[String]) params(name)
          else if (t =:= typeOf[Int]) params(name).map(_.toInt)
          else if (t =:= typeOf[Long]) params(name).map(_.toLong)
          else if (t =:= typeOf[Double]) params(name).map(_.toDouble)
          else if (t =:= typeOf[BigDecimal]) params(name).map(BigDecimal.apply)
          else if (t =:= typeOf[Date]) params(name).map(Date.valueOf)
          else if (t =:= typeOf[Time]) params(name).map(Time.valueOf)
          else if (t =:= typeOf[Timestamp]) params(name).map(Timestamp.valueOf)
          else throw new RuntimeException("Unsupported type " + typeOf[T])
        Some(Filter(name, In(values)))
      case _ => None
    }.toList
  }
}
