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

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import org.breezyweather.R
import org.breezyweather.background.weather.WeatherUpdateJob
import org.breezyweather.common.extensions.notificationBuilder
import org.breezyweather.common.extensions.workManager
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.presenters.notification.WidgetNotificationIMP
import org.json.JSONObject

class WatchdogService : Service() {

    private var alarmManager: AlarmManager? = null
    private var alarmPendingIntent: PendingIntent? = null
    private var serviceStartTime = 0L
    private var foregroundNotifId = Notifications.ID_WATCHDOG_KEEPALIVE

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        Log.d(TAG, "WatchdogService created")
        alarmManager = getSystemService()
        serviceStartTime = SystemClock.elapsedRealtime()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "WatchdogService onStartCommand")

        val source = intent?.getStringExtra(EXTRA_RESTART_SOURCE) ?: "sticky"
        incrementRestartCount(source)

        foregroundNotifId = if (WidgetNotificationIMP.isEnabled(this)) {
            Notifications.ID_WIDGET
        } else {
            Notifications.ID_WATCHDOG_KEEPALIVE
        }
        val notification = getForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                foregroundNotifId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            startForeground(foregroundNotifId, notification)
        }

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "BreezyWeather:WatchdogHeartbeat",
        )
        wakeLock.acquire(30_000L)
        Thread {
            try {
                performHeartbeat()
            } finally {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            }
            scheduleNextAlarm()
            stopSelf()
        }.start()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        if (foregroundNotifId == Notifications.ID_WIDGET) {
            stopForeground(STOP_FOREGROUND_DETACH)
        }
        Log.d(TAG, "WatchdogService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun getForegroundNotification(): Notification {
        if (foregroundNotifId == Notifications.ID_WIDGET) {
            getExistingNotification(Notifications.ID_WIDGET)?.let { return it }

            return notificationBuilder(Notifications.CHANNEL_WIDGET) {
                setSmallIcon(R.drawable.ic_running_in_background)
                setContentTitle(getString(R.string.notification_running_in_background))
                setOngoing(true)
                setShowWhen(false)
                priority = NotificationCompat.PRIORITY_MIN
            }.build()
        }

        return notificationBuilder(Notifications.CHANNEL_WATCHDOG) {
            setSmallIcon(R.drawable.ic_running_in_background)
            setContentTitle(getString(R.string.watchdog_notification_title))
            setContentText(getString(R.string.notification_running_in_background))
            setOngoing(true)
            setShowWhen(false)
            priority = if (isXiaomiDevice()) {
                NotificationCompat.PRIORITY_LOW
            } else {
                NotificationCompat.PRIORITY_MIN
            }
        }.build()
    }

    private fun getExistingNotification(id: Int): Notification? {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        return nm?.activeNotifications?.firstOrNull { it.id == id }?.notification
    }

    fun updateNotification() {
        if (foregroundNotifId == Notifications.ID_WIDGET) {
            return
        }

        val notification = getForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                foregroundNotifId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            startForeground(foregroundNotifId, notification)
        }
    }

    private fun performHeartbeat() {
        Log.d(TAG, "Performing heartbeat check")

        val workQuery = WorkQuery.Builder
            .fromUniqueWorkNames(listOf(WEATHER_UPDATE_AUTO_WORK_NAME))
            .addStates(listOf(WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED))
            .build()

        val healthyWorks = workManager.getWorkInfos(workQuery).get()

        val jobStatus = if (healthyWorks.isEmpty()) {
            Log.d(TAG, "Re-enqueued WeatherUpdateJob - was not found in RUNNING/ENQUEUED state")
            WeatherUpdateJob.setupTask(this)
            "re-enqueued"
        } else {
            Log.d(TAG, "WeatherUpdateJob is healthy (RUNNING or ENQUEUED)")
            "healthy"
        }

        writeDiagnostic(jobStatus)
        updateNotification()
    }

    private fun writeDiagnostic(jobStatus: String) {
        val prefs = getSharedPreferences("watchdog_diagnostics", Context.MODE_PRIVATE)
        val heartbeatCount = prefs.getInt("heartbeat_count", 0) + 1
        val uptimeMs = SystemClock.elapsedRealtime() - serviceStartTime

        val diagnostic = JSONObject().apply {
            put("timestamp", System.currentTimeMillis())
            put("heartbeat_count", heartbeatCount)
            put("uptime_ms", uptimeMs)
            put("job_status", jobStatus)
        }

        prefs.edit()
            .putString("last_diagnostic", diagnostic.toString())
            .putInt("heartbeat_count", heartbeatCount)
            .putLong("last_heartbeat_timestamp", System.currentTimeMillis())
            .apply()

        Log.d(TAG, "Diagnostic #$heartbeatCount: $diagnostic")
    }

    private fun incrementRestartCount(source: String) {
        val prefs = getSharedPreferences("watchdog_diagnostics", Context.MODE_PRIVATE)
        val count = prefs.getInt("restart_count", 0) + 1
        prefs.edit()
            .putInt("restart_count", count)
            .putString("last_restart_source", source)
            .putLong("last_restart_timestamp", System.currentTimeMillis())
            .apply()
        Log.d(TAG, "Restart #$count (source: $source)")
    }

    private fun scheduleNextAlarm() {
        val am = alarmManager ?: return

        val intent = Intent(this, WatchdogAlarmReceiver::class.java).apply {
            action = WatchdogAlarmReceiver.ACTION_WATCHDOG_ALARM
        }
        alarmPendingIntent = PendingIntent.getBroadcast(
            this,
            REQUEST_CODE_WATCHDOG_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val intervalMs = SettingsManager.getInstance(this).watchdogHeartbeatInterval.toLong() * 60 * 1000L
        val triggerAtMillis = SystemClock.elapsedRealtime() + intervalMs
        val nextAlarmWallClock = System.currentTimeMillis() + intervalMs

        try {
            am.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAtMillis,
                alarmPendingIntent!!,
            )
            Log.d(TAG, "Scheduled exact alarm in ${intervalMs / 1000 / 60} minutes")
        } catch (e: SecurityException) {
            Log.d(TAG, "setExactAndAllowWhileIdle restricted, falling back to setAndAllowWhileIdle: ${e.message}")
            am.setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAtMillis,
                alarmPendingIntent!!,
            )
        }

        getSharedPreferences("watchdog_diagnostics", Context.MODE_PRIVATE).edit()
            .putLong("next_alarm_timestamp", nextAlarmWallClock)
            .apply()
    }

    companion object {
        private const val TAG = "WatchdogService"
        private const val REQUEST_CODE_WATCHDOG_ALARM = 1001
        private const val WEATHER_UPDATE_AUTO_WORK_NAME = "WeatherUpdate-auto"
        internal const val EXTRA_RESTART_SOURCE = "restart_source"

        @Volatile
        var isRunning = false
            private set

        internal fun isXiaomiDevice(): Boolean {
            return Build.MANUFACTURER.lowercase() in listOf("xiaomi", "redmi", "poco")
        }

        fun start(context: Context, source: String = "manual") {
            val intent = Intent(context, WatchdogService::class.java).apply {
                putExtra(EXTRA_RESTART_SOURCE, source)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.w(TAG, "startForegroundService blocked (source=$source): ${e.javaClass.simpleName}: ${e.message}")
            }
            WatchdogRestartWorker.enqueue(context)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WatchdogService::class.java))
            WatchdogRestartWorker.cancel(context)
            val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val pi = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_WATCHDOG_ALARM,
                Intent(context, WatchdogAlarmReceiver::class.java).apply {
                    action = WatchdogAlarmReceiver.ACTION_WATCHDOG_ALARM
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            am?.cancel(pi)
            pi.cancel()
        }
    }
}