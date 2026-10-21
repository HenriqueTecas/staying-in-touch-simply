package com.dumpphone.actions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import com.dumpphone.data.DummyMode
import com.dumpphone.data.PtVariant

/**
 * Tier 1 — deterministic Android intents & deep links (plan: intent-first).
 * Intent *construction* is separated ([IntentFactory], pure) from *launching*
 * so the URIs can be unit-tested without a device.
 */
object IntentFactory {
    fun dial(phone: String): Intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phone)}"))

    fun call(phone: String): Intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(phone)}"))

    fun sms(phone: String?, message: String): Intent {
        val uri = if (phone.isNullOrBlank()) "sms:" else "sms:${Uri.encode(phone)}"
        return Intent(Intent.ACTION_SENDTO, Uri.parse(uri)).putExtra("sms_body", message)
    }

    fun setAlarm(hour: Int, minute: Int, label: String?): Intent =
        Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            if (!label.isNullOrBlank()) putExtra(AlarmClock.EXTRA_MESSAGE, label)
        }

    /** WhatsApp click-to-chat. [internationalDigits] must come from [PhoneNumbers]. */
    fun whatsAppChat(internationalDigits: String, message: String): Intent {
        val url = buildString {
            append("https://wa.me/").append(internationalDigits)
            if (message.isNotBlank()) append("?text=").append(Uri.encode(message))
        }
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }

    fun whatsAppOpen(): Intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/"))

    /** Messenger: m.me/<username> when known, otherwise just open the app / m.me. */
    fun messengerChat(username: String?, message: String): Intent {
        val url = if (!username.isNullOrBlank()) "https://m.me/$username" else "https://m.me/"
        return Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            // The text cannot be pre-filled via m.me; Tier 2 (accessibility) types it after confirmation.
            putExtra(Intent.EXTRA_TEXT, message)
        }
    }

    fun openPackage(packageName: String): Intent? = null // resolved via PackageManager at launch time
}

class Tier1Actions(private val context: Context) {

    sealed interface LaunchResult {
        data object Launched : LaunchResult
        data class Failed(val reasonPt: String) : LaunchResult
    }

    private fun held(message: String = DummyMode.HELD_CALL): LaunchResult = LaunchResult.Failed(message)

    private fun launch(intent: Intent, chooserTitle: String? = null): LaunchResult = try {
        val toLaunch = if (chooserTitle != null) Intent.createChooser(intent, chooserTitle) else intent
        toLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(toLaunch)
        LaunchResult.Launched
    } catch (e: Exception) {
        LaunchResult.Failed("Não consegui abrir. ${e.message ?: ""}".trim())
    }

    fun dial(phone: String): LaunchResult = if (DummyMode.ENABLED) held() else launch(IntentFactory.dial(phone))

    /** Direct call; caller must have checked CALL_PHONE permission, else falls back to the dialler. */
    fun call(phone: String, hasCallPermission: Boolean): LaunchResult = when {
        DummyMode.ENABLED -> held()
        hasCallPermission -> launch(IntentFactory.call(phone))
        else -> launch(IntentFactory.dial(phone))
    }

    fun sms(phone: String?, message: String): LaunchResult =
        if (DummyMode.ENABLED) held(DummyMode.HELD_MESSAGE) else launch(IntentFactory.sms(phone, message))

    fun setAlarm(hour: Int, minute: Int, label: String?): LaunchResult =
        launch(IntentFactory.setAlarm(hour, minute, label))

    fun whatsApp(phoneRaw: String?, message: String, variant: PtVariant): LaunchResult {
        if (DummyMode.ENABLED) return held(DummyMode.HELD_MESSAGE)
        val digits = PhoneNumbers.toInternationalDigits(phoneRaw, variant)
        return if (digits != null) launch(IntentFactory.whatsAppChat(digits, message))
        else {
            // No usable number: open WhatsApp so the user (or Tier 2) picks the chat.
            openApp("com.whatsapp").takeIf { it is LaunchResult.Launched }
                ?: LaunchResult.Failed("Não tenho o número desse contacto para o WhatsApp.")
        }
    }

    fun messenger(username: String?, message: String): LaunchResult =
        if (DummyMode.ENABLED) held(DummyMode.HELD_MESSAGE) else launch(IntentFactory.messengerChat(username, message))

    fun openApp(packageName: String): LaunchResult {
        if (DummyMode.ENABLED) return held(DummyMode.HELD_MESSAGE)
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return LaunchResult.Failed("Essa aplicação não está instalada.")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent); LaunchResult.Launched
        } catch (e: Exception) {
            LaunchResult.Failed("Não consegui abrir a aplicação.")
        }
    }

    fun isInstalled(packageName: String): Boolean =
        context.packageManager.getLaunchIntentForPackage(packageName) != null
}
