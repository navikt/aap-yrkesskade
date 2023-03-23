package yrkesskade

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.aap.kafka.streams.v2.Streams

fun Routing.actuators(prometheus: PrometheusMeterRegistry, kafka: Streams) {
    route("/actuator") {
        get("/metrics") {
            val metrics = prometheus.scrape()
            call.respondText(metrics)
        }

        get("/live") {
            val statusCode = if (kafka.live()) HttpStatusCode.OK else HttpStatusCode.InternalServerError
            call.respond(statusCode, "yrkesskade")
        }

        get("/ready") {
            val statusCode = if (kafka.ready()) HttpStatusCode.OK else HttpStatusCode.InternalServerError
            call.respond(statusCode, "yrkesskade")
        }
    }
}