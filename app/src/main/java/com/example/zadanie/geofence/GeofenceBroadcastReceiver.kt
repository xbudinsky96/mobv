package com.example.zadanie.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.example.zadanie.api.apiService
import com.example.zadanie.model.loggedInUser
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver(val fragment: Fragment) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.errorCode)
                return
            }
        } else {
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            triggeringGeofences?.forEach {
                if (it.requestId.compareTo("geofence " + loggedInUser.uid) == 0) {
                    apiService.checkOutCompany(fragment)
                }
            }
        }
    }
}