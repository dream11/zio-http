package zhttp.http

import zhttp.socket.{Socket, SocketProtocol}
import zio.ZIO

object Http {

  /**
   * Creates an Http app from a function that returns a ZIO
   */
  def fromEffectFunction[R, E](f: Request => ZIO[R, E, Response[R, E]]): Http[R, E] =
    HttpChannel.fromEffectFunction(f)

  /**
   * Converts a ZIO to an Http type
   */
  def responseM[R, E](res: ResponseM[R, E]): Http[R, E] = HttpChannel.fromEffect(res)

  /**
   * Creates an HTTP app which accepts a request and produces response.
   */
  def collect[R, E >: Throwable: PartialRequest](pf: PartialFunction[Request, Response[R, E]]): Http[R, E] =
    HttpChannel.collect[Request](pf)

  /**
   * Creates an HTTP app which accepts a requests and produces another Http app as response.
   */
  def collectM[R, E >: Throwable: PartialRequest](pf: PartialFunction[Request, ResponseM[R, E]]): Http[R, E] =
    HttpChannel.collectM[Request](pf)

  /**
   * Creates an HTTP app which always responds with the same plain text.
   */
  def text(str: String): Http[Any, Nothing] = HttpChannel.succeed(Response.text(str))

  /**
   * Creates an HTTP app which always responds with the same value.
   */
  def response[R, E](response: Response[R, E]): Http[R, E] = HttpChannel.succeed(response)

  /**
   * Creates an HTTP app which always responds with the same status code and empty data.
   */
  def empty(code: Status): Http[Any, Nothing] = HttpChannel.succeed(Response.http(code))

  /**
   * Creates an HTTP app which always fails with the same error type.
   */
  def error(error: HttpError): Http[Any, HttpError] = HttpChannel.fail(error)

  /**
   * Creates an HTTP app that fails with a NotFound exception.
   */
  def notFound: Http[Any, HttpError] =
    HttpChannel.fromEffectFunction(req => ZIO.fail(HttpError.NotFound(req.url.path)))

  /**
   * Creates an HTTP app which always responds with a 200 status code.
   */
  def ok: Http[Any, Nothing] = Http.empty(Status.OK)

  /**
   * Creates an HTTP app which accepts a requests and produces a websocket response.
   */
  def socket[R, E >: Throwable: PartialRequest](pf: PartialFunction[Request, Socket[R, E]]): Http[R, E] =
    HttpChannel.collect(pf).map(Response.socket)

  /**
   * Creates an HTTP app which accepts a requests and produces a websocket response for the provided sub-protocol,
   * effectfully.
   */
  def socketM[R, E >: Throwable: PartialRequest](subProtocol: String)(
    pf: PartialFunction[Request, ZIO[R, E, Socket[R, E]]],
  ): Http[R, E] = {
    val protocol = Socket.protocol(SocketProtocol.subProtocol(subProtocol))
    HttpChannel
      .collectM(pf)
      .map(socket => Response.socket(socket ++ protocol))
  }

  /**
   * Creates an HTTP app which accepts a requests and produces a websocket response effectfully.
   */
  def socketM[R, E >: Throwable: PartialRequest](
                                                  pf: PartialFunction[Request, ZIO[R, E, Socket[R, E]]],
  ): Http[R, E] = HttpChannel.collectM(pf).map(Response.socket)
}
