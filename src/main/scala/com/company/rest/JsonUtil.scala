package com.company.rest

import java.sql.Timestamp

import com.company.rest.meta.{Asc, Desc, Filter, Sort}
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

trait JsonUtil {
  implicit val timestampFormat: Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeString.apply(a.toString)
    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeString.map(Timestamp.valueOf).apply(c)
  }

  implicit val filterEncoder: Encoder[Filter] = new Encoder[Filter] {
    override def apply(a: Filter): Json = Encoder.encodeString.apply(a.toString)
  }

  implicit val sortEncoder: Encoder[Sort] = new Encoder[Sort] {
    override def apply(a: Sort): Json = Encoder.encodeString.apply(a.order match {
      case Desc => "!" + a.field
      case Asc => a.field
    })
  }
}
