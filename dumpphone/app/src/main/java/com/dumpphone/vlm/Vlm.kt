package com.dumpphone.vlm

import com.dumpphone.a11y.UiAction
import com.dumpphone.a11y.UiNodeSnapshot

/**
 * Phase 5 — Tier 3 VLM computer-use fallback.
 *
 * Hard rules encoded here:
 *  - Explicit opt-in consent is required before the loop starts.
 *  - Provider-agnostic: any screenshot-capable model behind [VlmClient].
 *  - Small action vocabulary only (tap-by-text, type, scroll, back, done, ask-user).
 *  - Max-step and max-call (cost) caps; stop-and-ask on low confidence.
 *  - Every step is narrated in plain Portuguese.
 *  - With no API key configured, ONLY [MockVlmClient] runs — a demo that
 *    explains what it *would* do. It never pretends a real model acted.
 */

sealed interface VlmAction {
    data class TapText(val text: String) : VlmAction
    data class Type(val text: String) : VlmAction
    data object Scroll : VlmAction
    data object Back : VlmAction
    data object Done : VlmAction
    data class AskUser(val questionPt: String) : VlmAction
}

data class VlmDecision(
    val action: VlmAction,
    val narrationPt: String,
    val confidence: Double, // 0.0..1.0
)

data class VlmObservation(
    val screenshotPngBase64: String?,
    val uiTreeTexts: List<String>,
    val currentPackage: String?,
    val goalPt: String,
    val step: Int,
)

interface VlmClient {
    val isConfigured: Boolean
    val displayName: String
    suspend fun decide(observation: VlmObservation): Result<VlmDecision>
}

/** Demo client used when no key exists. Deterministic, safe, and clearly labelled as a demo. */
class MockVlmClient : VlmClient {
    override val isConfigured: Boolean = false
    override val displayName: String = "Demonstração (sem modelo real)"

    override suspend fun decide(observation: VlmObservation): Result<VlmDecision> {
        val decision = when (observation.step) {
            0 -> VlmDecision(
                VlmAction.AskUser("Isto é uma demonstração, sem modelo de visão ligado. Quer que eu abra a aplicação para fazer isto à mão, com botões grandes?"),
                "Demonstração: mostraria aqui o que vejo no ecrã e pediria confirmação.",
                confidence = 0.0,
            )
            else -> VlmDecision(VlmAction.Done, "Demonstração terminada.", 0.0)
        }
        return Result.success(decision)
    }
}

/**
 * Provider-agnostic HTTP client stub: POSTs {goal, uiTreeTexts, screenshot}
 * as JSON to a caregiver-configured endpoint and expects
 * {"action":"tap|type|scroll|back|done|ask","text":"...","narrationPt":"...","confidence":0.0-1.0}.
 * The wire call itself is left as a TODO for the pilot deployment — until
 * then it fails honestly instead of pretending to work.
 */
class HttpVlmClient(private val endpoint: String, private val apiKey: String) : VlmClient {
    override val isConfigured: Boolean = endpoint.isNotBlank() && apiKey.isNotBlank()
    override val displayName: String = "Modelo de visão configurado"

    override suspend fun decide(observation: VlmObservation): Result<VlmDecision> {
        if (!isConfigured) return Result.failure(IllegalStateException("Modelo de visão não configurado."))
        // TODO(pilot): real HTTPS POST (OkHttp/HttpURLConnection) + JSON parse via parseDecision().
        return Result.failure(UnsupportedOperationException("Cliente de visão configurado mas transporte HTTP ainda não ligado neste piloto."))
    }

    companion object {
        private fun jsonStringField(json: String, field: String): String? =
            Regex(""""$field"\s*:\s*"((?:[^"\\]|\\.)*)"""").find(json)?.groupValues?.get(1)
                ?.replace("\\\"", "\"")?.replace("\\n", "\n")

        private fun jsonDoubleField(json: String, field: String): Double =
            Regex(""""$field"\s*:\s*([0-9]+(?:\.[0-9]+)?)""").find(json)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

        /** Pure JSON -> decision parsing (flat object), unit-tested independently of any network call. */
        fun parseDecision(json: String): VlmDecision? {
            return try {
                if (!json.trimStart().startsWith("{")) return null
                val narration = jsonStringField(json, "narrationPt") ?: "Vou continuar."
                val confidence = jsonDoubleField(json, "confidence")
                val text = jsonStringField(json, "text") ?: ""
                val action = when (jsonStringField(json, "action")) {
                    "tap" -> if (text.isBlank()) null else VlmAction.TapText(text)
                    "type" -> VlmAction.Type(text)
                    "scroll" -> VlmAction.Scroll
                    "back" -> VlmAction.Back
                    "done" -> VlmAction.Done
                    "ask" -> VlmAction.AskUser(text.ifBlank { narration })
                    else -> null
                }
                action?.let { VlmDecision(it, narration, confidence) }
            } catch (e: Exception) { null }
        }
    }
}

interface ScreenshotProvider {
    /** Base64 PNG, or null when capture is unavailable/refused. UI-tree-only mode is preferred. */
    fun captureBase64Png(): String?
}

data class VlmLoopConfig(
    val maxSteps: Int = 8,
    val maxModelCalls: Int = 8,
    val minConfidence: Double = 0.60,
    val consentGranted: Boolean = false,
)

sealed interface VlmLoopResult {
    data class Completed(val stepsUsed: Int, val narrationLog: List<String>) : VlmLoopResult
    data class StoppedAndAsked(val questionPt: String, val narrationLog: List<String>) : VlmLoopResult
    data class CapReached(val reasonPt: String, val narrationLog: List<String>) : VlmLoopResult
    data class Refused(val reasonPt: String) : VlmLoopResult
    data class Failed(val reasonPt: String, val narrationLog: List<String>) : VlmLoopResult
}

fun VlmAction.toUiAction(): UiAction? = when (this) {
    is VlmAction.TapText -> UiAction.TapOnText(text)
    is VlmAction.Type -> UiAction.TypeText(text)
    VlmAction.Scroll -> UiAction.ScrollForward
    VlmAction.Back -> UiAction.Back
    VlmAction.Done, is VlmAction.AskUser -> null
}

/**
 * The loop itself. All side effects go through callbacks so it is fully
 * unit-testable with a fake client/executor — no Android, no network.
 */
class VlmLoop(
    private val client: VlmClient,
    private val config: VlmLoopConfig,
    private val observe: () -> VlmObservation,
    private val execute: (UiAction) -> Boolean,
    private val narrate: (String) -> Unit,
) {
    suspend fun run(goalPt: String): VlmLoopResult {
        if (!config.consentGranted) {
            return VlmLoopResult.Refused("A ajuda visual precisa da sua autorização nas Definições do cuidador.")
        }
        val log = mutableListOf<String>()
        fun say(text: String) { log += text; narrate(text) }

        if (!client.isConfigured) {
            say("Sem modelo de visão configurado: vou mostrar uma demonstração, sem tocar em nada por si.")
        }

        var calls = 0
        for (step in 0 until config.maxSteps) {
            if (calls >= config.maxModelCalls) {
                return VlmLoopResult.CapReached("Cheguei ao limite de passos permitidos. Não vou continuar sozinho.", log)
            }
            val observation = observe().copy(goalPt = goalPt, step = step)
            calls++
            val decision = client.decide(observation).getOrElse {
                return VlmLoopResult.Failed("Não consegui falar com o modelo de visão. ${it.message ?: ""}".trim(), log)
            }
            say(decision.narrationPt)

            if (decision.confidence < config.minConfidence) {
                val question = (decision.action as? VlmAction.AskUser)?.questionPt
                    ?: "Não tenho a certeza do que fazer a seguir. Pode ajudar-me?"
                return VlmLoopResult.StoppedAndAsked(question, log)
            }
            when (val action = decision.action) {
                VlmAction.Done -> return VlmLoopResult.Completed(step + 1, log)
                is VlmAction.AskUser -> return VlmLoopResult.StoppedAndAsked(action.questionPt, log)
                else -> {
                    val uiAction = action.toUiAction()
                        ?: return VlmLoopResult.Failed("Ação desconhecida do modelo.", log)
                    if (!execute(uiAction)) {
                        return VlmLoopResult.StoppedAndAsked("Tentei, mas não consegui fazer esse passo. Quer tentar à mão?", log)
                    }
                    // Verification is the next loop iteration's observation (new UI tree/screenshot).
                }
            }
        }
        return VlmLoopResult.CapReached("Cheguei ao limite de ${config.maxSteps} passos. Parei para não fazer nada errado.", log)
    }
}

/** Builds the production loop from the accessibility executor + optional screenshot capture. */
object VlmLoopFactory {
    fun createClient(endpoint: String, apiKey: String): VlmClient =
        if (endpoint.isNotBlank() && apiKey.isNotBlank()) HttpVlmClient(endpoint, apiKey) else MockVlmClient()
}
