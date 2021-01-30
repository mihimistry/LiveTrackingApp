package com.example.livetrackingapp.model

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

class UserLocation(
    val geoPoint: GeoPoint? = null,
    val user: UserModel? = null,
    @ServerTimestamp
    val timestamp: Date? = null
)