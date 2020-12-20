package com.example.livetrackingapp

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (isPermissionGranted()) {
            initMap()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(
                ACCESS_COARSE_LOCATION
            )
        ) {
            AlertDialog.Builder(this)
                .setMessage("Grant Location Permission to access Map")
                .setPositiveButton("Okay") { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
                        PERMISSION_REQUEST_CODE
                    )
                }
                .create().show()
        } else
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_CODE
            )

    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            applicationContext,
            ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.addMarker(
            MarkerOptions()
                .position(LatLng(0.0, 0.0))
                .title("Marker")
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show()
            } else {
                requestPermissions()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 200
    }
}