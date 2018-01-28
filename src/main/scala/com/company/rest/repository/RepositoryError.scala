package com.company.rest.repository

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

abstract class RepositoryError
(
  val responseCode: Int,
  val message: String,
  val httpCode: StatusCode = StatusCodes.InternalServerError
) extends RuntimeException(message) {
  def response: ErrorResponse = ErrorResponse(responseCode, message)
}

case class ErrorResponse(code: Int, message: String)

class RepositoryItemNotFound(id: Any)
  extends RepositoryError(0, s"Element #$id not found", StatusCodes.NotFound)

class RepositoryItemNotCreated(value: Any)
  extends RepositoryError(1, s"Element $value failed on insert", StatusCodes.BadRequest)

class InvalidUpdateKey(oldValue: Any, newValue: Any)
  extends RepositoryError(2, s"Products $oldValue and $newValue have different key fields", StatusCodes.Conflict)

class NothingToUpdate
  extends RepositoryError(3, s"Nothing to update", StatusCodes.NoContent)

class ConflictOnDelete
  extends RepositoryError(4, s"Entity was changed", StatusCodes.Conflict)

// TODO make internal errors in separate file
class UnexpectedMultipleRows
  extends RepositoryError(4, "Expected single row, get multiple")

class UnknownBindingType(v: Any)
  extends RepositoryError(5, s"Can't bind value '$v' into SQL query")