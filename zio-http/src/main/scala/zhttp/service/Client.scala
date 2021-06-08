package zhttp.service

import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.{HttpVersion => JHttpVersion}
import zhttp.core._
import zhttp.http._
import zhttp.service
import zhttp.service.client.{ClientChannelInitializer, ClientHttpChannelReader, ClientInboundHandler}
import zio.{Promise, Task, ZIO}

import java.net.InetSocketAddress

final case class Client(zx: UnsafeChannelExecutor[Any], cf: JChannelFactory[JChannel], el: JEventLoopGroup)
    extends HttpMessageCodec {
  private def asyncRequest(
    req: Request[Any, Nothing, Any],
    jReq: JFullHttpRequest,
    promise: Promise[Throwable, JFullHttpResponse],
  ): Task[Unit] =
    ChannelFuture.unit {
      val read = ClientHttpChannelReader(jReq, promise)
      val hand = ClientInboundHandler(zx, read)
      val init = ClientChannelInitializer(hand)
      val host = req.url.host
      val port = req.url.port.getOrElse(80) match {
        case -1   => 80
        case port => port
      }

      val jboo = new JBootstrap().channelFactory(cf).group(el).handler(init)
      if (host.isDefined) jboo.remoteAddress(new InetSocketAddress(host.get, port))

      jboo.connect()
    }

  def request(request: Request[Any, Nothing, Complete]): Task[Response[Any, Nothing, Complete]] = for {
    promise <- Promise.make[Throwable, JFullHttpResponse]
    jReq = encodeRequest(JHttpVersion.HTTP_1_1, request)
    _    <- asyncRequest(request, jReq, promise).catchAll(cause => promise.fail(cause)).fork
    jRes <- promise.await
    res  <- ZIO.fromEither(decodeJResponse(jRes))
  } yield res
}

object Client {
  def make: ZIO[EventLoopGroup with ChannelFactory, Nothing, Client] = for {
    cf <- ZIO.access[ChannelFactory](_.get)
    el <- ZIO.access[EventLoopGroup](_.get)
    zx <- UnsafeChannelExecutor.make[Any]
  } yield service.Client(zx, cf, el)

  def request(url: String): ZIO[EventLoopGroup with ChannelFactory, Throwable, UResponse] = for {
    url <- ZIO.fromEither(URL.fromString(url))
    res <- request(Method.GET -> url)
  } yield res

  def request(url: String, headers: List[Header]): ZIO[EventLoopGroup with ChannelFactory, Throwable, UResponse] =
    for {
      url <- ZIO.fromEither(URL.fromString(url))
      res <- request(Method.GET -> url, headers)
    } yield res

  def request(endpoint: Endpoint): ZIO[EventLoopGroup with ChannelFactory, Throwable, UResponse] =
    request(Request(endpoint, Nil, Content.fromByteBuf(Unpooled.EMPTY_BUFFER)))

  def request(
    endpoint: Endpoint,
    headers: List[Header],
  ): ZIO[EventLoopGroup with ChannelFactory, Throwable, UResponse] =
    request(Request(endpoint, headers, Content.fromByteBuf(Unpooled.EMPTY_BUFFER)))

  def request(
    req: Request[Any, Nothing, Complete],
  ): ZIO[EventLoopGroup with ChannelFactory, Throwable, Response[Any, Nothing, Complete]] =
    make.flatMap(_.request(req))
}
