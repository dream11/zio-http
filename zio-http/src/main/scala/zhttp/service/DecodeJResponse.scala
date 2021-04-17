package zhttp.service

import zhttp.core.JFullHttpResponse
import zhttp.http._
import zio.Chunk

import scala.util.Try

trait DecodeJResponse {

  /**
   * Tries to decode netty request into ZIO Http Request
   */
  def decodeJResponse(jRes: JFullHttpResponse): Either[Throwable, UHttpResponse] = Try {
    val status  = Status.fromJHttpResponseStatus(jRes.status())
    val headers = Header.parse(jRes.headers())
    val bytes   = new Array[Byte](jRes.content().readableBytes)
    jRes.content().readBytes(bytes)
    val content = HttpContent.Complete(Chunk.fromArray(bytes))

    Response.http(status, headers, content): UHttpResponse
  }.toEither
}
