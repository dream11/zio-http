package zhttp.service

import io.netty.channel.epoll.{Epoll => JEpoll}
import io.netty.channel.{embedded => jEmbedded, socket => jSocket}
import io.netty.incubator.channel.uring.{IOUringSocketChannel => JIOUringSocketChannel}
import io.netty.incubator.channel.uring.{IOUring => JIOUring}
import io.netty.{channel => jChannel}
import zio.{UIO, ZLayer}

object ChannelFactory {
  def nio: ZLayer[Any, Nothing, ChannelFactory] = Live.nio.toLayer

  def epoll: ZLayer[Any, Nothing, ChannelFactory] = Live.epoll.toLayer

  def embedded: ZLayer[Any, Nothing, ChannelFactory] = Live.embedded.toLayer

  object Live {
    def nio: UIO[jChannel.ChannelFactory[jChannel.Channel]] =
      UIO(() => new jSocket.nio.NioSocketChannel())

    def epoll: UIO[jChannel.ChannelFactory[jChannel.Channel]] =
      UIO(() => new jChannel.epoll.EpollSocketChannel())

    def uring: UIO[jChannel.ChannelFactory[jChannel.Channel]] =
      UIO(() => new JIOUringSocketChannel())

    def embedded: UIO[jChannel.ChannelFactory[jChannel.Channel]] =
      UIO(() => new jEmbedded.EmbeddedChannel(false, false))

    def auto: UIO[jChannel.ChannelFactory[jChannel.Channel]] =
      if (JIOUring.isAvailable) uring else if (JEpoll.isAvailable) epoll else nio
  }

}
