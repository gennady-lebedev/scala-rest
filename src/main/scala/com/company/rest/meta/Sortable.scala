package com.company.rest.meta

trait Sortable[T <: Product] extends MetaSupport[T] {
  def parseSorting(param: String): List[Sort] = {
    val names = param.split(",")
    names.flatMap { n =>
      val order = if(n.startsWith("!")) Desc else Asc
      val withoutPrefix = if(n.startsWith("!")) n.drop(1) else n
      if(fieldNames.contains(withoutPrefix))
        Some(Sort(withoutPrefix, order))
      else
        None
    }.toList
  }
}
