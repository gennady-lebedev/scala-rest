package com.company.rest.meta

import java.sql.{Date, Time, Timestamp}

import scala.reflect.runtime.universe._

trait MetaSupport[T <: Product] {
  implicit val ttag: TypeTag[T]

  def fields: Seq[TermSymbol] = {
    typeOf[T].members.collect {
      case m: TermSymbol if m.isVal => m
    }.toSeq.reverse
  }

  def fieldTypeMap: Map[String, (Type, Boolean)] = {
    fields.map { f =>
      if(f.typeSignature.typeSymbol.fullName == "scala.Option")
        (f.name.toString.trim, (f.typeSignature.typeArgs.head, false))
      else
        (f.name.toString.trim, (f.typeSignature, true))
    }.toMap
  }

  def fieldNames: Seq[String] = fields.map(_.name.toString.trim)

  def keys: Set[String] = Set.empty

  def meta: EntityMeta = {
    val f = fieldTypeMap.flatMap {
      case (name, (t, required)) =>
        val metaType =
          if (t =:= typeOf[String]) "string"
          else if (t =:= typeOf[Int] || t =:=  typeOf[Double] || t =:=  typeOf[Long] || t =:= typeOf[BigDecimal]) "number"
          else if (t =:= typeOf[Timestamp]) "timestamp"
          else if (t =:= typeOf[Date]) "date"
          else if (t =:= typeOf[Time]) "time"
          else "unknown"
        Some(name, FieldMeta(name, keys.contains(name), metaType, required))
      case _ => None
    }
    EntityMeta(typeOf[T].typeSymbol.name.toString.trim, f)
  }
}
