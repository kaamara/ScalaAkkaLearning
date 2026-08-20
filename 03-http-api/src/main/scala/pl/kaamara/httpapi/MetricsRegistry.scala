package pl.kaamara.httpapi

import java.lang.management.ManagementFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.LongAdder
import scala.jdk.CollectionConverters._

/** Minimalny rejestr metryk renderowany do formatu tekstowego Prometheusa.
  *
  * Swiadomie bez biblioteki (micrometer / prometheus-client) — chodzi o to,
  * zeby bylo widac, jak ten format wyglada od srodka. W projekcie
  * produkcyjnym uzywa sie gotowego klienta.
  *
  * Format: nazwa{etykieta="wartosc",...} liczba
  */
final class MetricsRegistry {

  private final case class Key(method: String, path: String, status: Int)

  private val requests = new ConcurrentHashMap[Key, LongAdder]()
  private val durations = new ConcurrentHashMap[Key, LongAdder]()
  private val startedAt = System.currentTimeMillis()

  /** Rejestruje jedno obsluzone zadanie HTTP. */
  def record(method: String, path: String, status: Int, durationNanos: Long): Unit = {
    val key = Key(method, normalize(path, status), status)
    requests.computeIfAbsent(key, _ => new LongAdder()).increment()
    durations.computeIfAbsent(key, _ => new LongAdder()).add(durationNanos / 1000000L)
  }

  /** Ogranicza kardynalnosc etykiet. Bez tego kazda unikalna sciezka
    * (np. /counter/123) tworzy nowa serie czasowa i zabija Prometheusa.
    *
    * Przy 404 sciezka jest z definicji dowolna — kazdy skaner portow albo
    * literowka klienta tworzylaby kolejna serie. Zwijamy ja do jednej
    * etykiety: interesuje nas ile jest pudel, nie w co konkretnie.
    */
  private def normalize(path: String, status: Int): String =
    if (status == 404) "/{unmatched}"
    else if (path.isEmpty) "/"
    else path.replaceAll("/\\d+", "/{id}")

  private def escape(s: String): String =
    s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

  def render(counterValue: Long): String = {
    val sb = new StringBuilder

    sb.append("# HELP http_requests_total Liczba obsluzonych zadan HTTP.\n")
    sb.append("# TYPE http_requests_total counter\n")
    requests.asScala.foreach { case (k, v) =>
      sb.append(
        s"""http_requests_total{method="${escape(k.method)}",path="${escape(k.path)}",status="${k.status}"} ${v.sum()}"""
      ).append('\n')
    }

    sb.append("# HELP http_request_duration_milliseconds_total Sumaryczny czas obslugi zadan.\n")
    sb.append("# TYPE http_request_duration_milliseconds_total counter\n")
    durations.asScala.foreach { case (k, v) =>
      sb.append(
        s"""http_request_duration_milliseconds_total{method="${escape(k.method)}",path="${escape(k.path)}",status="${k.status}"} ${v.sum()}"""
      ).append('\n')
    }

    sb.append("# HELP app_counter_value Aktualna wartosc licznika w aktorze.\n")
    sb.append("# TYPE app_counter_value gauge\n")
    sb.append(s"app_counter_value $counterValue\n")

    val mem = ManagementFactory.getMemoryMXBean
    sb.append("# HELP jvm_memory_used_bytes Uzyta pamiec JVM wg obszaru.\n")
    sb.append("# TYPE jvm_memory_used_bytes gauge\n")
    sb.append(s"""jvm_memory_used_bytes{area="heap"} ${mem.getHeapMemoryUsage.getUsed}""").append('\n')
    sb.append(s"""jvm_memory_used_bytes{area="nonheap"} ${mem.getNonHeapMemoryUsage.getUsed}""").append('\n')

    val threads = ManagementFactory.getThreadMXBean
    sb.append("# HELP jvm_threads_current Liczba zywych watkow.\n")
    sb.append("# TYPE jvm_threads_current gauge\n")
    sb.append(s"jvm_threads_current ${threads.getThreadCount}\n")

    sb.append("# HELP process_uptime_seconds Czas dzialania procesu.\n")
    sb.append("# TYPE process_uptime_seconds gauge\n")
    sb.append(s"process_uptime_seconds ${(System.currentTimeMillis() - startedAt) / 1000}\n")

    sb.toString()
  }
}
