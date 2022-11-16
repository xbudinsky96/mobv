package com.example.zadanie.utilities

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun getDistanceFromLatLonInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371; // Radius of the earth in km
    val dLat = deg2rad(lat2-lat1);  // deg2rad below
    val dLon = deg2rad(lon2-lon1);
    val a =
        sin(dLat/2) * sin(dLat/2) +
                cos(deg2rad(lat1)) * cos(deg2rad(lat2)) *
                sin(dLon/2) * sin(dLon/2)
    ;
    val c = 2 * atan2(sqrt(a), sqrt(1-a));
    val d = R * c; // Distance in km
    return d;
}

fun getDistanceFromLatLonInM(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000; // Radius of the earth in m
    val dLat = deg2rad(lat2-lat1);  // deg2rad below
    val dLon = deg2rad(lon2-lon1);
    val a =
        sin(dLat/2) * sin(dLat/2) +
                cos(deg2rad(lat1)) * cos(deg2rad(lat2)) *
                sin(dLon/2) * sin(dLon/2)
    ;
    val c = 2 * atan2(sqrt(a), sqrt(1-a));
    val d = R * c; // Distance in m
    return d;
}

private fun deg2rad(deg: Double): Double {
    return deg * (Math.PI/180)
}