package zio-http.domain.http

import zio-http.domain.http.model.HttpError

sealed trait CanConcatenate[-E] {
  def is(e: E): Boolean
}

object CanConcatenate {
  implicit object ChainableHttpError extends CanConcatenate[HttpError] {
    override def is(e: HttpError): Boolean = e match {
      case HttpError.NotFound(_) => true
      case _                     => false
    }
  }
}
