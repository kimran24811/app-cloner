package com.opencloner.app.engine

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CloneAdminReceiver : DeviceAdminReceiver() {

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Log.i(TAG, "OpenCloner Sandbox profile provisioning complete.")
        
        val manager = WorkProfileManager.getInstance(context)
        manager.onProfileCreated(context)
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "OpenCloner Device Admin enabled.")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w(TAG, "OpenCloner Device Admin disabled.")
    }

    companion object {
        private const val TAG = "CloneAdminReceiver"
    }
}
