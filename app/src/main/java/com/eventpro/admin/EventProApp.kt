package com.eventpro.admin

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.eventpro.admin.worker.MilestoneReminderWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class EventProApp : Application() {
    @Inject
    lateinit var workerFactory: MilestoneReminderWorker.Factory

    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(this, Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build())
        createNotificationChannel()
        scheduleReminders()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "milestone_reminders",
                "Milestone Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming milestones"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun scheduleReminders() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        val request = PeriodicWorkRequestBuilder<MilestoneReminderWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "milestone_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
