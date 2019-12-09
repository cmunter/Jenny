package com.midsto.app.samplecode

import android.provider.Settings.Secure
import java.math.BigInteger
import java.security.MessageDigest


class DeviceIdUtils {

    fun getDeviceId(): String? {
        val androidUid = Secure.ANDROID_ID
        return if (androidUid.isEmpty()) {
            // TODO generate random ID
            "12356789" // for emulator testing
        } else {
            var androidUidBytes = androidUid.toByteArray()
            val md5Digest = MessageDigest.getInstance("MD5")
            md5Digest.update(androidUidBytes)
            androidUidBytes = md5Digest.digest()
            val androidUidInt: BigInteger = BigInteger(androidUidBytes).abs()
            androidUidInt.toString(36)
        }
    }
}