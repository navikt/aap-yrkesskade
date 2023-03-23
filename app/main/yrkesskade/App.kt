package yrkesskade

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.aap.dto.kafka.YrkesskadeKafkaDto
import no.nav.aap.kafka.streams.v2.KafkaStreams
import no.nav.aap.kafka.streams.v2.Streams
import no.nav.aap.kafka.streams.v2.Topology
import no.nav.aap.kafka.streams.v2.topology
import no.nav.aap.ktor.config.loadConfig
import org.apache.kafka.clients.producer.ProducerRecord
import yrkesskade.kafka.Topics

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

fun Application.server(kafka: Streams = KafkaStreams()) {
    val config = loadConfig<Config>()
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) { registry = prometheus }
    val yrkesskadeClient = YrkesskadeClient(config.azure)

    kafka.connect(
        config = config.kafka,
        registry = prometheus,
        topology = topology(yrkesskadeClient)

    )

    routing {
        actuators(prometheus, kafka)
        route("/test/{personident}") {
            get {
                val personident = requireNotNull(call.parameters["personident"])
                kafka.createProducer(config.kafka, Topics.yrkesskade).use {
                    it.send(
                        ProducerRecord(
                            Topics.yrkesskade.name, personident, YrkesskadeKafkaDto(
                                request = YrkesskadeKafkaDto.Request(
                                    personidenterInkludertHistoriske = listOf(personident)
                                ),
                                response = null
                            )
                        )
                    )
                }
            }
        }
    }
}

internal fun topology(yrkesskadeClient: YrkesskadeClient): Topology = topology {
    consume(Topics.yrkesskade)
        .filter(YrkesskadeKafkaDto::skalSjekkeYrkesskade)
        .mapNotNull { _, yrkessksadeKafkaDTO ->
            val request = YrkesskadeRequest(
                foedselsnumre = yrkessksadeKafkaDTO.request.personidenterInkludertHistoriske,
                fomDato = yrkessksadeKafkaDTO.request.filtrerFraOgMedDato
            )
            yrkesskadeClient.hentYrkesskade(request)?.let { response ->
                yrkessksadeKafkaDTO.copy(
                    response = YrkesskadeKafkaDto.Response(
                        måSjekkesManuelt = response.harYrkesskadeEllerYrkessykdom == "MAA_SJEKKES_MANUELT",
                        beskrivelser = response.beskrivelser,
                        kilde = response.kilde,
                        kvalitetsikkerDataPeriode = response.kildeperiode?.let { kildeperiode ->
                            YrkesskadeKafkaDto.Periode(
                                fom = kildeperiode.fomDato,
                                tom = kildeperiode.tomDato
                            )
                        }
                    )
                )
            }
        }
        .produce(Topics.yrkesskade)
}



