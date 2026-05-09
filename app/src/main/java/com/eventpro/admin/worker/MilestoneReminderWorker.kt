package com.eventpro.admin.worker

import android.content.Context
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.eventpro.admin.data.local.AppDatabase
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

class MilestoneReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val db: AppDatabase
) : CoroutineWorker(appContext, workerParams) {

    @Singleton
    class Factory @Inject constructor(
        private val db: AppDatabase
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? {
            return if (workerClassName == MilestoneReminderWorker::class.java.name) {
                MilestoneReminderWorker(appContext, workerParameters, db)
            } else {
                null
            }
        }
    }

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
