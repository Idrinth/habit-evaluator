package de.idrinth.habitevaluator.android

import android.content.Context

class PreApi33PermissionHandler : NotificationPermissionHandler {
    override fun hasPermission(context: Context): Boolean = true
    override fun permissionName(): String? = null
}
