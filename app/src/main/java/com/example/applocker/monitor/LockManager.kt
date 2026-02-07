package com.example.applocker.monitor

import android.content.Context
import com.example.applocker.data.LockedApp

class LockManager {

    fun saveLockRule(context: Context, packageName: String, minutes: Long, distanceMeters: Float) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rule = LockedApp(packageName, minutes, distanceMeters)
        prefs.edit()
            .putString(KEY_PACKAGE, rule.packageName)
            .putLong(KEY_MINUTES, rule.lockAfterMinutes)
            .putFloat(KEY_DISTANCE, rule.unlockDistanceMeters)
            .apply()
    }

    fun loadLockRule(context: Context): LockedApp? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val packageName = prefs.getString(KEY_PACKAGE, null) ?: return null
        val minutes = prefs.getLong(KEY_MINUTES, 0L)
        val distance = prefs.getFloat(KEY_DISTANCE, 0f)
        if (minutes <= 0 || distance <= 0f) {
            return null
        }
        return LockedApp(packageName, minutes, distance)
    }

    fun markUnlocked(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_UNLOCKED, System.currentTimeMillis()).apply()
    }

    fun setLocked(context: Context, locked: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_LOCKED, locked).apply()
    }

    fun isLocked(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_LOCKED, false)
    }

    fun lastUnlockedAt(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_UNLOCKED, 0L)
    }

    companion object {
        private const val PREFS_NAME = "lock_rules"
        private const val KEY_PACKAGE = "package_name"
        private const val KEY_MINUTES = "lock_minutes"
        private const val KEY_DISTANCE = "unlock_distance"
        private const val KEY_LAST_UNLOCKED = "last_unlocked"
        private const val KEY_IS_LOCKED = "is_locked"
    }
}
