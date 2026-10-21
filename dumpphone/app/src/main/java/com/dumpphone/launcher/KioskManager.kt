package com.dumpphone.launcher

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build

/**
 * Phase 7 — Device Owner / LockTask (kiosk) readiness.
 * The launcher works as a normal HOME app without Device Owner; when the
 * caregiver provisions Device Owner (see docs/KIOSK.md), this enables
 * LockTask pinning so the user cannot leave Dumpphone accidentally.
 * No custom ROM until this path proves out on one phone model (per plan).
 */
class KioskManager(private val context: Context) {
    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val admin = ComponentName(context, DumpphoneAdminReceiver::class.java)

    fun isDeviceOwner(): Boolean = dpm.isDeviceOwnerApp(context.packageName)

    /** Apply kiosk policies. Safe no-op (returns false) when not Device Owner. */
    fun applyKioskPoliciesIfDeviceOwner(): Boolean {
        if (!isDeviceOwner()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dpm.setLockTaskPackages(admin, arrayOf(context.packageName, "com.whatsapp", "com.facebook.orca", "com.android.dialer", "com.google.android.dialer"))
        }
        return true
    }

    fun startKioskIfAllowed(activity: android.app.Activity): Boolean {
        if (!isDeviceOwner()) return false
        applyKioskPoliciesIfDeviceOwner()
        return try {
            activity.startLockTask()
            true
        } catch (e: IllegalStateException) {
            false
        }
    }

    fun stopKiosk(activity: android.app.Activity) {
        try { activity.stopLockTask() } catch (_: Exception) {}
    }
}
