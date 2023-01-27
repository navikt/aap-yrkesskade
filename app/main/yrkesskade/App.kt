package yrkesskade

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.aap.dto.kafka.YrkesskadeKafkaDto
import no.nav.aap.kafka.streams.KStreams
import no.nav.aap.kafka.streams.KafkaStreams
import no.nav.aap.kafka.streams.extension.*
import no.nav.aap.ktor.config.loadConfig
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import yrkesskade.kafka.Topics
import java.time.LocalDate

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

fun Application.server(kafka: KStreams = KafkaStreams) {
    val config = loadConfig<Config>()
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) { registry = prometheus }

    kafka.connect(
        config = config.kafka,
        registry = prometheus,
        topology = topology()
    )

    routing {
        actuators(prometheus, kafka)
    }
}

internal fun topology(): Topology {
    val streams = StreamsBuilder()

    streams
        .consume(Topics.yrkesskade)
        .filter("filter-yrkesskade-response-null") { _, value ->
            value?.response == null
        }
        .mapValues("lag-yrkesskade-response") { yrkesskade ->
            // TODO sett opp rest-kall mot yrkesskade
            YrkesskadeKafkaDto(
                response = YrkesskadeKafkaDto.Response(listOf(YrkesskadeKafkaDto.Response.Yrkesskade(LocalDate.now())))
            )
        }
        .produce(Topics.yrkesskade, "produced-yrkesskade-med-response")

    return streams.build()
}
