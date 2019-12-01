package com.midsto.app.samplecode

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*

private const val TAG = "DeviceDataCollectorUtils"

class DeviceDataCollectorUtils(context: Context) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    init {
        initGpsLocation(context)
    }

    fun destroy() {
        stopLocationUpdates()
    }

    fun registerHeadsetPlugged(context: Context) {
        val headsetPluggedReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                if(intent?.action===Intent.ACTION_HEADSET_PLUG) {
                    val state = intent.getIntExtra("state", -1)
                    when(state) {
                        0 -> Log.d(TAG, "Headset is unplugged")
                        1 -> Log.d(TAG, "Headset is plugged")
                        else -> Log.e(TAG, "Illegal headset state is")

                    }
                }
            }
        }

        LocalBroadcastManager.getInstance(context).registerReceiver(headsetPluggedReceiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))
    }

    private fun initGpsLocation(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        locationRequest = LocationRequest()
        locationRequest.interval = 1000             // 1 secs
        locationRequest.fastestInterval = 1000      // 1 secs
        locationRequest.smallestDisplacement = 1f   // 1m
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.lastLocation
                    Log.d(TAG, "Location $location")
                }
            }
        }
    }

    fun gpsLocation(context: Context) {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}