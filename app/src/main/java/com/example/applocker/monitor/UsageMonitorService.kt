package com.example.applocker.monitor

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.example.applocker.location.DistanceUnlockService

class UsageMonitorService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val lockManager = LockManager()

    private val pollRunnable = object : Runnable {
        override fun run() {
            checkUsage()
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler.post(pollRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkUsage() {
        val rule = lockManager.loadLockRule(this) ?: return
        val usageStats = getUsageStats(rule.packageName) ?: return
        val usedMillis = usageStats.totalTimeInForeground
        val lockAfterMillis = rule.lockAfterMinutes * 60_000

        if (usedMillis >= lockAfterMillis && !lockManager.isLocked(this)) {
            lockManager.setLocked(this, true)
            startDistanceUnlock(rule.unlockDistanceMeters)
        }
    }

    private fun startDistanceUnlock(requiredDistance: Float) {
        val intent = Intent(this, DistanceUnlockService::class.java)
            .putExtra(DistanceUnlockService.EXTRA_DISTANCE_METERS, requiredDistance)
        startService(intent)
    }

    private fun getUsageStats(packageName: String) = runCatching {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - LOOKBACK_WINDOW_MS
        usageStatsManager
            .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            .firstOrNull { it.packageName == packageName }
    }.onFailure {
        Log.w(TAG, "Usage stats query failed. Ensure usage access is granted.", it)
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.getOrNull()

    companion object {
        private const val TAG = "UsageMonitorService"
        private const val POLL_INTERVAL_MS = 15_000L
        private const val LOOKBACK_WINDOW_MS = 24 * 60 * 60 * 1000L
    }
}
