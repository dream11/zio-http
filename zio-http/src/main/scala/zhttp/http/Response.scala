package zhttp.http

import zhttp.socket.{SocketBuilder, WebSocket}

import java.io.{PrintWriter, StringWriter}

// RESPONSE
sealed trait Response[-R, +E] extends Product with Serializable { self => }

object Response {
  private val defaultStatus  = Status.OK
  private val defaultHeaders = Nil
  private val emptyContent   = HttpContent.Complete("")

  // Constructors
  final case class HttpResponse[R](status: Status, headers: List[Header], content: HttpContent[R, String])
      extends Response[R, Nothing]

  final case class SocketResponse[R, E](ss: SocketBuilder[R, E]) extends Response[R, E]

  // Helpers

  /**
   * Creates a new Http Response
   */
  def http[R](
    status: Status = defaultStatus,
    headers: List[Header] = defaultHeaders,
    content: HttpContent[R, String] = emptyContent,
  ): Response.HttpResponse[R] =
    HttpResponse(status, headers, content)

  /**
   * Creates a new WebSocket Response with a sub-protocol
   */
  def socket[R, E](ss: SocketBuilder[R, E]): Response[R, E] = SocketResponse(ss)

  /**
   * Creates a new WebSocket Response
   */
  def socket[R, E](s: WebSocket[R, E]): Response[R, E] = socket(SocketBuilder.message(s.asStream(_)))

  def fromHttpError(error: HttpError): UResponse = {
    error match {
      case cause: HTTPErrorWithCause =>
        http(
          error.status,
          Nil,
          HttpContent.Complete(cause.cause match {
            case Some(throwable) =>
              val sw = new StringWriter
              throwable.printStackTrace(new PrintWriter(sw))
              s"${cause.message}:\n${sw.toString}"
            case None            => s"${cause.message}"
          }),
        )
      case _                         => http(error.status, Nil, HttpContent.Complete(error.message))
    }

  }

  def ok: UResponse = http(Status.OK)

  def text(text: String): UResponse =
    http(
      content = HttpContent.Complete(text),
      headers = List(Header.contentTypeTextPlain),
    )

  def jsonString(data: String): UResponse =
    http(
      content = HttpContent.Complete(data),
      headers = List(Header.contentTypeJson),
    )

  def status(status: Status): UResponse = http(status)
}
