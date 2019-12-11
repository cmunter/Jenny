package com.midsto.app.samplecode

import android.content.Context
import android.provider.Settings.Secure
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*


class DeviceIdUtils {

    fun getDeviceId(context: Context): String? {

        // TODO Use Firebase instance ID
        // implementation 'com.google.firebase:firebase-messaging:20.0.1'
        // String reliableIdentifier = FirebaseInstanceId.getInstance().getId();

        // https://en.wikipedia.org/wiki/Salt_(cryptography) Hashed value = SHA256 (Salt value + collector value)
        // Use same salt value (fixed salt) for the same collector type, This will make it possible to match the same value without knowing the specific value

        val androidUid = Secure.getString(context.contentResolver, Secure.ANDROID_ID)
        return if (androidUid.isEmpty()) {
            UUID.randomUUID().toString() // for emulator testing
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