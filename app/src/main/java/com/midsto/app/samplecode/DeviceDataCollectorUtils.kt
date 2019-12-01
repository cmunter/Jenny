package com.midsto.app.samplecode

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*


private const val TAG = "DeviceDataCollectorUtils"

class DeviceDataCollectorUtils(private val context: Context) {

    private lateinit var headsetPluggedReceiver: BroadcastReceiver

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private lateinit var accelerometerSensor: Sensor
    private lateinit var accelerometerEventListener: SensorEventListener

    init {
        initHeadsetPlugged()
        initGpsLocation()
        initAccelerometer()
    }

    fun startCollector() {
        startHeadsetPlugged()
        startGpsLocation()
        startAccelerometer()
        startWifi()
    }

    fun destroy() {
        stopHeadsetPlugged()
        stopGpsLocation()
        stopAccelerometer()
    }

    private fun initHeadsetPlugged() {
        headsetPluggedReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                if(intent?.action===Intent.ACTION_HEADSET_PLUG) {
                    val state = intent.getIntExtra("state", -1)
                    when(state) {
                        0 -> Log.d(TAG, "Headset is unplugged")
                        1 -> Log.d(TAG, "Headset is plugged")
                        else -> Log.e(TAG, "Illegal headset state")
                    }
                }
            }
        }
    }

    private fun startHeadsetPlugged() {
        LocalBroadcastManager.getInstance(context).registerReceiver(headsetPluggedReceiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))
    }

    private fun stopHeadsetPlugged() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(headsetPluggedReceiver)
    }

    private fun initGpsLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
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

    private fun startGpsLocation() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private fun stopGpsLocation() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun initAccelerometer() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {

            accelerometerSensor = it

            accelerometerEventListener = object : SensorEventListener {
               override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

               override fun onSensorChanged(event: SensorEvent?) {
                   if(event!=null) {
                       Log.d(TAG, "Acceleromer ${event.values[0]}, ${event.values[1]}, ${event.values[2]}")
                   }
               }

           }
        }
    }

    private fun startAccelerometer() {
        sensorManager.registerListener(accelerometerEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun stopAccelerometer() {
        sensorManager.unregisterListener(accelerometerEventListener)
    }

    private fun startWifi() {
        val info = wifiManager.connectionInfo

        val isWiFiConnected = info != null && info.ssid != null && !info.ssid.contentEquals("<unknown ssid>")
        if (isWiFiConnected) {
            // Known SSID values: <unknown ssid>
            val ssidValue = info.ssid.replace("\"".toRegex(), "")
            val signalLevel = info.rssi
            Log.d(TAG, "WiFi $ssidValue, $signalLevel")
        }
    }
}