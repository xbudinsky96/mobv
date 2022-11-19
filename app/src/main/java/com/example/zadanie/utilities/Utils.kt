package com.example.zadanie.utilities

import okhttp3.internal.and
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun getDistanceFromLatLon(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Pair<String, Int> {
    val R = 6371; // Radius of the earth in km
    val dLat = deg2rad(lat2-lat1);  // deg2rad below
    val dLon = deg2rad(lon2-lon1);
    val a =
        sin(dLat/2) * sin(dLat/2) +
                cos(deg2rad(lat1)) * cos(deg2rad(lat2)) *
                sin(dLon/2) * sin(dLon/2)
    ;
    val c = 2 * atan2(sqrt(a), sqrt(1-a));
    var d = R * c; // Distance in km
    var unit = "km"
    if (d.toInt() == 0) {
        d = (R * 1000 * c)
        unit = "m"
    }
    return Pair(unit, d.toInt())
}

private fun deg2rad(deg: Double): Double {
    return deg * (Math.PI/180)
}

fun hashPassword(password: String, salt: ByteArray): String {
    try {
        val md = MessageDigest.getInstance("SHA-512")
        md.update(salt)
        val bytes = md.digest(password.encodeToByteArray())
        val sb: StringBuilder = StringBuilder()
        for (element in bytes) {
            sb.append(((element.and(0xff)) + 0x100).toString(16)).substring(1)
        }
        return sb.toString()
    }
    catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""
}

fun getSalt(): ByteArray {
    val salt = ByteArray(16)
    SecureRandom().nextBytes(salt)
    return salt
}