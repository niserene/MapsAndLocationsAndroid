package com.example.mapsandlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    val locationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        when{
            isFineLocationGranted() ->{
                when{
                    isLocationEnabled() -> setupLocationListener()
                    else -> showGpsNotEnableDialog()
                }
            }
            else ->requestAccessFineLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            999->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    when{
                        isLocationEnabled() -> setupLocationListener()
                        else -> showGpsNotEnableDialog()
                    }
                }
                else{
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupLocationListener(){

        val providers:MutableList<String> = locationManager.getProviders(true)

        var loc: Location?=null
        for(i:Int in providers.indices.reversed()){
            loc = locationManager.getLastKnownLocation(providers[i])
            if(loc!=null)
                break
        }
        loc?.let{
            if(::mMap.isInitialized){
                val myLoc = LatLng(it.latitude, it.longitude)
                mMap.addMarker(MarkerOptions().position(myLoc).title("my location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc))
            }
        }
    }

    fun isFineLocationGranted():Boolean{
        return checkSelfPermission(
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    }

    private fun requestAccessFineLocation(){
        this.requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            999
        )
    }

    fun isLocationEnabled():Boolean{
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun showGpsNotEnableDialog(){
        AlertDialog.Builder(this)
            .setTitle("Enable GPS")
            .setMessage("Gps is required for google maps")
            .setCancelable(false)
            .setPositiveButton("Enable Now"){ dialogInterface:DialogInterface, i:Int->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                dialogInterface.dismiss()
            }.show()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
            isMyLocationButtonEnabled = true
            isCompassEnabled = true
        }
        // Add a marker in Bhopal and move the camera

        val bhopal = LatLng(23.2599, 77.4126)
        mMap.addMarker(MarkerOptions().position(bhopal).title("Marker in Bhopal"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bhopal))
    }
}