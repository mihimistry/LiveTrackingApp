package com.example.livetrackingapp

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.livetrackingapp.databinding.ActivityMapBinding
import com.example.livetrackingapp.model.UserLocation
import com.example.livetrackingapp.utils.UserSharedPreference
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_map.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewBinding: ActivityMapBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

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

        viewBinding.edtSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.action == KeyEvent.ACTION_DOWN
                || event.action == KeyEvent.KEYCODE_ENTER
            ) {
                geoLocate()
            }

            return@setOnEditorActionListener false
        }
    }

    private fun geoLocate() {
        val geocoder = Geocoder(this)
        var addressList = ArrayList<Address>()
        addressList =
            geocoder.getFromLocationName(edt_search.text.toString(), 1) as ArrayList<Address>

        if (addressList.isNotEmpty()) {
            val address = addressList[0]
            Log.d(TAG, "geoLocate:${address}")
            moveCamera(LatLng(address.latitude, address.longitude), DEFAULT_ZOOM, address.locality)
        }
    }

    private fun getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
        } else {
            val location = fusedLocationClient.lastLocation
            location.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(Companion.TAG, "onComplete: found location!")
                    val currentLocation =
                        it.result as Location

                    moveCamera(
                        LatLng(currentLocation.latitude, currentLocation.longitude),
                        DEFAULT_ZOOM, "My Location"
                    )

                    saveUserLocation(currentLocation)
                }
            }
        }

    }

    private fun saveUserLocation(currentLocation: Location) {
        val userLocation = UserLocation(
            GeoPoint(currentLocation.latitude, currentLocation.longitude),
            UserSharedPreference.instance?.getUser(this),
            null
        )
        FirebaseFirestore.getInstance().collection("User")
            .document(UserSharedPreference.instance?.getUser(this)?.email.toString())
            .set(userLocation).addOnCompleteListener {
                if (it.isSuccessful)

                    Log.d(TAG, "saveUserLocation: Fetching Location...")
                else Log.e(TAG, "saveUserLocation: ", it.exception)
            }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float, title: String) {
        Log.d(
            Companion.TAG,
            "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude
        )
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))

        val markerOptions = MarkerOptions().position(latLng).title(title)
        map.addMarker(markerOptions)
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        if (googleMap != null) {
            map = googleMap
        }

        getCurrentLocation()
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        map.isMyLocationEnabled = true
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
        private const val TAG = "MapActivity"
        private const val DEFAULT_ZOOM = 15f

    }
}