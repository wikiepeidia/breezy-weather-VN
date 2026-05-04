/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.background.watchdog

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import androidx.work.WorkerParameters
import org.breezyweather.R
import org.breezyweather.background.weather.WeatherUpdateJob
import org.breezyweather.common.extensions.notificationBuilder
import org.breezyweather.common.extensions.setForegroundSafely
import org.breezyweather.common.extensions.workManager
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.remoteviews.Notifications
import java.util.concurrent.TimeUnit

class WatchdogRestartWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!SettingsManager.getInstance(applicationContext).watchdogEnabled) {
            Log.d(TAG, "Watchdog disabled - skipping restart check")
            return Result.success()
        }

        setForegroundSafely()

        val workQuery = WorkQuery.Builder
            .fromUniqueWorkNames(listOf(WEATHER_UPDATE_AUTO_WORK_NAME))
            .addStates(listOf(WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED))
            .build()

        val healthyWorks = applicationContext.workManager.getWorkInfos(workQuery).get()
        if (healthyWorks.isEmpty()) {
            Log.d(TAG, "WeatherUpdateJob not found in RUNNING/ENQUEUED - re-enqueueing via WorkManager fallback")
            WeatherUpdateJob.setupTask(applicationContext)
        } else {
            Log.d(TAG, "WeatherUpdateJob is healthy (RUNNING or ENQUEUED)")
        }

        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = applicationContext.notificationBuilder(Notifications.CHANNEL_WATCHDOG) {
            setSmallIcon(R.drawable.ic_running_in_background)
            setContentTitle(applicationContext.getString(R.string.notification_running_in_background))
            setOngoing(true)
            setShowWhen(false)
            priority = NotificationCompat.PRIORITY_MIN
        }.build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                Notifications.ID_WATCHDOG_KEEPALIVE,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            ForegroundInfo(Notifications.ID_WATCHDOG_KEEPALIVE, notification)
        }
    }

    companion object {
        private const val TAG = "WatchdogRestart"
        const val WORK_NAME = "WatchdogRestart"
        private const val WEATHER_UPDATE_AUTO_WORK_NAME = "WeatherUpdate-auto"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<WatchdogRestartWorker>(
                30,
                TimeUnit.MINUTES,
            ).build()
            context.workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
            Log.d(TAG, "Enqueued periodic restart check (30min)")
        }

        fun cancel(context: Context) {
            context.workManager.cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled periodic restart check")
        }
    }
}