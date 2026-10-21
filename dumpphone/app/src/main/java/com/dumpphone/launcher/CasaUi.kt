package com.dumpphone.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dumpphone.data.Contact
import com.dumpphone.data.MockThread
import com.dumpphone.safety.ConfirmationPolicy
import com.dumpphone.voice.ChatApp
import com.dumpphone.voice.ParsedIntent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal val Cream = Color(0xFFF8F1E7)
internal val InkBrown = Color(0xFF2C2416)
internal val Muted = Color(0xFF8A7B68)
internal val CardWhite = Color(0xFFFFFCF8)
internal val MicOrange = Color(0xFFFF7A2F)
internal val HangupRed = Color(0xFFE23B3B)
internal val CallGreen = Color(0xFF2E9B4A)
internal val SosRed = Color(0xFFE10600)

@Composable
internal fun CasaScaffold(
    onLigar: () -> Unit,
    onFalar: () -> Unit,
    onDesligar: () -> Unit,
    listening: Boolean,
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Cream)) {
        Box(Modifier.weight(1f).fillMaxWidth()) { content() }
        CasaDock(onLigar, onFalar, onDesligar, listening)
    }
}

@Composable
private fun CasaDock(onLigar: () -> Unit, onFalar: () -> Unit, onDesligar: () -> Unit, listening: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DockButton("Ligar", Icons.Filled.Call, CallGreen, 64.dp, onLigar)
        DockButton(if (listening) "A ouvir" else "FALAR", Icons.Filled.Mic, MicOrange, 92.dp, onFalar)
        DockButton("Desligar", Icons.Filled.CallEnd, HangupRed, 64.dp, onDesligar)
    }
}

@Composable
private fun DockButton(label: String, icon: ImageVector, color: Color, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            Modifier.size(size).shadow(8.dp, CircleShape).clip(CircleShape).background(color),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(size * 0.42f))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = InkBrown, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
internal fun CasaBack(onBack: () -> Unit) {
    Surface(onClick = onBack, shape = RoundedCornerShape(24.dp), color = CardWhite, shadowElevation = 2.dp) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = InkBrown)
            Spacer(Modifier.width(4.dp))
            Text("Voltar", color = InkBrown, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
internal fun CasaHome(
    statusText: String,
    favourites: List<Contact>,
    onTile: (PickerMode) -> Unit,
    onAlarm: () -> Unit,
    onSos: () -> Unit,
    onChats: () -> Unit,
    onContact: (Contact) -> Unit,
    onMore: () -> Unit,
    onCaregiver: () -> Unit,
    onLog: () -> Unit,
) {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val hello = when { hour < 12 -> "Bom dia"; hour < 20 -> "Boa tarde"; else -> "Boa noite" }
    val date = SimpleDateFormat("EEEE, d 'de' MMMM '·' HH:mm", Locale("pt", "PT")).format(Date())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "PT")) else it.toString() }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("$hello, Amélia!", color = InkBrown, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(date, color = Muted, fontSize = 14.sp)
            }
            Row {
                IconButton(onClick = onLog) { Icon(Icons.Filled.Menu, "Registo", tint = InkBrown) }
                IconButton(onClick = onCaregiver) { Icon(Icons.Filled.Settings, "Definições", tint = InkBrown) }
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = CardWhite, shadowElevation = 1.dp) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Mic, null, tint = MicOrange)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (statusText.isBlank()) "Toque em FALAR no centro, ou num botão grande." else statusText,
                    color = InkBrown, fontSize = 16.sp,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PastelTile("Ligar", "Chamar alguém", Color(0xFFE7F6EA), CallGreen, Icons.Filled.Call, Modifier.weight(1f)) { onTile(PickerMode.CALL) }
            PastelTile("Mensagens", "SMS do telemóvel", Color(0xFFE7F1FB), Color(0xFF3B82F6), Icons.Filled.Chat, Modifier.weight(1f)) { onTile(PickerMode.SMS) }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PastelTile("Despertador", "Acordar a horas", Color(0xFFFFF3E0), Color(0xFFE8890C), Icons.Filled.Alarm, Modifier.weight(1f)) { onAlarm() }
            PastelTile("WhatsApp", "Conversas", Color(0xFFE5F6EA), CallGreen, Icons.Filled.Forum, Modifier.weight(1f)) { onChats() }
        }
        Spacer(Modifier.height(10.dp))
        PastelTile(
            "SOS — Ajuda",
            "Emergência, 1 toque",
            SosRed,
            Color.White,
            Icons.Filled.Warning,
            Modifier.fillMaxWidth(),
            titleColor = Color.White,
            subColor = Color.White.copy(alpha = 0.9f),
        ) { onSos() }
        Spacer(Modifier.height(14.dp))
        Text("Pessoas importantes", color = InkBrown, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Toque numa pessoa para ligar", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            favourites.take(3).forEach { person ->
                PersonBubble(person) { onContact(person) }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onMore)) {
                Box(Modifier.size(58.dp).clip(CircleShape).background(Color(0xFFE7E0D6)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, "Mais", tint = InkBrown)
                }
                Text("Mais", color = InkBrown, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun PastelTile(
    title: String, subtitle: String, bg: Color, accent: Color, icon: ImageVector,
    modifier: Modifier, titleColor: Color = InkBrown, subColor: Color = Muted, onClick: () -> Unit,
) {
    Surface(modifier.height(108.dp).clickable(onClick = onClick), shape = RoundedCornerShape(18.dp), color = bg) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(26.dp))
            Text(title, color = titleColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(subtitle, color = subColor, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PersonBubble(contact: Contact, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        InitialAvatar(contact, 58.dp)
        Text(contact.displayName.substringBefore(" "), color = InkBrown, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(contact.relation ?: "", color = Muted, fontSize = 12.sp)
    }
}

@Composable
internal fun InitialAvatar(contact: Contact, size: androidx.compose.ui.unit.Dp) {
    val color = when (contact.id) {
        "demo-maria" -> Color(0xFFE07A5F)
        "demo-joao" -> Color(0xFF4C78D4)
        "demo-ana" -> Color(0xFF7B5EA7)
        "demo-carlos" -> Color(0xFFE0A106)
        "demo-maria-costa" -> Color(0xFFD4A017)
        "demo-familia" -> Color(0xFFE09A3E)
        else -> Color(0xFF8D6E63)
    }
    Box(Modifier.size(size).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
        Text(contact.displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.38f).sp)
    }
}

@Composable
internal fun CasaPeople(
    title: String,
    subtitle: String,
    contacts: List<Contact>,
    onBack: () -> Unit,
    onPick: (Contact) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        CasaBack(onBack)
        Spacer(Modifier.height(12.dp))
        Text(title, color = InkBrown, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Muted, fontSize = 15.sp)
        Spacer(Modifier.height(10.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(contacts) { contact ->
                Surface(shape = RoundedCornerShape(18.dp), color = CardWhite, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth().clickable { onPick(contact) }) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        InitialAvatar(contact, 52.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(contact.displayName, color = InkBrown, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(contact.relation ?: "", color = Muted, fontSize = 13.sp)
                            Text(contact.displayPhone ?: "", color = Muted, fontSize = 14.sp)
                        }
                        Icon(Icons.Filled.Call, null, tint = CallGreen)
                    }
                }
            }
        }
    }
}

@Composable
internal fun CasaListening(heard: String?) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(8.dp))
        Box(Modifier.size(108.dp).clip(CircleShape).background(MicOrange.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
            Box(Modifier.size(72.dp).clip(CircleShape).background(MicOrange), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Mic, null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("A ouvir... fale agora", color = InkBrown, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (heard != null) {
            Spacer(Modifier.height(8.dp))
            Text("Ouvi isto:", color = MicOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("“$heard”", color = InkBrown, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}

private fun comArtigo(contact: Contact): String =
    if (contact.relation in setOf("Filho", "Irmão", "Neto", "Marido", "Pai")) "o" else "a"

@Composable
internal fun CasaConfirm(intent: ParsedIntent, contact: Contact?, heard: String?, onYes: () -> Unit, onNo: () -> Unit, onBack: () -> Unit) {
    val prompt = ConfirmationPolicy.confirmationPromptPt(intent, contact?.displayName)
    val eyebrow = when (intent) {
        is ParsedIntent.CallContact -> "ANTES DE LIGAR, CONFIRME:"
        is ParsedIntent.SetAlarm -> "ANTES DE CONTINUAR, CONFIRME:"
        else -> "ANTES DE ENVIAR, CONFIRME:"
    }
    val footnote = when (intent) {
        is ParsedIntent.CallContact -> "Nenhuma chamada sai sem o seu “Sim”."
        is ParsedIntent.SetAlarm -> "O despertador só fica marcado com o seu “Sim”."
        else -> "Nada é enviado sem o seu “Sim”."
    }
    val question = buildAnnotatedString {
        when (intent) {
            is ParsedIntent.CallContact -> {
                val artigo = contact?.let { comArtigo(it) } ?: "a"
                val name = contact?.displayName ?: "esse contacto"
                append("Quer ligar para $artigo $name?")
            }
            is ParsedIntent.SendWhatsApp, is ParsedIntent.SendSms -> {
                val msg = when (intent) {
                    is ParsedIntent.SendWhatsApp -> intent.message
                    is ParsedIntent.SendSms -> intent.message
                    else -> ""
                }
                val first = contact?.displayName?.substringBefore(" ") ?: "essa pessoa"
                val artigo = contact?.let { comArtigo(it) } ?: "a"
                append("Quer enviar ")
                withStyle(SpanStyle(color = MicOrange, fontWeight = FontWeight.Bold)) { append("“$msg”") }
                append(" para $artigo $first?")
            }
            else -> append(prompt)
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        CasaBack(onBack)
        if (heard != null) CasaListening(heard) else Spacer(Modifier.height(12.dp))
        Surface(shape = RoundedCornerShape(18.dp), color = CardWhite, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (contact != null) {
                        InitialAvatar(contact, 44.dp)
                        Spacer(Modifier.width(10.dp))
                    }
                    Column {
                        Text(eyebrow, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        if (contact != null) {
                            Text("Para ${comArtigo(contact)} ${contact.displayName}", color = InkBrown, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(question, color = InkBrown, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(footnote, color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onYes, modifier = Modifier.weight(1f).height(64.dp), colors = ButtonDefaults.buttonColors(containerColor = CallGreen), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Filled.Check, null); Spacer(Modifier.width(6.dp)); Text("SIM", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = onNo, modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(2.dp, HangupRed)) {
                Icon(Icons.Filled.Close, null, tint = HangupRed); Spacer(Modifier.width(6.dp)); Text("NÃO", color = HangupRed, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text("Também pode dizer “sim” ou “não” em voz alta.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))
    }
}

@Composable
internal fun CasaChats(
    threads: List<MockThread>,
    contacts: List<Contact>,
    onBack: () -> Unit,
    onOpen: (MockThread) -> Unit,
    onReadAll: () -> Unit,
    appLabel: String = "WhatsApp",
) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            CasaBack(onBack)
            Surface(shape = RoundedCornerShape(20.dp), color = CallGreen) {
                Text(appLabel, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("As suas conversas", color = InkBrown, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Row {
            Text("Toque em ", color = Muted, fontSize = 15.sp)
            Text("Ouvir", color = MicOrange, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(" e eu leio em voz alta.", color = Muted, fontSize = 15.sp)
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(threads) { thread ->
                val contact = contacts.firstOrNull { it.id == thread.contactId }
                Surface(shape = RoundedCornerShape(18.dp), color = CardWhite, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (contact != null) InitialAvatar(contact, 48.dp) else InitialAvatar(Contact(thread.contactId, thread.title), 48.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(thread.title, color = InkBrown, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(thread.timeLabel, color = Muted, fontSize = 12.sp)
                                Spacer(Modifier.weight(1f))
                                if (thread.unread > 0) {
                                    Box(Modifier.size(22.dp).clip(CircleShape).background(CallGreen), contentAlignment = Alignment.Center) {
                                        Text(thread.unread.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Icon(Icons.Filled.DoneAll, null, tint = Color(0xFFB7A898), modifier = Modifier.size(18.dp))
                                }
                            }
                            Text(thread.relation, color = Muted, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(thread.preview, color = InkBrown, fontSize = 14.sp, maxLines = 1, modifier = Modifier.weight(1f))
                                Surface(onClick = { onOpen(thread) }, shape = RoundedCornerShape(20.dp), color = Color(0xFFF3E6D4)) {
                                    Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.PlayArrow, null, tint = MicOrange, modifier = Modifier.size(14.dp))
                                        Text("Ouvir", color = MicOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Button(onClick = onReadAll, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = CallGreen), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("Ler tudo em voz alta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
internal fun CasaReadAloud(
    thread: MockThread,
    contact: Contact?,
    onBack: () -> Unit,
    onAgain: () -> Unit,
    chip: String = "A ler",
    kicker: String = "WHATSAPP",
    againLabel: String = "Ler outra vez",
) {
    val relation = (contact?.relation ?: thread.relation).uppercase()
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CasaBack(onBack)
            Surface(shape = RoundedCornerShape(20.dp), color = CallGreen) {
                Text(chip, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (contact != null) InitialAvatar(contact, 56.dp) else InitialAvatar(Contact(thread.contactId, thread.title), 56.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(if (relation.isBlank()) kicker else "$kicker · $relation", color = Muted, fontSize = 12.sp)
                Text(thread.title, color = InkBrown, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Surface(shape = RoundedCornerShape(18.dp), color = CardWhite, shadowElevation = 1.dp) {
            Column(Modifier.padding(16.dp)) {
                Text("HOJE · ${thread.timeLabel}", color = MicOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("“${thread.body}”", color = InkBrown, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(14.dp))
        Button(onClick = onAgain, modifier = Modifier.fillMaxWidth().height(58.dp), colors = ButtonDefaults.buttonColors(containerColor = CallGreen), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text(againLabel, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
internal fun CasaDictate(contact: Contact, app: ChatApp, draft: String, onBack: () -> Unit) {
    val appName = when (app) { ChatApp.WHATSAPP -> "WhatsApp"; ChatApp.SMS -> "Mensagem" }
    val first = contact.displayName.substringBefore(" ")
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        CasaBack(onBack)
        Spacer(Modifier.height(12.dp))
        Text("$appName para ${comArtigo(contact)} $first", color = InkBrown, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Row {
            Text("Toque em ", color = InkBrown, fontSize = 16.sp)
            Text("FALAR", color = MicOrange, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(" e diga a mensagem.", color = InkBrown, fontSize = 16.sp)
        }
        Text("Eu repito antes de enviar.", color = InkBrown, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        Surface(shape = RoundedCornerShape(18.dp), color = CardWhite, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialAvatar(contact, 44.dp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("MENSAGEM", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${contact.displayName} · ${contact.relation ?: ""}", color = InkBrown, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFF6EFE4), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        draft.ifBlank { "A sua frase aparece aqui, em letras grandes." },
                        color = InkBrown, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun CasaAlarm(onBack: () -> Unit, onSet: (Int, Int) -> Unit) {
    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(30) }
    Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth()) { CasaBack(onBack) }
        Spacer(Modifier.height(8.dp))
        Text("Despertador", color = InkBrown, fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
        Text("%02d:%02d".format(hour, minute), color = InkBrown, fontSize = 64.sp, fontWeight = FontWeight.Bold)
        Text("Todos os dias", color = Muted, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(18.dp), color = CardWhite, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Step("Horas", hour) { hour = (hour + it + 24) % 24 }
                HorizontalDivider(color = Cream)
                Step("Minutos", minute) { minute = (minute + it + 60) % 60 }
            }
        }
        Spacer(Modifier.height(14.dp))
        Button(onClick = { onSet(hour, minute) }, modifier = Modifier.fillMaxWidth().height(58.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8890C)), shape = RoundedCornerShape(16.dp)) {
            Text("Pôr o despertador", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
private fun Step(label: String, value: Int, onDelta: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = InkBrown, fontSize = 18.sp, modifier = Modifier.width(90.dp))
        FilledIconButton(onClick = { onDelta(-1) }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFFFF3E0))) {
            Icon(Icons.Filled.Remove, "menos", tint = Color(0xFFE8890C))
        }
        Text("%02d".format(value), color = InkBrown, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        FilledIconButton(onClick = { onDelta(1) }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFE8890C))) {
            Icon(Icons.Filled.Add, "mais", tint = Color.White)
        }
    }
}

@Composable
internal fun CasaDisambiguate(candidates: List<Contact>, onBack: () -> Unit, onPick: (Contact) -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        CasaBack(onBack)
        Spacer(Modifier.height(12.dp))
        Text("Qual destas pessoas?", color = InkBrown, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Encontrei mais do que uma pessoa. Toque na pessoa certa.", color = Muted, fontSize = 16.sp)
        Spacer(Modifier.height(10.dp))
        candidates.forEach { contact ->
            Surface(shape = RoundedCornerShape(18.dp), color = CardWhite, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onPick(contact) }) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    InitialAvatar(contact, 52.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(contact.displayName, color = InkBrown, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(contact.relation ?: "", color = Muted, fontSize = 13.sp)
                        Text(contact.displayPhone ?: "", color = Muted, fontSize = 14.sp)
                    }
                    Icon(Icons.Filled.Call, null, tint = CallGreen)
                }
            }
        }
        Surface(shape = RoundedCornerShape(16.dp), color = CardWhite, modifier = Modifier.fillMaxWidth()) {
            Text("Não escolho por si. Se não for nenhuma, toque em Voltar.", color = InkBrown, modifier = Modifier.padding(14.dp), fontSize = 15.sp)
        }
    }
}

@Composable
internal fun CasaMockCall(name: String, onHangup: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("A ligar…", color = Muted, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text(name, color = InkBrown, fontSize = 36.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text("Exemplo. Nenhuma chamada saiu do telemóvel.", color = Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onHangup, colors = ButtonDefaults.buttonColors(containerColor = HangupRed), shape = RoundedCornerShape(18.dp), modifier = Modifier.height(64.dp).fillMaxWidth()) {
            Icon(Icons.Filled.CallEnd, null); Spacer(Modifier.width(8.dp)); Text("Desligar", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
