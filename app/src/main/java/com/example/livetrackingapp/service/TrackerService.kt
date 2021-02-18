package com.example.livetrackingapp.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.livetrackingapp.R
import com.example.livetrackingapp.ui.MapActivity
import com.example.livetrackingapp.utils.Consts
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import timber.log.Timber
import java.lang.UnsupportedOperationException

class TrackerService : LifecycleService() {

    private val TAG = "TrackerService"
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            if (p0 != null && p0.lastLocation != null) {
                Log.d(TAG,"onLocationResult: lat-${p0.lastLocation.latitude} lng-${p0.lastLocation.longitude}")
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        throw UnsupportedOperationException("Not yet implemented")
    }

    private fun startLocationService() {
        val channelId = "location_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MapActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("Location Service")
            .setContentText("Running")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                "Location Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val locationRequest = LocationRequest()
        locationRequest.interval = 4000
        locationRequest.fastestInterval = 2000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        } else {
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(
                locationRequest, locationCallback,
                Looper.getMainLooper()
            )
            startForeground(150, notification.build())
        }
    }

    private fun stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(
            locationCallback
        )
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                Consts.ACTION_START_OR_RESUME_SERVICE -> {
                    startLocationService()
                    Log.d(TAG,"Service started or resumed")
                }
                Consts.ACTION_PAUSE_SERVICE -> {
                    Log.d(TAG,"Service paused")
                }
                Consts.ACTION_STOP_SERVICE -> {
                    stopLocationService()
                }
                else -> return super.onStartCommand(intent, flags, startId)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}