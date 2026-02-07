package com.example.applocker.data

data class LockedApp(
    val packageName: String,
    val lockAfterMinutes: Long,
    val unlockDistanceMeters: Float,
    val lastUnlockedAtMillis: Long = 0L
)
