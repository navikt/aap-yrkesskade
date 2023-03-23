package no.nav.aap.dto.kafka

import java.time.LocalDate

data class YrkesskadeKafkaDto(
    val request: Request,
    val response: Response?
) {
    data class Request(
        val personidenterInkludertHistoriske: List<String>,
        val filtrerFraOgMedDato: LocalDate? = null
    )

    data class Response(
        val måSjekkesManuelt: Boolean,
        val beskrivelser: List<String>,
        val kilde: String,
        val kvalitetsikkerDataPeriode: Periode?
    )

    data class Periode(
        val fom: LocalDate,
        val tom: LocalDate?
    )

    fun skalSjekkeYrkesskade(): Boolean = request.personidenterInkludertHistoriske.isNotEmpty() && response == null

    fun skalFinneHistoriskePersonidenter(): Boolean = request.personidenterInkludertHistoriske.isEmpty()

    fun erBesvart(): Boolean = response != null
}