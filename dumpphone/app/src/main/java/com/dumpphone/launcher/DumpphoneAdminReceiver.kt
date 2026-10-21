package com.dumpphone.launcher

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

/** Device admin receiver required for Device Owner provisioning (Phase 7). */
class DumpphoneAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }
}
