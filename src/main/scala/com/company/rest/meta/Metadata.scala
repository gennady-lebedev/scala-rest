package com.company.rest.meta

trait Metadata {
  def meta: EntityMeta
}

case class EntityMeta(entity: String, fields: Map[String, FieldMeta])
case class FieldMeta(id: String, key: Boolean, `type`: String, required: Boolean)