package zhttp.service

import io.netty.channel.epoll.{Epoll => JEpoll}
import io.netty.incubator.channel.uring.{IOUring => JIOUring}

import io.netty.incubator.channel.uring.{IOUringEventLoopGroup => JIOUringEventLoopGroup};
import io.netty.{channel => jChannel}
import zio._

import java.util.concurrent.Executor

/**
 * Simple wrapper over NioEventLoopGroup
 */
object EventLoopGroup {
  def nio(nThreads: Int): ZLayer[Any, Nothing, EventLoopGroup] = EventLoopGroup.Live.nio(nThreads).toLayer

  def epoll(nThreads: Int): ZLayer[Any, Nothing, EventLoopGroup] = EventLoopGroup.Live.epoll(nThreads).toLayer

  def auto(nThreads: Int = 0): ZLayer[Any, Nothing, EventLoopGroup] = EventLoopGroup.Live.auto(nThreads).toLayer

  def default: ZLayer[Any, Nothing, EventLoopGroup] = EventLoopGroup.Live.default.toLayer

  object Live {
    def nio(nThreads: Int): ZManaged[Any, Nothing, jChannel.EventLoopGroup] =
      make(UIO(new jChannel.nio.NioEventLoopGroup(nThreads)))

    def nio(nThreads: Int, executor: Executor): ZManaged[Any, Nothing, jChannel.EventLoopGroup] =
      make(UIO(new jChannel.nio.NioEventLoopGroup(nThreads, executor)))

    def make(eventLoopGroup: UIO[jChannel.EventLoopGroup]): ZManaged[Any, Nothing, jChannel.EventLoopGroup] =
      eventLoopGroup.toManaged(ev => ChannelFuture.unit(ev.shutdownGracefully).orDie)

    def epoll(nThreads: Int): ZManaged[Any, Nothing, jChannel.EventLoopGroup] =
      make(UIO(new jChannel.epoll.EpollEventLoopGroup(nThreads)))

    def epoll(nThreads: Int, executor: Executor): ZManaged[Any, Nothing, jChannel.EventLoopGroup] =
      make(UIO(new jChannel.epoll.EpollEventLoopGroup(nThreads, executor)))

    def ioUring(nThreads: Int): ZManaged[Any, Nothing, jChannel.EventLoopGroup] =
      make(UIO(new JIOUringEventLoopGroup(nThreads)))

    def ioUring(nThreads: Int, executor: Executor): ZManaged[Any, Nothing, jChannel.EventLoopGroup] =
      make(UIO(new JIOUringEventLoopGroup(nThreads, executor)))

    def auto(nThreads: Int): ZManaged[Any, Nothing, jChannel.EventLoopGroup] =
      if (JIOUring.isAvailable) ioUring(nThreads) else if (JEpoll.isAvailable) epoll(nThreads) else nio(nThreads)

    def default: ZManaged[Any, Nothing, jChannel.EventLoopGroup] = make(UIO(new jChannel.DefaultEventLoopGroup()))
  }

}
