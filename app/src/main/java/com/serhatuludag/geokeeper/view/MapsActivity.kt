package com.serhatuludag.geokeeper.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.serhatuludag.geokeeper.R
import com.serhatuludag.geokeeper.databinding.ActivityMapsBinding
import com.serhatuludag.geokeeper.model.Place
import com.serhatuludag.geokeeper.roomdb.PlaceDao
import com.serhatuludag.geokeeper.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean : Boolean? = null
    private var selectedLatitude : Double? = null
    private var selectedLongitude : Double? = null

    private lateinit var db: PlaceDatabase
    private lateinit var placeDao : PlaceDao
    val compositeDisposable = CompositeDisposable()

    var placeFromMain : Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()

        selectedLatitude = 0.0
        selectedLongitude = 0.0

        binding.saveButton.isEnabled = false

        sharedPreferences = this.getSharedPreferences("com.serhatuludag.geokeeper", MODE_PRIVATE)
        trackBoolean = false

        db = Room.databaseBuilder(
            applicationContext,
            PlaceDatabase::class.java, "Places"
        ) //.allowMainThreadQueries()
            .build()

        placeDao = db.placeDao()


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)

        val intent = intent
        val info = intent.getStringExtra("info")

        if (info == "new"){

            binding.saveButton.visibility = View.VISIBLE
            binding.deleteButton.visibility = View.GONE

            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    trackBoolean = sharedPreferences.getBoolean("trackBoolean",false)
                    if (trackBoolean == false){
                        val userLocation = LatLng(location.latitude,location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,13f))
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply()
                    }
                }
            }

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //permission not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)){
                    Snackbar.make(binding.root,"Permission needed for location",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                        //request permission
                        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()
                }else{
                    //request permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }else{
                //permission granted
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null){
                    val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,13f))
                }
                mMap.isMyLocationEnabled = true
            }
        }else {
            mMap.clear()
            placeFromMain = intent.getSerializableExtra("place") as? Place
            placeFromMain?.let {

                val latLng = LatLng(it.latitude, it.longitude)

                mMap.addMarker(MarkerOptions().position(latLng).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))

            binding.placeText.setText(it.name)

        }


        }
        /*lat=38.319862984919475,long= 26.640186947605486  IZTECH Library

        val iztech = LatLng(38.319862984919475, 26.640186947605486)
        mMap.addMarker(MarkerOptions().position(iztech).title("IZTECH Library"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(iztech, 13f))

         */

    }

    private fun registerLauncher(){
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
             if (result){
                 if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                     //permission granted
                     locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,0f,locationListener)
                     val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                     if (lastLocation != null){
                         val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                         mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                     }
                     mMap.isMyLocationEnabled = true
                 }
             }else{
                //permission not granted
                Toast.makeText(this@MapsActivity,"Permission needed!",Toast.LENGTH_LONG).show()
             }
        }
    }

    override fun onMapLongClick(p0: LatLng) {

        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0))

        selectedLatitude = p0.latitude
        selectedLongitude = p0.longitude

        binding.saveButton.isEnabled = true

    }

    fun save(view: View){
        val place = Place(binding.placeText.text.toString(),selectedLatitude!!,selectedLongitude!!)
        compositeDisposable.add(
            placeDao.insert(place)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).
                subscribe(this::handleResponse)
        )
    }

    fun delete(view: View){

        placeFromMain?.let {
            compositeDisposable.add(
                placeDao.delete(it).
            subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponse))
        }

    }

    private fun handleResponse(){
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)


    }

    override fun onDestroy(){
        super.onDestroy()
        compositeDisposable.clear()
    }

}