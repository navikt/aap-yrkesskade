package yrkesskade.kafka

import no.nav.aap.dto.kafka.YrkesskadeKafkaDto
import no.nav.aap.kafka.streams.v2.Topic
import no.nav.aap.kafka.streams.v2.serde.JsonSerde

object Topics {
    val yrkesskade = Topic("aap.yrkesskade.v1", JsonSerde.jackson<YrkesskadeKafkaDto>())
}