package com.midsto.app.samplecode

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

private const val TAG = "DeviceDataCollectorUtils"

class DeviceDataCollectorUtils {

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
}