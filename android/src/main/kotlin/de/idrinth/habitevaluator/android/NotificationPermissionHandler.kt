package de.idrinth.habitevaluator.android

import android.content.Context

interface NotificationPermissionHandler {
    fun hasPermission(context: Context): Boolean
    fun permissionName(): String?
}
