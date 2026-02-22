package de.idrinth.habitevaluator.android

import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

@RequiresApi(33)
class Api33PermissionHandler : NotificationPermissionHandler {

    companion object {
        private const val PERMISSION_POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"
    }

    override fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, PERMISSION_POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    override fun permissionName(): String = PERMISSION_POST_NOTIFICATIONS
}
