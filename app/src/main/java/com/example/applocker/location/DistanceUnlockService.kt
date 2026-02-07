package com.example.applocker.location

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.applocker.monitor.LockManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class DistanceUnlockService : Service() {

    private val lockManager = LockManager()
    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var totalDistanceMeters = 0f
    private var lastLocation: Location? = null
    private var requiredDistance = 0f

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val previous = lastLocation
            if (previous != null) {
                totalDistanceMeters += previous.distanceTo(location)
                if (totalDistanceMeters >= requiredDistance) {
                    unlockAndStop()
                }
            }
            lastLocation = location
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        requiredDistance = intent?.getFloatExtra(EXTRA_DISTANCE_METERS, 0f) ?: 0f
        if (requiredDistance <= 0f) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission missing, cannot track distance.")
            stopSelf()
            return START_NOT_STICKY
        }
        requestUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        locationClient.removeLocationUpdates(callback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun requestUpdates() {
        val request = LocationRequest.Builder(UPDATE_INTERVAL_MS)
            .setMinUpdateIntervalMillis(UPDATE_INTERVAL_MS)
            .build()
        locationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    private fun unlockAndStop() {
        lockManager.setLocked(this, false)
        lockManager.markUnlocked(this)
        stopSelf()
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val EXTRA_DISTANCE_METERS = "distance_meters"
        private const val UPDATE_INTERVAL_MS = 5_000L
        private const val TAG = "DistanceUnlockService"
    }
}
