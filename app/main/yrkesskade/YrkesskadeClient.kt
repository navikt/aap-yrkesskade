package yrkesskade

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking
import no.nav.aap.ktor.client.AzureAdTokenProvider
import no.nav.aap.ktor.client.AzureConfig
import org.slf4j.LoggerFactory
import java.time.LocalDate


private val secureLog = LoggerFactory.getLogger("secureLog")

class YrkesskadeClient(azureConfig: AzureConfig) {

    val tokenProvider = AzureAdTokenProvider(
        config = azureConfig,
        scope = "api://dev-gcp.yrkesskade.yrkesskade-saker/.default"
    )

    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout)
        install(HttpRequestRetry)
        install(Logging) {
            level = LogLevel.BODY
            logger = object : Logger {
                override fun log(message: String) = secureLog.info(message)
            }
        }
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }
        }
    }

    /**
     * https://yrkesskade-saker.dev.intern.nav.no/swagger-ui/index.html
     */
    internal fun hentYrkesskade(request: YrkesskadeRequest): YrkesskadeResponse? {
        return runBlocking {
            val response =
                httpClient.post("http://yrkesskade-saker.yrkesskade/api/v1/saker/har-yrkesskade-eller-yrkessykdom") {
                    accept(ContentType.Application.Json)
                    header("Nav-Consumer-Id", "aap-yrkesskade")
                    bearerAuth(tokenProvider.getClientCredentialToken())
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

            if (response.status.isSuccess()) {
                response.body<YrkesskadeResponse>()
            } else null
        }
    }
}

internal data class YrkesskadeRequest(
    val foedselsnumre: List<String>,
    val fomDato: LocalDate?
)

internal data class YrkesskadeResponse(
    val harYrkesskadeEllerYrkessykdom: String,
    val beskrivelser: List<String>,
    val kilde: String,
    val kildeperiode: Kildeperiode?
)

internal data class Kildeperiode(
    val fomDato: LocalDate,
    val tomDato: LocalDate
)
