package com.eventpro.admin.worker

import android.content.Context
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eventpro.admin.data.local.AppDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class MilestoneReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val db: AppDatabase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val in24h = now + TimeUnit.HOURS.toMillis(24)

        val milestones = db.timelineDao().getUpcomingMilestones(now, in24h)

        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)

        for (milestone in milestones) {
            val notification = NotificationCompat.Builder(applicationContext, "milestone_reminders")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(milestone.title)
                .setContentText(milestone.description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(milestone.id.toInt(), notification)
        }

        return Result.success()
    }
}
