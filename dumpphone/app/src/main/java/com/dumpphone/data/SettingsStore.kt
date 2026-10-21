package com.dumpphone.data

import android.content.Context
import com.dumpphone.safety.ActionLog
import com.dumpphone.safety.DEFAULT_CAREGIVER_PIN
import com.dumpphone.safety.PinHasher

/** Caregiver settings + action log persistence (SharedPreferences; no extra dependencies). */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("dumpphone_settings", Context.MODE_PRIVATE)

    var ptVariant: PtVariant
        get() = PtVariant.fromTag(prefs.getString("pt_variant", PtVariant.PT_PT.localeTag))
        set(value) { prefs.edit().putString("pt_variant", value.localeTag).apply() }

    var sosNumber: String
        get() = prefs.getString("sos_number", "") ?: ""
        set(value) { prefs.edit().putString("sos_number", value).apply() }

    var sosContactName: String
        get() = prefs.getString("sos_name", "") ?: ""
        set(value) { prefs.edit().putString("sos_name", value).apply() }

    var vlmConsent: Boolean
        get() = prefs.getBoolean("vlm_consent", false)
        set(value) { prefs.edit().putBoolean("vlm_consent", value).apply() }

    var vlmEndpoint: String
        get() = prefs.getString("vlm_endpoint", "") ?: ""
        set(value) { prefs.edit().putString("vlm_endpoint", value).apply() }

    var vlmApiKey: String
        get() = prefs.getString("vlm_api_key", "") ?: ""
        set(value) { prefs.edit().putString("vlm_api_key", value).apply() }

    var cloudSttEndpoint: String
        get() = prefs.getString("cloud_stt_endpoint", "") ?: ""
        set(value) { prefs.edit().putString("cloud_stt_endpoint", value).apply() }

    var cloudSttApiKey: String
        get() = prefs.getString("cloud_stt_api_key", "") ?: ""
        set(value) { prefs.edit().putString("cloud_stt_api_key", value).apply() }

    var kioskEnabledByCaregiver: Boolean
        get() = prefs.getBoolean("kiosk_enabled", false)
        set(value) { prefs.edit().putBoolean("kiosk_enabled", value).apply() }

    /** Custom nicknames: "contactId" -> "tia, mãezinha" (comma-separated, caregiver-edited). */
    fun nicknamesFor(contactId: String): List<String> =
        (prefs.getString("nick_$contactId", "") ?: "").split(",").map { it.trim() }.filter { it.isNotBlank() }

    fun setNicknamesFor(contactId: String, nicknames: List<String>) {
        prefs.edit().putString("nick_$contactId", nicknames.joinToString(", ")).apply()
    }

    // --- Caregiver PIN ---
    fun hasCustomPin(): Boolean = prefs.contains("pin_hash")
    fun verifyPin(pin: String): Boolean {
        val hash = prefs.getString("pin_hash", null)
        val salt = prefs.getString("pin_salt", null)
        return if (hash != null && salt != null) PinHasher.verify(pin, salt, hash)
        else pin == DEFAULT_CAREGIVER_PIN
    }
    fun setPin(pin: String) {
        require(PinHasher.isValidPinFormat(pin)) { "O PIN deve ter 4 a 8 números." }
        val salt = PinHasher.newSaltHex()
        prefs.edit().putString("pin_salt", salt).putString("pin_hash", PinHasher.hash(pin, salt)).apply()
    }

    // --- Action log ---
    val actionLog = ActionLog()
    init {
        val saved = prefs.getStringSet("action_log", emptySet())?.toList()?.sorted() ?: emptyList()
        // StringSet loses order; entries carry timestamps and ActionLog keeps insertion order of load.
        actionLog.load(saved.sortedBy { it.substringBefore("\t").toLongOrNull() ?: 0L })
    }
    fun log(textPt: String) {
        actionLog.add(textPt)
        prefs.edit().putStringSet("action_log", actionLog.serialized().toSet()).apply()
    }
    fun clearLog() {
        actionLog.load(emptyList())
        prefs.edit().remove("action_log").apply()
    }
}
