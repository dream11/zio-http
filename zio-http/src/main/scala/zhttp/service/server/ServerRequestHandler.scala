package zhttp.service.server

import io.netty.handler.codec.http.websocketx.{WebSocketServerHandshakerFactory => JWebSocketServerHandshakerFactory}
import zhttp.core._
import zhttp.http._
import zhttp.service._
import zio.Exit

/**
 * Helper class with channel methods
 */
@JSharable
final case class ServerRequestHandler[R, E: SilentResponse](
  zExec: UnsafeChannelExecutor[R],
  app: Http[R, E],
) extends JSimpleChannelInboundHandler[JFullHttpRequest](AUTO_RELEASE_REQUEST)
    with HttpMessageCodec
    with ServerHttpExceptionHandler {

  self =>

  /**
   * Tries to release the request byte buffer, ignores if it can not.
   */
  private def releaseOrIgnore(jReq: JFullHttpRequest): Boolean = jReq.release(jReq.content().refCnt())

  private def webSocketUpgrade(
    ctx: JChannelHandlerContext,
    jReq: JFullHttpRequest,
    res: Response.SocketResponse[R, E],
  ): Unit = {
    val settings = res.ss.settings
    val hh       = new JWebSocketServerHandshakerFactory(jReq.uri(), settings.subProtocol.orNull, false).newHandshaker(jReq)
    if (hh == null) {
      JWebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel, ctx.channel().voidPromise())
    } else {
      val pl = ctx.channel().pipeline()
      pl.addLast(WEB_SOCKET_HANDLER, ServerSocketHandler(zExec, settings))
      try {
        // handshake can throw
        hh.handshake(ctx.channel(), jReq).addListener { (future: JChannelFuture) =>
          // FAILURE CONDITION
          if (!future.isSuccess) {
            pl.remove(WEB_SOCKET_HANDLER)
            ctx.fireExceptionCaught(future.cause)
            zExec.unsafeExecute_(ctx)(settings.onError(future.cause()))
            ()
          } else {
            // SUCCESS
            pl.remove(HTTP_REQUEST_HANDLER)
            // TODO: handle channel closure on failure
            zExec.unsafeExecute_(ctx)(settings.onOpen(ctx.channel().remoteAddress()))
            ()
          }
        }
      } catch {
        case cause: JWebSocketHandshakeException =>
          JWebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel, ctx.channel().voidPromise())
          zExec.unsafeExecute_(ctx)(settings.onError(cause))
      }
    }
    ()
  }

  /**
   * Asynchronously executes the Http app and passes the response to the callback.
   */
  private def executeAsync(ctx: JChannelHandlerContext, jReq: JFullHttpRequest)(cb: Response[R, E] => Unit): Unit =
    decodeJRequest(jReq) match {
      case Left(err)  => cb(err.toResponse)
      case Right(req) =>
        app.eval(req) match {
          case HttpResult.Success(a)  => cb(a)
          case HttpResult.Failure(e)  => cb(implicitly[SilentResponse[E]].silent(e))
          case HttpResult.Continue(z) =>
            zExec.unsafeExecute(ctx, z) {
              case Exit.Success(res)   => cb(res)
              case Exit.Failure(cause) =>
                cause.failureOption match {
                  case Some(e) => cb(implicitly[SilentResponse[E]].silent(e))
                  case None    => ()
                }
            }
        }
    }

  /**
   * Unsafe channel reader for HttpRequest
   */
  override def channelRead0(ctx: JChannelHandlerContext, jReq: JFullHttpRequest): Unit = {
    executeAsync(ctx, jReq) {
      case res @ Response.HttpResponse(_, _, _) =>
        ctx.writeAndFlush(encodeResponse(jReq.protocolVersion(), res), ctx.channel().voidPromise())
        releaseOrIgnore(jReq)
        ()
      case res @ Response.SocketResponse(_) =>
        self.webSocketUpgrade(ctx, jReq, res)
        releaseOrIgnore(jReq)
        ()
    }
  }

  /**
   * Handles exceptions that throws
   */
  override def exceptionCaught(ctx: JChannelHandlerContext, cause: Throwable): Unit = {
    if (self.canThrowException(cause)) {
      super.exceptionCaught(ctx, cause)
    }
  }
}
