package pl.kaamara.httpapi

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.LongAdder
import scala.jdk.CollectionConverters._

final class MetricsRegistry {

  private val requests = new ConcurrentHashMap[String, LongAdder]()

  def record(method: String, path: String, status: Int): Unit = {
    val metricPath = if (status == 404) "/{unmatched}" else path
    val labels     = s"""method="$method",path="$metricPath",status="$status""""
    requests.computeIfAbsent(labels, _ => new LongAdder).increment()
  }

  def render(counterValue: Long): String = {
    val requestLines = requests.asScala.map { case (labels, count) =>
      s"http_requests_total{$labels} ${count.sum}\n"
    }
    requestLines.mkString + s"app_counter_value $counterValue\n"
  }
}
