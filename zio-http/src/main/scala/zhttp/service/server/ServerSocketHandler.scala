package zhttp.service.server

import zhttp.core.{JChannelHandlerContext, JSimpleChannelInboundHandler, JWebSocketFrame}
import zhttp.service.{ChannelFuture, UnsafeChannelExecutor}
import zhttp.socket.{SocketBuilder, WebSocketFrame}
import zio.{Exit, ZIO}

/**
 * Creates a new websocket handler
 */
final case class ServerSocketHandler[R](
  zExec: UnsafeChannelExecutor[R],
  ss: SocketBuilder.Settings[R, Throwable],
) extends JSimpleChannelInboundHandler[JWebSocketFrame] {

  /**
   * Unsafe channel reader for WSFrame
   */

  override def channelRead0(ctx: JChannelHandlerContext, msg: JWebSocketFrame): Unit = {
    WebSocketFrame.fromJFrame(msg) match {
      case Some(frame) =>
        executeAsync(
          ctx,
          ss.onMessage(frame)
            .mapM(frame => ChannelFuture.unit(ctx.writeAndFlush(frame.toJWebSocketFrame)))
            .runDrain,
        )

      case _ => ()
    }
  }

  def executeAsync(ctx: JChannelHandlerContext, program: ZIO[R, Throwable, Unit]): Unit = {
    zExec.unsafeExecute(ctx, program) {
      case Exit.Success(_)     => ()
      case Exit.Failure(cause) =>
        cause.failureOption match {
          case Some(error: Throwable) => ctx.fireExceptionCaught(error)
          case _                      => ()
        }
        ctx.close()
        ()
    }
  }

  override def exceptionCaught(ctx: JChannelHandlerContext, x: Throwable): Unit =
    executeAsync(ctx, ss.onError(x).uninterruptible)

  override def channelUnregistered(ctx: JChannelHandlerContext): Unit =
    executeAsync(ctx, ss.onClose(ctx.channel().remoteAddress(), None).uninterruptible)
}
