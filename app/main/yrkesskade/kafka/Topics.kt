package yrkesskade.kafka

import no.nav.aap.dto.kafka.YrkesskadeKafkaDto
import no.nav.aap.kafka.serde.json.JsonSerde
import no.nav.aap.kafka.streams.Topic

object Topics {
    val yrkesskade = Topic("aap.yrkesskade.v1", JsonSerde.jackson<YrkesskadeKafkaDto>())
}