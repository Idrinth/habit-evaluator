package de.idrinth.habitevaluator.android

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.idrinth.habitevaluator.android.persistence.AppDatabase
import de.idrinth.habitevaluator.android.persistence.RoomPlannerActivityRepository
import de.idrinth.habitevaluator.android.persistence.RoomPlannerGroupRepository
import de.idrinth.habitevaluator.android.persistence.RoomSlotConfirmationRepository
import de.idrinth.habitevaluator.android.persistence.RoomWeekPlannerSlotRepository
import de.idrinth.habitevaluator.shared.model.SlotConfirmation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles confirm, deny, and timeout actions for planner slot notifications.
 * When a planner notification fires, the user can tap Confirm or Deny.
 * If the notification times out (1 hour), an auto-deny is recorded.
 */
class PlannerConfirmationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CONFIRM = "de.idrinth.habitevaluator.PLANNER_CONFIRM"
        const val ACTION_DENY = "de.idrinth.habitevaluator.PLANNER_DENY"
        const val ACTION_TIMEOUT = "de.idrinth.habitevaluator.PLANNER_TIMEOUT"
        const val EXTRA_SLOT_ID = "planner_slot_id"
        const val EXTRA_ACTIVITY_ID = "planner_activity_id"
        const val EXTRA_GROUP_ID = "planner_group_id"
        const val EXTRA_USER_ID = "planner_user_id"
        const val EXTRA_NOTIFICATION_ID = "planner_notification_id"

        private const val TIMEOUT_MILLIS = 60 * 60 * 1000L // 1 hour

        fun scheduleTimeout(
            context: Context,
            notificationId: Int,
            slotId: String,
            activityId: String,
            groupId: String,
            userId: String
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                ?: return
            val pi = createTimeoutPendingIntent(context, notificationId, slotId, activityId, groupId, userId)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + TIMEOUT_MILLIS,
                pi
            )
        }

        fun cancelTimeout(context: Context, notificationId: Int, slotId: String, activityId: String, groupId: String, userId: String) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                ?: return
            val pi = createTimeoutPendingIntent(context, notificationId, slotId, activityId, groupId, userId)
            alarmManager.cancel(pi)
        }

        private fun createTimeoutPendingIntent(
            context: Context,
            notificationId: Int,
            slotId: String,
            activityId: String,
            groupId: String,
            userId: String
        ): PendingIntent {
            val intent = Intent(context, PlannerConfirmationReceiver::class.java).apply {
                action = ACTION_TIMEOUT
                putExtra(EXTRA_SLOT_ID, slotId)
                putExtra(EXTRA_ACTIVITY_ID, activityId)
                putExtra(EXTRA_GROUP_ID, groupId)
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getBroadcast(
                context, notificationId + 50000, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != ACTION_CONFIRM && action != ACTION_DENY && action != ACTION_TIMEOUT) return

        val slotId = intent.getStringExtra(EXTRA_SLOT_ID) ?: return
        val activityId = intent.getStringExtra(EXTRA_ACTIVITY_ID) ?: return
        val groupId = intent.getStringExtra(EXTRA_GROUP_ID) ?: return
        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        val confirmed = action == ACTION_CONFIRM

        // Cancel the timeout alarm if user acted before timeout
        if (action != ACTION_TIMEOUT) {
            cancelTimeout(context, notificationId, slotId, activityId, groupId, userId)
        }

        // Dismiss the notification
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancel(notificationId)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val slotRepo = RoomWeekPlannerSlotRepository(db.dayPlannerDao())
                val activityRepo = RoomPlannerActivityRepository(db.dayPlannerDao())
                val groupRepo = RoomPlannerGroupRepository(db.dayPlannerDao())
                val confirmationRepo = RoomSlotConfirmationRepository(db.dayPlannerDao())

                val slot = slotRepo.findById(slotId).orElse(null)
                val activity = activityRepo.findById(activityId).orElse(null)
                val group = groupRepo.findById(groupId).orElse(null)

                val confirmation = SlotConfirmation()
                confirmation.slot = slot
                confirmation.activity = activity
                confirmation.group = group
                confirmation.isConfirmed = confirmed
                confirmation.user = slot?.user ?: activity?.user

                confirmationRepo.saveSuspend(confirmation)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
