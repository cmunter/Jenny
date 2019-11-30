package com.midsto.app.samplecode

import android.content.Context
import android.content.Intent
import android.net.Uri

class AppActionsUtils {

    /**
     * https://developers.google.com/maps/documentation/urls/android-intents
     */
    fun launchMapNavigationHome(context: Context) {
        val gmmIntentUri = Uri.parse("google.navigation:q=Home")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        context.startActivity(mapIntent)
    }
}