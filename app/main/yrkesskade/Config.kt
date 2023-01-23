package yrkesskade

import no.nav.aap.kafka.streams.KStreamsConfig

internal data class Config(
    val kafka: KStreamsConfig,
)