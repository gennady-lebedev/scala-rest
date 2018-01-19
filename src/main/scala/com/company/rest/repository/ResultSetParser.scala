package com.company.rest.repository

import java.sql.{ResultSet, Timestamp}

object ResultSetParser {
  implicit def int(column: Symbol)(implicit rs: ResultSet): Int = rs.getInt(column.name)
  implicit def long(column: Symbol)(implicit rs: ResultSet): Long = rs.getLong(column.name)
  implicit def string(column: Symbol)(implicit rs: ResultSet): String = rs.getString(column.name)
  implicit def timestamp(column: Symbol)(implicit rs: ResultSet): Timestamp = rs.getTimestamp(column.name)

  implicit def intOpt(column: Symbol)(implicit rs: ResultSet): Option[Int] = Option(rs.getInt(column.name))
  implicit def longOpt(column: Symbol)(implicit rs: ResultSet): Option[Long] = Option(rs.getLong(column.name))
  implicit def stringOpt(column: Symbol)(implicit rs: ResultSet): Option[String] = Option(rs.getString(column.name))
  implicit def timestampOpt(column: Symbol)(implicit rs: ResultSet): Option[Timestamp] = Option(rs.getTimestamp(column.name))
}