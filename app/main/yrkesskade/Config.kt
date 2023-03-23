package yrkesskade

import no.nav.aap.kafka.streams.v2.config.StreamsConfig
import no.nav.aap.ktor.client.AzureConfig


internal data class Config(
    val kafka: StreamsConfig,
    val azure: AzureConfig
)