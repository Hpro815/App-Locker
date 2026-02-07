package com.example.applocker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.example.applocker.databinding.ActivityMainBinding
import com.example.applocker.monitor.LockManager
import com.example.applocker.monitor.UsageMonitorService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val lockManager = LockManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.requestUsageAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        binding.requestLocationAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        binding.startMonitoring.setOnClickListener {
            val intent = Intent(this, UsageMonitorService::class.java)
            startService(intent)
        }

        binding.openAppSettings.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        binding.saveLockRule.setOnClickListener {
            val packageName = binding.packageNameInput.text.toString().trim()
            val minutes = binding.lockMinutesInput.text.toString().toLongOrNull() ?: 0L
            val distanceMeters = binding.distanceInput.text.toString().toFloatOrNull() ?: 0f
            if (packageName.isNotEmpty() && minutes > 0 && distanceMeters > 0f) {
                lockManager.saveLockRule(this, packageName, minutes, distanceMeters)
            }
        }
    }
}
