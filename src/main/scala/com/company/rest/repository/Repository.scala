package com.company.rest.repository

import com.company.rest.meta.{Query, Result}

trait Repository[P <: Product, K <: AnyVal]

trait RoRepository[P <: Product, K <: AnyVal] extends Repository[P, K] {
  def find(query: Query[P]): Result[P]
  def count(query: Query[P]): Long
  def findById(id: K): Option[P]
  def get(id: K): P
}

trait WoRepository[P <: Product, K <: AnyVal] extends Repository[P, K] {
  def create(draft: P): P
  def update(entity: P): Unit
  def delete(entity: P): Unit
}

trait RwRepository[P <: Product, K <: AnyVal] extends RoRepository[P, K] with WoRepository[P, K]
