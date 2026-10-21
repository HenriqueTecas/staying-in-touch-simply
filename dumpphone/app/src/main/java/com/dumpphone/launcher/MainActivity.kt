package com.dumpphone.launcher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dumpphone.a11y.ChatAutomation
import com.dumpphone.a11y.DumpphoneAccessibilityService
import com.dumpphone.a11y.UiTreeReader
import com.dumpphone.actions.Tier1Actions
import com.dumpphone.data.Contact
import com.dumpphone.data.ContactsRepository
import com.dumpphone.data.DummyMode
import com.dumpphone.data.MockThread
import com.dumpphone.data.PtVariant
import com.dumpphone.data.SettingsStore
import com.dumpphone.router.IntentRouter
import com.dumpphone.safety.ConfirmationPolicy
import com.dumpphone.safety.ContactMatcher
import com.dumpphone.vlm.*
import com.dumpphone.voice.*
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// High-contrast oversized theme (Phase 1 accessibility pass: >=96dp targets, no gestures, <=2 taps).
private val Ink = Color(0xFF000000)
private val Paper = Color(0xFFFFFFFF)
private val Green = Color(0xFF0B6E2F)
private val Blue = Color(0xFF0B4DA2)
private val Orange = Color(0xFFB34700)
private val Teal = Color(0xFF00695C)
private val Red = Color(0xFFB00020)

class MainActivity : ComponentActivity() {
    lateinit var settingsStore: SettingsStore
    lateinit var speaker: AndroidSpeaker
    lateinit var stt: FallbackSpeechToText

    /**
     * Benchmark hook. Set only by an explicit adb intent:
     * `--es dumpphone_bench_transcript "..."` plus `--es dumpphone_bench_nonce <id>`.
     * The app parses and shows the confirmation screen, and the SIM button does nothing.
     * SOS, opening apps, and reading messages are not executed on this path.
     */
    var benchRequest by mutableStateOf<String?>(null)
        private set

    private fun readBench(intent: Intent?): String? {
        val transcript = intent?.getStringExtra("dumpphone_bench_transcript") ?: return null
        val nonce = intent.getStringExtra("dumpphone_bench_nonce") ?: ""
        return "$transcript|$nonce"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsStore = SettingsStore(this)
        speaker = AndroidSpeaker(this) { settingsStore.ptVariant }
        stt = FallbackSpeechToText(
            AndroidSpeechToText(this),
            CloudSpeechToTextStub(settingsStore.cloudSttEndpoint.ifBlank { null }, settingsStore.cloudSttApiKey.ifBlank { null }),
        )
        if (settingsStore.kioskEnabledByCaregiver) {
            KioskManager(this).startKioskIfAllowed(this)
        }
        benchRequest = readBench(intent)
        setContent { DumpphoneApp(this) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        benchRequest = readBench(intent)
    }

    fun speak(text: String) = speaker.speak(text)
    override fun onDestroy() { speaker.shutdown(); stt.stop(); super.onDestroy() }
}

internal enum class PickerMode { CALL, SMS, WHATSAPP }
private sealed interface Screen {
    data object Home : Screen
    data class Picker(val mode: PickerMode) : Screen
    data object AlarmSetup : Screen
    data class Dictate(val contact: Contact, val app: ChatApp, val draft: String = "") : Screen
    data class Confirm(val intent: ParsedIntent, val contact: Contact?, val heard: String? = null) : Screen
    data class Disambiguate(val intent: ParsedIntent, val candidates: List<Contact>) : Screen
    data class Listening(val heard: String? = null) : Screen
    data class MockCall(val name: String) : Screen
    data class Chats(val app: ChatApp) : Screen
    data class ReadAloud(val thread: MockThread, val chip: String = "A ler", val kicker: String = "WHATSAPP") : Screen
    data object PinGate : Screen
    data object CaregiverSettings : Screen
    data object ActionLogScreen : Screen
    data class ShowMessages(val app: ChatApp, val messages: List<String>) : Screen
    data class Notice(val textPt: String) : Screen
}

@Composable
private fun DumpphoneApp(activity: MainActivity) {
    val context = LocalContext.current
    val settings = activity.settingsStore
    val tier1 = remember { Tier1Actions(context) }
    val chatAutomation = remember { ChatAutomation(context) }
    val contactsRepo = remember { ContactsRepository(context, settings) }
    val parser = remember { RuleBasedIntentParser() }

    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var benchMode by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    var voiceSession by remember { mutableIntStateOf(0) }
    val heardHandler = remember { mutableStateOf<(String) -> Unit>({}) }
    val benchHandler = remember { mutableStateOf<(ParsedIntent, String?) -> Unit>({ _, _ -> }) }

    fun failVoice(message: String, stay: Boolean) {
        listening = false
        statusText = message
        activity.speak(message)
        if (screen is Screen.Dictate) screen = (screen as Screen.Dictate).copy(draft = "")
        if (!stay && screen is Screen.Listening) screen = Screen.Home
    }

    fun logAndTell(textPt: String, speak: Boolean = true) {
        settings.log(textPt)
        statusText = textPt
        if (speak) activity.speak(textPt)
    }

    fun ao(contact: Contact): String =
        if (contact.relation in setOf("Filho", "Irmão", "Neto", "Marido", "Pai")) "ao" else "à"

    fun artigoDe(contact: Contact): String =
        if (contact.relation in setOf("Filho", "Irmão", "Neto", "Marido", "Pai")) "o" else "a"

    fun spokenPrompt(intent: ParsedIntent, contact: Contact?): String {
        if (intent is ParsedIntent.CallContact) {
            val name = contact?.displayName ?: intent.contactQuery
            val artigo = contact?.let { artigoDe(it) } ?: "a"
            return "Quer ligar para $artigo $name?"
        }
        return ConfirmationPolicy.confirmationPromptPt(intent, contact?.displayName)
    }

    fun appLabel(app: ChatApp): String = when (app) {
        ChatApp.WHATSAPP -> "WhatsApp"
        ChatApp.SMS -> "Mensagens"
    }

    fun isNo(text: String): Boolean {
        val n = text.lowercase(Locale("pt", "PT"))
        return listOf("não", "nao", "cancela", "esquece", "não quero", "nao quero", "errado").any { n.contains(it) }
    }

    fun isYes(text: String): Boolean {
        val n = text.lowercase(Locale("pt", "PT"))
        return listOf("sim", "quero", "pode", "confirmo", "está bem", "esta bem", "ok", "claro", "envia", "liga").any { n.contains(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        contacts = contactsRepo.loadContacts()
    }
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        listening = false
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!text.isNullOrBlank()) {
            statusText = text
            heardHandler.value(text)
        } else {
            failVoice("Não ouvi nada. Toque em FALAR e fale.", stay = screen !is Screen.Listening)
        }
    }
    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        contacts = contactsRepo.loadContacts()
    }
    LaunchedEffect(activity.benchRequest) {
        val req = activity.benchRequest ?: return@LaunchedEffect
        val transcript = req.substringBeforeLast("|")
        if (transcript.isBlank()) return@LaunchedEffect
        benchMode = true
        val t0 = System.nanoTime()
        val parsed = parser.parse(transcript)
        val ms = (System.nanoTime() - t0) / 1_000_000.0
        android.util.Log.i(
            "DUMPPHONE_BENCH",
            "transcript=${transcript.replace('\n', ' ')} intent=$parsed latency_ms=${"%.1f".format(ms)} executed=false",
        )
        when (parsed) {
            is ParsedIntent.Sos ->
                screen = Screen.Notice("Teste: reconheci um pedido de ajuda. Nenhuma chamada foi feita.")
            is ParsedIntent.OpenApp, is ParsedIntent.ReadMessages ->
                screen = Screen.Notice("Teste: reconheci o pedido. Não abri nenhuma aplicação.")
            else -> benchHandler.value(parsed, transcript)
        }
    }

    fun hasCallPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

    fun mockSent(contact: Contact?, kicker: String, message: String) {
        val name = contact?.displayName ?: "essa pessoa"
        val line = "Mensagem enviada para $name. Exemplo. Nada saiu do telemóvel."
        screen = Screen.ReadAloud(
            thread = MockThread(
                contactId = contact?.id ?: "sent",
                title = name,
                relation = contact?.relation ?: "",
                timeLabel = "agora",
                unread = 0,
                preview = message,
                body = message,
            ),
            chip = "Enviada",
            kicker = kicker,
        )
        activity.speak("$line $message")
        settings.log(line)
        statusText = line
        android.util.Log.i("DUMPPHONE", "mock-message to=$name sent=false")
    }

    fun doSos() {
        if (DummyMode.ENABLED) {
            screen = Screen.MockCall("Emergência")
            val line = "A ligar para a emergência. Exemplo. Nenhuma chamada saiu do telemóvel."
            activity.speak(line)
            settings.log(line)
            statusText = line
            android.util.Log.i("DUMPPHONE", "mock-sos dialed=false")
            return
        }
        val number = settings.sosNumber.ifBlank { "112" }
        val who = settings.sosContactName.ifBlank { "emergência (112)" }
        logAndTell("A ligar para $who. Mantenha a calma, a ajuda vem a caminho.")
        tier1.call(number, hasCallPermission())
        settings.log("SOS: chamada para $who ($number).")
    }

    fun executeConfirmed(intent: ParsedIntent, contact: Contact?) {
        if (intent is ParsedIntent.MessengerRemoved) {
            val msg = DummyMode.MESSENGER_REMOVED
            screen = Screen.Notice(msg)
            activity.speak(msg)
            settings.log(msg)
            android.util.Log.i("DUMPPHONE", "messenger-removed opened=false sent=false")
            return
        }
        if (DummyMode.ENABLED && intent !is ParsedIntent.SetAlarm) {
            when (intent) {
                is ParsedIntent.CallContact -> {
                    val name = contact?.displayName ?: intent.contactQuery
                    screen = Screen.MockCall(name)
                    val line = "A ligar para $name. Exemplo. Nenhuma chamada saiu do telemóvel."
                    activity.speak(line)
                    settings.log(line)
                    statusText = line
                    android.util.Log.i("DUMPPHONE", "mock-call name=$name dialed=false")
                }
                is ParsedIntent.SendSms -> mockSent(contact, "MENSAGEM", intent.message)
                is ParsedIntent.SendWhatsApp -> mockSent(contact, "WHATSAPP", intent.message)
                else -> {
                    screen = Screen.Notice(DummyMode.HELD_MESSAGE)
                    activity.speak(DummyMode.HELD_MESSAGE)
                    settings.log(DummyMode.HELD_MESSAGE)
                }
            }
            return
        }
        when (intent) {
            is ParsedIntent.CallContact -> {
                val phone = contact?.phoneNumber
                if (phone.isNullOrBlank()) {
                    screen = Screen.Notice("Não tenho o número de ${contact?.displayName ?: "esse contacto"}.")
                    activity.speak("Não tenho esse número.")
                } else {
                    tier1.call(phone, hasCallPermission())
                    logAndTell("A ligar para ${contact.displayName}.")
                }
            }
            is ParsedIntent.SendSms -> {
                tier1.sms(contact?.phoneNumber, intent.message)
                logAndTell("Mensagem escrita para ${contact?.displayName}. Toque em Enviar no ecrã das mensagens.")
            }
            is ParsedIntent.SendWhatsApp -> {
                activity.lifecycleScope.launch {
                    val result = withContext(Dispatchers.Default) {
                        if (contact != null) chatAutomation.sendWhatsAppTier2(contact, intent.message, settings.ptVariant)
                        else ChatAutomation.Result.Failed("Contacto não encontrado.")
                    }
                    when (result) {
                        ChatAutomation.Result.Done -> logAndTell("WhatsApp aberto para ${contact?.displayName}.")
                        is ChatAutomation.Result.NeedsAccessibility -> { screen = Screen.Notice(result.reasonPt); activity.speak(result.reasonPt) }
                        is ChatAutomation.Result.Failed -> { screen = Screen.Notice(result.reasonPt); activity.speak(result.reasonPt) }
                    }
                }
            }
            is ParsedIntent.SetAlarm -> {
                tier1.setAlarm(intent.hour, intent.minute, intent.label)
                logAndTell("Despertador para as ${"%02d:%02d".format(intent.hour, intent.minute)}. Confirme no ecrã do relógio.")
            }
            else -> Unit
        }
        if (screen is Screen.Confirm || screen is Screen.Disambiguate) screen = Screen.Home
    }

    fun resolveIntent(intent: ParsedIntent, heard: String? = null) {
        val router = IntentRouter(settings.vlmConsent, DumpphoneAccessibilityService.isEnabled)
        when (intent) {
            is ParsedIntent.Cancel -> {
                logAndTell("Está bem, não faço nada.")
                screen = Screen.Home
            }
            is ParsedIntent.Help -> {
                val help = "Pode dizer: ligar para a Maria; mandar um WhatsApp à Maria a dizer já cheguei; pôr o despertador para as sete; ler as mensagens; ou socorro."
                screen = Screen.Notice(help)
                activity.speak(help)
            }
            is ParsedIntent.MessengerRemoved -> {
                val msg = DummyMode.MESSENGER_REMOVED
                android.util.Log.i("DUMPPHONE", "messenger-removed opened=false sent=false")
                screen = Screen.Notice(msg)
                activity.speak(msg)
                settings.log(msg)
            }
            is ParsedIntent.Sos -> doSos()
            is ParsedIntent.SetAlarm -> {
                router.route(intent)
                screen = Screen.Confirm(intent, null, heard)
                activity.speak(spokenPrompt(intent, null))
            }
            is ParsedIntent.OpenApp -> {
                if (DummyMode.ENABLED) {
                    when (intent.app) {
                        ChatApp.WHATSAPP -> screen = Screen.Chats(ChatApp.WHATSAPP)
                        ChatApp.SMS -> screen = Screen.Picker(PickerMode.SMS)
                    }
                    activity.speak("A abrir ${intent.app.displayNamePt}. Isto é um exemplo.")
                } else {
                    when (val result = tier1.openApp(intent.app.packageName)) {
                        is Tier1Actions.LaunchResult.Failed -> {
                            screen = Screen.Notice(result.reasonPt)
                            activity.speak(result.reasonPt)
                        }
                        else -> logAndTell("A abrir ${intent.app.displayNamePt}.")
                    }
                }
            }
            is ParsedIntent.ReadMessages -> {
                if (DummyMode.ENABLED) {
                    screen = Screen.Chats(intent.app)
                    val spoken = ContactsRepository.mockThreads().joinToString(". ") { "${it.title} disse: ${it.body}" }
                    activity.speak(spoken)
                    settings.log("Leu conversas de exemplo.")
                    statusText = "Li as conversas de exemplo."
                } else {
                    activity.speak("Vou abrir ${intent.app.displayNamePt} e ler as mensagens.")
                    activity.lifecycleScope.launch {
                        val result = withContext(Dispatchers.Default) { chatAutomation.readLatestMessages(intent.app) }
                        when (result) {
                            is ChatAutomation.Result2.Messages -> {
                                screen = Screen.ShowMessages(intent.app, result.messages)
                                activity.speak(result.messages.joinToString(". ") { it })
                                settings.log("Leu ${result.messages.size} mensagens do ${intent.app.displayNamePt} em voz alta.")
                            }
                            is ChatAutomation.Result2.NeedsAccessibility -> { screen = Screen.Notice(result.reasonPt); activity.speak(result.reasonPt) }
                            is ChatAutomation.Result2.Failed -> { screen = Screen.Notice(result.reasonPt); activity.speak(result.reasonPt) }
                        }
                    }
                }
            }
            is ParsedIntent.CallContact, is ParsedIntent.SendSms, is ParsedIntent.SendWhatsApp -> {
                val query = when (intent) {
                    is ParsedIntent.CallContact -> intent.contactQuery
                    is ParsedIntent.SendSms -> intent.contactQuery
                    is ParsedIntent.SendWhatsApp -> intent.contactQuery
                    else -> ""
                }
                when (val match = ContactMatcher.match(query, contacts)) {
                    is ContactMatcher.MatchResult.NotFound -> {
                        val msg = "Não encontrei nenhum contacto chamado $query. Vamos tentar outra vez?"
                        screen = Screen.Notice(msg)
                        activity.speak(msg)
                    }
                    is ContactMatcher.MatchResult.Ambiguous -> {
                        screen = Screen.Disambiguate(intent, match.candidates)
                        activity.speak("Encontrei mais do que uma pessoa com esse nome. Qual é?")
                    }
                    is ContactMatcher.MatchResult.Unique -> {
                        val messageBlank = when (intent) {
                            is ParsedIntent.SendSms -> intent.message.isBlank()
                            is ParsedIntent.SendWhatsApp -> intent.message.isBlank()
                            else -> false
                        }
                        if (messageBlank) {
                            val app = when (intent) {
                                is ParsedIntent.SendWhatsApp -> ChatApp.WHATSAPP
                                else -> ChatApp.SMS
                            }
                            screen = Screen.Dictate(match.contact, app)
                            activity.speak("O que quer dizer ${ao(match.contact)} ${match.contact.displayName}? Toque em falar.")
                        } else {
                            router.route(intent, !match.contact.phoneNumber.isNullOrBlank())
                            screen = Screen.Confirm(intent, match.contact, heard)
                            activity.speak(spokenPrompt(intent, match.contact))
                        }
                    }
                }
            }
            is ParsedIntent.Unknown -> {
                if (settings.vlmConsent) {
                    runVlmFallback(activity, settings, intent.transcript)
                    logAndTell("Vou tentar ajudar com a ajuda visual autorizada.", speak = false)
                } else {
                    val msg = "Desculpe, não percebi. Toque em FALAR e diga, por exemplo: ligar para a Maria."
                    screen = Screen.Notice(msg)
                    activity.speak(msg)
                }
            }
        }
    }

    fun beginSystemRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-PT")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "pt-PT")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale agora")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try {
            android.util.Log.i("DumpphoneSpeech", "opening system recognizer")
            voiceLauncher.launch(intent)
        } catch (e: Exception) {
            android.util.Log.i("DumpphoneSpeech", "system-dialog ${e.message}")
            failVoice("Não consegui ouvir. Tente outra vez.", stay = screen !is Screen.Listening)
        }
    }

    fun startListening(stayOnScreen: Boolean = false, onText: (String) -> Unit) {
        val id = voiceSession + 1
        voiceSession = id
        heardHandler.value = onText
        listening = true
        statusText = "A ouvir... fale agora"
        if (!stayOnScreen) screen = Screen.Listening(null)
        else if (screen is Screen.Dictate) screen = (screen as Screen.Dictate).copy(draft = "A ouvir...")
        android.util.Log.i("DumpphoneSpeech", "falar tapped")
        activity.stt.listen(settings.ptVariant) { result ->
            if (voiceSession != id) return@listen
            voiceSession = 0
            listening = false
            result.onSuccess { text ->
                statusText = text
                onText(text)
            }.onFailure { err ->
                val msg = err.message ?: "Não consegui ouvir. Tente outra vez."
                android.util.Log.i("DumpphoneSpeech", "ui-fail $msg")
                val broken = msg.contains("Não consegui ouvir") ||
                    msg.contains("Falta o português") ||
                    msg.contains("não tem reconhecimento") ||
                    msg.contains("Erro no microfone")
                if (broken) {
                    activity.stt.stop()
                    beginSystemRecognizer()
                } else failVoice(msg, stayOnScreen)
            }
        }
    }

    fun acceptConfirm(intent: ParsedIntent, contact: Contact?) {
        if (benchMode) {
            android.util.Log.i("DUMPPHONE_BENCH", "yes-ignored executed=false")
            logAndTell("Modo de teste: não faço essa ação.")
            screen = Screen.Home
        } else {
            executeConfirmed(intent, contact)
            if (screen is Screen.Confirm) screen = Screen.Home
        }
    }

    fun hangUp() {
        activity.stt.stop()
        voiceSession = 0
        listening = false
        activity.speaker.stop()
        if (screen is Screen.MockCall) logAndTell("Desliguei. Nenhuma chamada saiu do telemóvel.")
        screen = Screen.Home
    }

    fun onFalar() {
        when (val s = screen) {
            is Screen.Dictate -> startListening(stayOnScreen = true) { text ->
                val intent = when (s.app) {
                    ChatApp.WHATSAPP -> ParsedIntent.SendWhatsApp(s.contact.displayName, text)
                    ChatApp.SMS -> ParsedIntent.SendSms(s.contact.displayName, text)
                }
                screen = Screen.Confirm(intent, s.contact, heard = text)
                activity.speak(spokenPrompt(intent, s.contact))
            }
            is Screen.Confirm -> startListening(stayOnScreen = true) { text ->
                when {
                    isNo(text) -> {
                        logAndTell("Está bem, não faço nada.")
                        screen = Screen.Home
                    }
                    isYes(text) -> acceptConfirm(s.intent, s.contact)
                    else -> logAndTell("Diga sim ou não.")
                }
            }
            else -> startListening { text -> resolveIntent(parser.parse(text), text) }
        }
    }

    benchHandler.value = { parsed, heard -> resolveIntent(parsed, heard) }

    @Composable
    fun Docked(content: @Composable () -> Unit) {
        CasaScaffold(
            onLigar = { screen = Screen.Picker(PickerMode.CALL) },
            onFalar = { onFalar() },
            onDesligar = { hangUp() },
            listening = listening,
            content = content,
        )
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = CallGreen, background = Cream, onBackground = InkBrown)) {
        Surface(Modifier.fillMaxSize(), color = Cream) {
            when (val s = screen) {
                Screen.Home -> Docked {
                    CasaHome(
                        statusText = statusText,
                        favourites = contactsRepo.favourites(contacts),
                        onTile = { mode -> screen = Screen.Picker(mode) },
                        onAlarm = { screen = Screen.AlarmSetup },
                        onSos = { doSos() },
                        onChats = { screen = Screen.Chats(ChatApp.WHATSAPP) },
                        onContact = { contact ->
                            screen = Screen.Confirm(ParsedIntent.CallContact(contact.displayName), contact)
                            activity.speak("Quer ligar para ${artigoDe(contact)} ${contact.displayName}?")
                        },
                        onMore = { screen = Screen.Picker(PickerMode.CALL) },
                        onCaregiver = { screen = Screen.PinGate },
                        onLog = { screen = Screen.ActionLogScreen },
                    )
                }
                is Screen.Picker -> Docked {
                    CasaPeople(
                        title = when (s.mode) {
                            PickerMode.CALL -> "Ligar para quem?"
                            PickerMode.SMS -> "Mensagem para quem?"
                            PickerMode.WHATSAPP -> "WhatsApp para quem?"
                        },
                        subtitle = if (s.mode == PickerMode.CALL) {
                            "Toque na pessoa. Eu confirmo antes de ligar."
                        } else {
                            "Toque na pessoa. Eu confirmo antes de enviar."
                        },
                        contacts = contacts,
                        onBack = { screen = Screen.Home },
                        onPick = { contact ->
                            when (s.mode) {
                                PickerMode.CALL -> {
                                    screen = Screen.Confirm(ParsedIntent.CallContact(contact.displayName), contact)
                                    activity.speak("Quer ligar para ${artigoDe(contact)} ${contact.displayName}?")
                                }
                                else -> {
                                    val app = when (s.mode) {
                                        PickerMode.WHATSAPP -> ChatApp.WHATSAPP
                                        else -> ChatApp.SMS
                                    }
                                    screen = Screen.Dictate(contact, app)
                                    activity.speak("O que quer dizer ${ao(contact)} ${contact.displayName}? Toque em falar.")
                                }
                            }
                        },
                    )
                }
                Screen.AlarmSetup -> Docked {
                    CasaAlarm(onBack = { screen = Screen.Home }) { hour, minute ->
                        val intent = ParsedIntent.SetAlarm(hour, minute)
                        screen = Screen.Confirm(intent, null)
                        activity.speak(spokenPrompt(intent, null))
                    }
                }
                is Screen.Dictate -> Docked {
                    CasaDictate(contact = s.contact, app = s.app, draft = s.draft, onBack = { screen = Screen.Home })
                }
                is Screen.Listening -> Docked {
                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.padding(18.dp)) { CasaBack(onBack = { hangUp() }) }
                        CasaListening(s.heard)
                    }
                }
                is Screen.Confirm -> Docked {
                    CasaConfirm(
                        intent = s.intent,
                        contact = s.contact,
                        heard = s.heard,
                        onYes = { acceptConfirm(s.intent, s.contact) },
                        onNo = {
                            logAndTell("Está bem, não faço nada.")
                            screen = Screen.Home
                        },
                        onBack = { screen = Screen.Home },
                    )
                }
                is Screen.Disambiguate -> Docked {
                    CasaDisambiguate(
                        candidates = s.candidates,
                        onBack = { screen = Screen.Home },
                        onPick = { contact ->
                            val messageBlank = when (val i = s.intent) {
                                is ParsedIntent.SendSms -> i.message.isBlank()
                                is ParsedIntent.SendWhatsApp -> i.message.isBlank()
                                else -> false
                            }
                            if (messageBlank && s.intent !is ParsedIntent.CallContact) {
                                val app = when (s.intent) {
                                    is ParsedIntent.SendWhatsApp -> ChatApp.WHATSAPP
                                    else -> ChatApp.SMS
                                }
                                screen = Screen.Dictate(contact, app)
                                activity.speak("O que quer dizer ${ao(contact)} ${contact.displayName}?")
                            } else {
                                screen = Screen.Confirm(s.intent, contact)
                                activity.speak(spokenPrompt(s.intent, contact))
                            }
                        },
                    )
                }
                is Screen.Chats -> Docked {
                    CasaChats(
                        threads = ContactsRepository.mockThreads(),
                        contacts = contacts,
                        appLabel = appLabel(s.app),
                        onBack = { screen = Screen.Home },
                        onOpen = { thread ->
                            screen = Screen.ReadAloud(thread, kicker = appLabel(s.app).uppercase())
                            activity.speak("${thread.title} disse: ${thread.body}")
                            settings.log("Leu uma mensagem de exemplo de ${thread.title}.")
                        },
                        onReadAll = {
                            val spoken = ContactsRepository.mockThreads().joinToString(". ") { "${it.title} disse: ${it.body}" }
                            activity.speak(spoken)
                            settings.log("Leu as conversas de exemplo em voz alta.")
                        },
                    )
                }
                is Screen.ReadAloud -> Docked {
                    CasaReadAloud(
                        thread = s.thread,
                        contact = contacts.firstOrNull { it.id == s.thread.contactId },
                        chip = s.chip,
                        kicker = s.kicker,
                        againLabel = if (s.chip == "Enviada") "Ouvir outra vez" else "Ler outra vez",
                        onBack = { screen = Screen.Home },
                        onAgain = { activity.speak(s.thread.body) },
                    )
                }
                is Screen.MockCall -> Docked {
                    CasaMockCall(name = s.name, onHangup = { hangUp() })
                }
                Screen.PinGate -> PinGateScreen(
                    onBack = { screen = Screen.Home },
                    onUnlocked = { screen = Screen.CaregiverSettings },
                    verify = { settings.verifyPin(it) },
                )
                Screen.CaregiverSettings -> CaregiverSettingsScreen(
                    activity = activity, settings = settings,
                    onBack = { screen = Screen.Home },
                    onOpenLog = { screen = Screen.ActionLogScreen },
                )
                Screen.ActionLogScreen -> ActionLogScreen(
                    entries = settings.actionLog.all().map { it.formatted() },
                    onBack = { screen = Screen.Home },
                )
                is Screen.ShowMessages -> MessagesScreen(
                    app = s.app, messages = s.messages,
                    onBack = { screen = Screen.Home },
                    onReadAgain = { activity.speak(s.messages.joinToString(". ")) },
                )
                is Screen.Notice -> Column(Modifier.fillMaxSize().padding(18.dp)) {
                    CasaBack(onBack = { screen = Screen.Home })
                    Spacer(Modifier.height(24.dp))
                    Text(
                        s.textPt,
                        fontSize = 24.sp,
                        color = InkBrown,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private fun runVlmFallback(activity: MainActivity, settings: SettingsStore, goal: String) {
    val service = DumpphoneAccessibilityService.instance
    val client = VlmLoopFactory.createClient(settings.vlmEndpoint, settings.vlmApiKey)
    val loop = VlmLoop(
        client = client,
        config = VlmLoopConfig(consentGranted = settings.vlmConsent),
        observe = {
            VlmObservation(
                screenshotPngBase64 = null, // UI-tree first; screenshots only via a consenting ScreenshotProvider in pilot.
                uiTreeTexts = service?.currentTree()?.let { UiTreeReader.extractLatestMessages(it, 20) } ?: emptyList(),
                currentPackage = service?.currentPackage(),
                goalPt = goal, step = 0,
            )
        },
        execute = { action -> service?.perform(action) ?: false },
        narrate = { activity.speak(it) },
    )
    activity.lifecycleScope.launch {
        val result = withContext(Dispatchers.Default) { loop.run(goal) }
        val text = when (result) {
            is VlmLoopResult.Completed -> "Terminei."
            is VlmLoopResult.StoppedAndAsked -> result.questionPt
            is VlmLoopResult.CapReached -> result.reasonPt
            is VlmLoopResult.Refused -> result.reasonPt
            is VlmLoopResult.Failed -> result.reasonPt
        }
        settings.log("Ajuda visual: $text")
        activity.speak(text)
    }
}

// ---------- Screens ----------

@Composable
private fun BigTile(label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(120.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun HomeScreen(
    favourites: List<Contact>, listening: Boolean, statusText: String,
    onTile: (PickerMode) -> Unit, onAlarm: () -> Unit, onSos: () -> Unit, onSpeak: () -> Unit,
    onReadMessages: () -> Unit, onContactTap: (Contact) -> Unit, onCaregiver: () -> Unit, onLog: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Olá!", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Ink)
            Row {
                IconButton(onClick = onLog, modifier = Modifier.size(56.dp)) { Icon(Icons.Filled.List, "Registo", Modifier.size(32.dp)) }
                IconButton(onClick = onCaregiver, modifier = Modifier.size(56.dp)) { Icon(Icons.Filled.Settings, "Definições do cuidador", Modifier.size(32.dp)) }
            }
        }
        Text(statusText, fontSize = 20.sp, color = Ink, modifier = Modifier.padding(top = 8.dp))
        Text(
            "Contactos de exemplo. Esta versão não liga e não envia mensagens.",
            fontSize = 18.sp,
            color = Ink.copy(alpha = 0.75f),
            modifier = Modifier.padding(bottom = 8.dp),
        )

        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            item { BigTile("Ligar", Icons.Filled.Call, Green) { onTile(PickerMode.CALL) } }
            item { BigTile("Mensagens", Icons.AutoMirrored.Filled.Message, Blue) { onTile(PickerMode.SMS) } }
            item { BigTile("Despertador", Icons.Filled.Alarm, Orange) { onAlarm() } }
            item { BigTile("WhatsApp", Icons.Filled.Chat, Teal) { onTile(PickerMode.WHATSAPP) } }
            item { BigTile("SOS — Ajuda", Icons.Filled.Emergency, Red) { onSos() } }
        }

        if (favourites.isNotEmpty()) {
            Text("Pessoas importantes — toque para ligar", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            LazyColumn(Modifier.heightIn(max = 180.dp)) {
                items(favourites.take(4)) { contact -> ContactRow(contact) { onContactTap(contact) } }
            }
        }

        Button(
            onClick = onReadMessages,
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            shape = RoundedCornerShape(16.dp),
        ) { Text("Ler as minhas mensagens", fontSize = 22.sp, fontWeight = FontWeight.Bold) }

        Button(
            onClick = onSpeak,
            modifier = Modifier.fillMaxWidth().height(84.dp).padding(top = 10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (listening) Red else Ink),
            shape = RoundedCornerShape(20.dp),
        ) {
            Icon(Icons.Filled.Mic, null, Modifier.size(36.dp), tint = Color.White)
            Spacer(Modifier.width(10.dp))
            Text(if (listening) "A ouvir… fale agora" else "FALAR", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun ContactRow(contact: Contact, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 5.dp).height(84.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F1F1)),
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            ContactAvatar(contact, 60.dp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(contact.displayName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
                contact.phoneNumber?.let { Text(it, fontSize = 18.sp, color = Ink) }
            }
        }
    }
}

@Composable
private fun ContactAvatar(contact: Contact, size: androidx.compose.ui.unit.Dp) {
    val context = LocalContext.current
    var bitmap by remember(contact.photoUri) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(contact.photoUri) {
        val uriString = contact.photoUri
        if (!uriString.isNullOrBlank()) {
            bitmap = withContext(Dispatchers.IO) {
                try { context.contentResolver.openInputStream(Uri.parse(uriString))?.use { BitmapFactory.decodeStream(it) } } catch (e: Exception) { null }
            }
        }
    }
    val bmp = bitmap
    if (bmp != null) {
        Image(bmp.asImageBitmap(), contentDescription = contact.displayName, Modifier.size(size).clip(CircleShape), contentScale = ContentScale.Crop)
    } else {
        Box(Modifier.size(size).clip(CircleShape).background(Green), contentAlignment = Alignment.Center) {
            Text(contact.displayName.take(1).uppercase(), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BackTitle(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = onBack, modifier = Modifier.height(60.dp), shape = RoundedCornerShape(14.dp)) { Text("← Voltar", fontSize = 20.sp) }
        Spacer(Modifier.width(12.dp))
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
    }
}

@Composable
private fun ContactPickerScreen(titlePt: String, contacts: List<Contact>, onBack: () -> Unit, onPick: (Contact) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        BackTitle(titlePt, onBack)
        LazyColumn { items(contacts) { contact -> ContactRow(contact) { onPick(contact) } } }
    }
}

@Composable
private fun AlarmSetupScreen(onBack: () -> Unit, onSet: (Int, Int) -> Unit) {
    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        BackTitle("Despertador", onBack)
        Text("${"%02d:%02d".format(hour, minute)}", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(16.dp))
        StepperRow("Horas", hour, { hour = (it + 24) % 24 })
        StepperRow("Minutos", minute, { minute = (it + 60) % 60 }, step = 5)
        Spacer(Modifier.weight(1f))
        Button(onClick = { onSet(hour, minute) }, modifier = Modifier.fillMaxWidth().height(84.dp), shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange)) {
            Text("Pôr o despertador", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StepperRow(label: String, value: Int, onChange: (Int) -> Unit, step: Int = 1) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
        Button(onClick = { onChange(value - step) }, modifier = Modifier.size(72.dp), shape = CircleShape) { Text("−", fontSize = 32.sp) }
        Text("%02d".format(value), fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Button(onClick = { onChange(value + step) }, modifier = Modifier.size(72.dp), shape = CircleShape) { Text("+", fontSize = 32.sp) }
    }
}

@Composable
private fun DictateScreen(contact: Contact, app: ChatApp, listening: Boolean, onBack: () -> Unit, onSpeakTap: () -> Unit, onTyped: (String) -> Unit) {
    var typed by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        BackTitle("${app.displayNamePt} para ${contact.displayName}", onBack)
        Text("Toque no botão grande e diga a mensagem. Eu repito antes de enviar.", fontSize = 22.sp)
        Spacer(Modifier.height(14.dp))
        Button(onClick = onSpeakTap, modifier = Modifier.fillMaxWidth().height(96.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (listening) Red else Ink), shape = RoundedCornerShape(20.dp)) {
            Icon(Icons.Filled.Mic, null, Modifier.size(36.dp), tint = Color.White)
            Spacer(Modifier.width(10.dp))
            Text(if (listening) "A ouvir…" else "DIZER A MENSAGEM", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.height(16.dp))
        Text("Ou escreva aqui:", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = typed, onValueChange = { typed = it }, modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontSize = 22.sp), minLines = 3)
        Spacer(Modifier.height(12.dp))
        Button(onClick = { if (typed.isNotBlank()) onTyped(typed.trim()) }, modifier = Modifier.fillMaxWidth().height(76.dp),
            shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Green)) {
            Text("Continuar", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ConfirmationScreen(promptPt: String, onYes: () -> Unit, onNo: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Text(promptPt, fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Ink)
        Spacer(Modifier.weight(1f))
        Button(onClick = onYes, modifier = Modifier.fillMaxWidth().height(96.dp), shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("SIM", fontSize = 32.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(14.dp))
        Button(onClick = onNo, modifier = Modifier.fillMaxWidth().height(96.dp), shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("NÃO", fontSize = 32.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun DisambiguationScreen(candidates: List<Contact>, onBack: () -> Unit, onPick: (Contact) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        BackTitle("Qual destas pessoas?", onBack)
        Text("Encontrei mais do que uma. Toque na pessoa certa.", fontSize = 22.sp)
        Spacer(Modifier.height(10.dp))
        LazyColumn { items(candidates) { contact -> ContactRow(contact) { onPick(contact) } } }
    }
}

@Composable
private fun PinGateScreen(onBack: () -> Unit, onUnlocked: () -> Unit, verify: (String) -> Boolean) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        BackTitle("Área do cuidador", onBack)
        Text("Introduza o PIN do cuidador.", fontSize = 22.sp)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = pin, onValueChange = { pin = it.filter { c -> c.isDigit() }.take(8) },
            modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(fontSize = 28.sp),
            placeholder = { Text("PIN", fontSize = 24.sp) }, singleLine = true)
        if (error.isNotBlank()) Text(error, color = Red, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(14.dp))
        Button(onClick = { if (verify(pin)) onUnlocked() else error = "PIN errado. Tente outra vez." },
            modifier = Modifier.fillMaxWidth().height(76.dp), shape = RoundedCornerShape(18.dp)) {
            Text("Entrar", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
        Text("PIN inicial do piloto: 1234 — mude-o nas definições a seguir.", fontSize = 16.sp, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun CaregiverSettingsScreen(activity: MainActivity, settings: SettingsStore, onBack: () -> Unit, onOpenLog: () -> Unit) {
    val context = LocalContext.current
    var variant by remember { mutableStateOf(settings.ptVariant) }
    var sosName by remember { mutableStateOf(settings.sosContactName) }
    var sosNumber by remember { mutableStateOf(settings.sosNumber) }
    var vlmConsent by remember { mutableStateOf(settings.vlmConsent) }
    var vlmEndpoint by remember { mutableStateOf(settings.vlmEndpoint) }
    var vlmKey by remember { mutableStateOf(settings.vlmApiKey) }
    var newPin by remember { mutableStateOf("") }
    var savedMsg by remember { mutableStateOf("") }
    val kiosk = remember { KioskManager(context) }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { BackTitle("Definições do cuidador", onBack) }
        item { Text("Português", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        item {
            Row {
                FilterChip(selected = variant == PtVariant.PT_PT, onClick = { variant = PtVariant.PT_PT; settings.ptVariant = variant },
                    label = { Text("Portugal (pt-PT)", fontSize = 18.sp) }, modifier = Modifier.height(56.dp))
                Spacer(Modifier.width(10.dp))
                FilterChip(selected = variant == PtVariant.PT_BR, onClick = { variant = PtVariant.PT_BR; settings.ptVariant = variant },
                    label = { Text("Brasil (pt-BR)", fontSize = 18.sp) }, modifier = Modifier.height(56.dp))
            }
        }
        item { Spacer(Modifier.height(14.dp)); Text("SOS", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        item { OutlinedTextField(sosName, { sosName = it }, label = { Text("Nome (ex.: Filho João)") }, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)) }
        item { OutlinedTextField(sosNumber, { sosNumber = it }, label = { Text("Número SOS (vazio = 112)") }, modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)) }
        item {
            Button(onClick = { settings.sosContactName = sosName; settings.sosNumber = sosNumber; savedMsg = "Guardado." },
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(top = 8.dp)) { Text("Guardar SOS e português", fontSize = 20.sp) }
        }
        item { Spacer(Modifier.height(14.dp)); Text("Ajuda visual (nível 3, opcional)", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = vlmConsent, onCheckedChange = { vlmConsent = it; settings.vlmConsent = it }, modifier = Modifier.size(64.dp))
                Text("Autorizo enviar o conteúdo do ecrã para um modelo de visão quando o Dumpphone não perceber um pedido. Sem chave configurada, corre apenas uma demonstração.", fontSize = 17.sp)
            }
        }
        item { OutlinedTextField(vlmEndpoint, { vlmEndpoint = it; settings.vlmEndpoint = it }, label = { Text("Endereço do modelo de visão (opcional)") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(vlmKey, { vlmKey = it; settings.vlmApiKey = it }, label = { Text("Chave do modelo de visão (opcional)") }, modifier = Modifier.fillMaxWidth()) }
        item { Spacer(Modifier.height(14.dp)); Text("Telefone e permissões", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        item {
            Button(onClick = { context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) },
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(top = 6.dp)) { Text("Escolher Dumpphone como ecrã inicial", fontSize = 18.sp) }
        }
        item {
            Button(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) },
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(top = 6.dp)) { Text("Ativar acessibilidade do Dumpphone", fontSize = 18.sp) }
        }
        item {
            Text(
                "Modo quiosque (dispositivo dedicado): " + if (kiosk.isDeviceOwner()) "Dumpphone É o dono do dispositivo — quiosque disponível."
                else "ainda não provisionado. Veja docs/KIOSK.md.",
                fontSize = 17.sp, modifier = Modifier.padding(top = 8.dp),
            )
        }
        item { Spacer(Modifier.height(14.dp)); Text("Mudar o PIN do cuidador", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        item { OutlinedTextField(newPin, { newPin = it.filter { c -> c.isDigit() }.take(8) }, label = { Text("Novo PIN (4–8 números)") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Button(onClick = {
                try { settings.setPin(newPin); savedMsg = "PIN mudado." } catch (e: Exception) { savedMsg = e.message ?: "PIN inválido." }
            }, modifier = Modifier.fillMaxWidth().height(64.dp).padding(top = 8.dp)) { Text("Mudar PIN", fontSize = 20.sp) }
        }
        item {
            Button(onClick = onOpenLog, modifier = Modifier.fillMaxWidth().height(64.dp).padding(top = 12.dp)) { Text("Ver registo de ações", fontSize = 20.sp) }
        }
        if (savedMsg.isNotBlank()) item { Text(savedMsg, fontSize = 18.sp, color = Green, modifier = Modifier.padding(top = 10.dp)) }
        item { Spacer(Modifier.height(30.dp)) }
    }
}

@Composable
private fun ActionLogScreen(entries: List<String>, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        BackTitle("Registo de ações", onBack)
        if (entries.isEmpty()) Text("Ainda não há ações registadas.", fontSize = 20.sp)
        LazyColumn { items(entries) { entry -> Text(entry, fontSize = 19.sp, modifier = Modifier.padding(vertical = 6.dp)); HorizontalDivider() } }
    }
}

@Composable
private fun MessagesScreen(app: ChatApp, messages: List<String>, onBack: () -> Unit, onReadAgain: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        BackTitle("Mensagens do ${app.displayNamePt}", onBack)
        LazyColumn(Modifier.weight(1f)) {
            items(messages) { msg -> Text("• $msg", fontSize = 24.sp, modifier = Modifier.padding(vertical = 8.dp)) }
        }
        Button(onClick = onReadAgain, modifier = Modifier.fillMaxWidth().height(76.dp), shape = RoundedCornerShape(18.dp)) {
            Text("Ler outra vez", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NoticeScreen(textPt: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Text(textPt, fontSize = 26.sp, textAlign = TextAlign.Center, color = Ink)
        Spacer(Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(84.dp), shape = RoundedCornerShape(20.dp)) {
            Text("Voltar ao início", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}
