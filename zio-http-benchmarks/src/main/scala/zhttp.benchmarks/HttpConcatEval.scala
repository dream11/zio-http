package zhttp.benchmarks

import org.openjdk.jmh.annotations._
import zhttp.http._

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class HttpConcatEval {

  private val MAX = 1_000

  val app: Http[Any, String, Int, String] = Http.collect[Int]({ case 0 => "A" })
  val spec                                = (0 to MAX).foldLeft(app)((a, _) => a <> app)

  @Benchmark
  def benchmarkHttpFlatMap(): Unit = {
    spec.eval(-1)
    ()
  }
}
