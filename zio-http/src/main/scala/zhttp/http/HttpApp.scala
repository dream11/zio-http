package zhttp.http

import zio.ZIO

object HttpApp {

  /**
   * Creates an Http app from a function that returns a ZIO
   */
  def fromEffectFunction[R, E](f: Request => ZIO[R, E, Response[R, E]]): HttpApp[R, E] =
    Http.fromEffectFunction(f)

  /**
   * Converts a ZIO to an Http type
   */
  def responseM[R, E](res: ResponseM[R, E]): HttpApp[R, E] = Http.fromEffect(res)

  /**
   * Creates an HTTP app which accepts a request and produces response.
   */
  def collect[R, E >: Throwable: PartialRequest](pf: PartialFunction[Request, Response[R, E]]): HttpApp[R, E] =
    Http.collect[Request](pf)

  /**
   * Creates an HTTP app which accepts a requests and produces another Http app as response.
   */
  def collectM[R, E >: Throwable: PartialRequest](pf: PartialFunction[Request, ResponseM[R, E]]): HttpApp[R, E] =
    Http.collectM[Request](pf)

  /**
   * Creates an HTTP app which always responds with the same plain text.
   */
  def text(str: String): HttpApp[Any, Nothing] = Http.succeed(Response.text(str))

  /**
   * Creates an HTTP app which always responds with the same value.
   */
  def response[R, E](response: Response[R, E]): HttpApp[R, E] = Http.succeed(response)

  /**
   * Creates an HTTP app which always responds with the same status code and empty data.
   */
  def empty(code: Status): HttpApp[Any, Nothing] = Http.succeed(Response.http(code))

  /**
   * Creates an HTTP app which always fails with the same error type.
   */
  def error(error: HttpError): HttpApp[Any, HttpError] = Http.fail(error)

  /**
   * Creates an HTTP app that fails with a NotFound exception.
   */
  def notFound: HttpApp[Any, HttpError] =
    Http.fromEffectFunction(req => ZIO.fail(HttpError.NotFound(req.url.path)))

  /**
   * Creates an HTTP app which always responds with a 200 status code.
   */
  def ok: HttpApp[Any, Nothing] = HttpApp.empty(Status.OK)
}
