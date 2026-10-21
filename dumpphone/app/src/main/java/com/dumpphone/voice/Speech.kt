package com.dumpphone.voice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.content.ContextCompat
import com.dumpphone.data.PtVariant
import java.util.Locale

/** Speech-to-text behind an interface (Phase 2): on-device Android first, cloud fallback. */
interface SpeechToTextEngine {
    val name: String
    fun listen(variant: PtVariant, onResult: (Result<String>) -> Unit)
    fun stop()
}

/** Cloud STT fallback stub. Honest behaviour: fails with a clear error until a key/endpoint is configured. */
class CloudSpeechToTextStub(private val endpoint: String?, private val apiKey: String?) : SpeechToTextEngine {
    override val name = "Cloud STT (não configurado)"
    val isConfigured: Boolean = !endpoint.isNullOrBlank() && !apiKey.isNullOrBlank()
    override fun listen(variant: PtVariant, onResult: (Result<String>) -> Unit) {
        if (!isConfigured) {
            onResult(Result.failure(IllegalStateException("Reconhecimento na nuvem não está configurado. A usar o reconhecimento do telefone.")))
            return
        }
        // TODO(pilot): POST recorded audio to the configured Whisper-class endpoint.
        onResult(Result.failure(UnsupportedOperationException("Cloud STT configurado mas ainda não implementado neste piloto.")))
    }
    override fun stop() = Unit
}

/** Android SpeechRecognizer, Portuguese. Uses the phone's recognizer (not forced offline). */
class AndroidSpeechToText(private val context: Context) : SpeechToTextEngine {
    override val name = "Telefone (Android)"
    private var recognizer: SpeechRecognizer? = null
    private val main = Handler(Looper.getMainLooper())
    private var triedFallback = false

    override fun listen(variant: PtVariant, onResult: (Result<String>) -> Unit) {
        triedFallback = false
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            deliver(onResult, Result.failure(IllegalStateException("Preciso do microfone. Aceite a permissão e toque outra vez.")))
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            deliver(onResult, Result.failure(IllegalStateException("Este telefone não tem reconhecimento de voz.")))
            return
        }
        main.post { startSession(variant, onResult, explicit = false) }
    }

    private fun startSession(variant: PtVariant, onResult: (Result<String>) -> Unit, explicit: Boolean) {
        stop()
        val component = if (explicit) recognitionComponent() else null
        val language = if (explicit) "pt" else variant.localeTag
        Log.i(TAG, "listening explicit=$explicit component=${component?.flattenToShortString() ?: "default"} lang=$language")
        val created = if (explicit && component != null) {
            SpeechRecognizer.createSpeechRecognizer(context, component)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
        recognizer = created.apply {
            setRecognitionListener(listener(variant, onResult))
            startListening(recognizeIntent(language))
        }
    }

    private fun listener(variant: PtVariant, onResult: (Result<String>) -> Unit) = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            if (text.isNullOrBlank()) deliver(onResult, Result.failure(IllegalStateException("Não ouvi nada. Tente outra vez.")))
            else deliver(onResult, Result.success(text))
        }
        override fun onError(error: Int) {
            Log.i(TAG, "error=$error fallbackTried=$triedFallback")
            val retryable = error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE ||
                error == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ||
                error == SpeechRecognizer.ERROR_CLIENT ||
                error == SpeechRecognizer.ERROR_SERVER ||
                error == SpeechRecognizer.ERROR_NETWORK ||
                error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT ||
                error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY
            if (!triedFallback && retryable) {
                triedFallback = true
                main.post { startSession(variant, onResult, explicit = true) }
                return
            }
            val msg = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "Não percebi. Tente outra vez, mais devagar."
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Não ouvi nada. Toque no botão e fale."
                SpeechRecognizer.ERROR_AUDIO -> "Erro no microfone."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Preciso do microfone. Aceite a permissão e toque outra vez."
                SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE, SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ->
                    "Falta o português neste telefone. Nas definições do Google, descarregue o reconhecimento em português."
                else -> "Não consegui ouvir. Tente outra vez."
            }
            deliver(onResult, Result.failure(IllegalStateException(msg)))
        }
        override fun onReadyForSpeech(params: Bundle?) { Log.i(TAG, "ready") }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun recognizeIntent(language: String) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        // Leaving PREFER_OFFLINE unset: forcing offline made this phone fail at once.
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1400L)
    }

    /** Prefer the on-device Google recognizer that is actually installed on this phone. */
    private fun recognitionComponent(): ComponentName? {
        val services = context.packageManager.queryIntentServices(
            Intent(RecognitionService.SERVICE_INTERFACE), PackageManager.MATCH_DEFAULT_ONLY,
        )
        val infos = services.mapNotNull { it.serviceInfo }
        val preferred = listOf(
            "com.google.android.googlequicksearchbox",
            "com.google.android.as",
            "com.google.android.tts",
        )
        val chosen = preferred.firstNotNullOfOrNull { pkg -> infos.firstOrNull { it.packageName == pkg } }
            ?: infos.firstOrNull()
        return chosen?.let { ComponentName(it.packageName, it.name) }
    }

    private fun deliver(onResult: (Result<String>) -> Unit, value: Result<String>) {
        if (Looper.myLooper() == Looper.getMainLooper()) onResult(value) else main.post { onResult(value) }
    }

    override fun stop() {
        try { recognizer?.cancel() } catch (_: Exception) {}
        try { recognizer?.destroy() } catch (_: Exception) {}
        recognizer = null
    }

    companion object { private const val TAG = "DumpphoneSpeech" }
}

/** Tries on-device STT; only falls back to cloud when configured and on-device fails. */
class FallbackSpeechToText(
    private val primary: SpeechToTextEngine,
    private val cloud: CloudSpeechToTextStub,
) : SpeechToTextEngine {
    override val name = "Automático"
    override fun listen(variant: PtVariant, onResult: (Result<String>) -> Unit) {
        primary.listen(variant) { result ->
            if (result.isSuccess || !cloud.isConfigured) onResult(result)
            else cloud.listen(variant, onResult)
        }
    }
    override fun stop() { primary.stop(); cloud.stop() }
}

/** Text-to-speech read-back in Portuguese (Phase 2/4). */
interface Speaker {
    fun speak(textPt: String)
    fun stop()
}

class AndroidSpeaker(context: Context, private val variantProvider: () -> PtVariant) : Speaker {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: String? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                applyLocale()
                ready = true
                pending?.let { speak(it) }
            }
        }
    }

    private fun applyLocale() {
        val variant = variantProvider()
        val locale = if (variant == PtVariant.PT_BR) Locale("pt", "BR") else Locale("pt", "PT")
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fall back to any Portuguese voice rather than silence.
            tts?.setLanguage(Locale("pt"))
        }
        tts?.setSpeechRate(0.92f) // slightly slower for elderly listeners
    }

    override fun speak(textPt: String) {
        if (!ready) { pending = textPt; return }
        applyLocale()
        tts?.speak(textPt, TextToSpeech.QUEUE_FLUSH, null, "dumpphone-utterance")
    }

    override fun stop() { tts?.stop() }
    fun shutdown() { tts?.shutdown() }
}
