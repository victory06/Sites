package com.example.sites

import android.content.Context
import android.content.Intent
import android.gesture.Gesture
import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.gesture.GestureOverlayView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import java.lang.Math.*

class MainActivity : AppCompatActivity(), GestureOverlayView.OnGesturePerformedListener,
    SensorEventListener {

    //Previene pulsar cualquier boton mientras se hace el gesto
    private var gLibrary: GestureLibrary? = null

    // Variables de cámara
    private var cam = Camera(this)


    // record the compass picture angle turned
    private var currentDegree = 0f
    // device sensor manager
    private var mSensorManager: SensorManager? = null
    // image compass
    private var imview: ImageView?=null

    val MIN_TIME: Long=1000000
    var locationManager:LocationManager?=null
    var longit:Double=0.0
    var latit:Double= 0.0
    var latlonActualizadas:Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cam.onCreate()

        initData()


        gestureSetup()

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1000)
        }else{
            iniciarLocalizacion()
        }

    }

    fun iniciarLocalizacion(){
        locationManager= getSystemService(Context.LOCATION_SERVICE) as LocationManager


        val gpsEnabled: Boolean= locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if(!gpsEnabled){
            var intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1000)

            return;
        }
        locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, 0F, locationListener)
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, 0F, locationListener)
        locationManager?.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_TIME, 0F, locationListener)



    }

    //define the location listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            /*val sb = StringBuilder()
            val ubi = ("Longitud:" + location.longitude + " Latitud: " + location.latitude)
            sb.append(ubicaciones.text).append(System.lineSeparator()).append(ubi)
            ubicaciones.text=sb.toString()*/
            longit=location.longitude
            latit=location.latitude
            ubicaciones.text=(System.lineSeparator()+"Longitud:" + location.longitude + System.lineSeparator()+  " Latitud: " + location.latitude)
            calcularZona()
            latlonActualizadas=true
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun gestureSetup(){
        gLibrary= GestureLibraries.fromRawResource(this,R.raw.gesture)

        if(gLibrary?.load()==false){
            finish()
        }
        gOverlay.addOnGesturePerformedListener(this)
    }

    override fun onGesturePerformed(overlay: GestureOverlayView?, gesture: Gesture?) {

        val predictions = gLibrary?.recognize(gesture)

        predictions?.let {
            if(it.size > 0 && it[0].score > 1.0){
                val action = "cambiando a mapa"
                Toast.makeText(this, action, Toast.LENGTH_SHORT).show()

                val intent: Intent = Intent(this, ActivityGPS::class.java)
                startActivity(intent)
                /** Fading Transition Effect */
                this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == cam.getRequestCameraPermission()) {
            if (grantResults.size>0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(this@MainActivity, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show()
                finish()
            }
        }
        if(requestCode==1000){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                iniciarLocalizacion()
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(cam.getTAG(), "onResume")
        cam.startBackgroundThread()
        if (cam.textureView!!.isAvailable) {
            cam.openCamera()
        } else {
            cam.onResume()
        }

        mSensorManager?.registerListener(this,mSensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        Log.e(cam.getTAG(), "onPause")
        //closeCamera();
        cam.stopBackgroundThread()
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val degree=Math.round(event?.values?.get(0)!!)

        val rotateAnimation = RotateAnimation(currentDegree,(-degree).toFloat(), Animation.RELATIVE_TO_SELF,0.5f,
            Animation.RELATIVE_TO_SELF,0.5f)
        rotateAnimation.duration=210
        rotateAnimation.fillAfter=true

        imview?.startAnimation(rotateAnimation)
        currentDegree= (-degree).toFloat()
        if(latlonActualizadas) {
            mirandoHacia(currentDegree)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun initData(){
        mSensorManager=getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        imview=findViewById(R.id.imgCompass)
    }




    fun getDistanceFromLatLonInKm(lat1:Double,lon1:Double,lat2:Double,lon2:Double): Double {
        var R = 6371; // Radius of the earth in km
        var dLat = deg2rad(lat2-lat1);  // deg2rad below
        var dLon = deg2rad(lon2-lon1);
        var a =
            Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                    Math.sin(dLon/2) * Math.sin(dLon/2)
        ;
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        var d = R * c; // Distance in km
        return d;
    }

    fun deg2rad(deg:Double): Double {
        return deg * (Math.PI/180)
    }

    fun <T> append(arr: Array<T>, element: T): Array<T?> {
        val array = arr.copyOf(arr.size + 1)
        array[arr.size] = element
        return array
    }

    fun anguloLatLon(lat1:Double,lon1:Double,lat2:Double,lon2:Double): Double{
        return Math.atan2(sin(lat1-lon2)*cos(lat1), cos(lat2)*sin(lat1)-sin(lat2)*cos(lat1)*cos(lon1-lon2))

    }

    fun mirandoHacia(deg: Float){
        mirandoa.text = ""
        var m=Miradores
        var z=Zona
        var enMirador:Boolean=false
        //Indice en el array de Miradores del mirador en el que estamos (si estamos en alguno)
        var indiceMirador:Int = 0

        //Primero calculamos si nos encontramos en un mirador para no mostrar la posición de otros miradores en el TextView
        //Si estamos a menos de 20 metros de un mirador, consideramos que estamos en él
        for (x in m.getArray()){
            var dist=getDistanceFromLatLonInKm(latit, longit, x.lat,x.lon)
            if(dist<0.020){
                enMirador=true
                mirandoa.text="Te encuentras en el mirador "+m.arrayNombres[indiceMirador]+System.lineSeparator()
            }else {
                indiceMirador = indiceMirador + 1
            }
        }

        //Si no estamos en un mirador, añadimos si estamos mirando hacia algún mirador
        if(!enMirador){
            mirandoa.text="No te encuentras en ningun mirador"+System.lineSeparator()
            var indiceMir:Int=0
            for(mir in m.arraySitios) {
                var degg = -deg
                var angulo: Double = anguloLatLon(mir.lat, mir.lon, latit, longit)
                var distMir=getDistanceFromLatLonInKm(latit, longit, mir.lat, mir.lon)
                if(abs(toRadians(degg.toDouble())-angulo)<PI/(distMir*10)){
                    mirandoa.text=mirandoa.text.toString()+m.arrayNombres[indiceMir]+System.lineSeparator()
                }
                indiceMir=indiceMir+1
            }
        }

        //En caso de que estemos en un mirador, sólo se escribirían las zonas a las que miramos
        //Variable para llevar el índice del mirador en el array de zonas
        var numMirador:Int=0
        var distcer: Double
        var distlej: Double
        for(zone in z.arrayZonas) {
            //Este array contiene los dos puntos entre los que estarás mirando a una zona
            var puntosVista = ArrayList<Zona.Point>()
            //Para ello, guardamos en ese array los dos puntos que no sean el más cercano ni el más lejano
            var lejano: Zona.Point = Zona.Point(1.0, 1.0)
            var cercano: Zona.Point = Zona.Point(1.0, 1.0)
            distlej = 0.0
            distcer = Zona.INF
            for (p in zone) {

                var distactual = getDistanceFromLatLonInKm(latit, longit, p.x, p.y)
                if (distactual > distlej) {
                    lejano = p
                    distlej = distactual
                }
                if (distactual < distcer) {
                    cercano = p
                    distcer = distactual
                }
            }
            //Añadimos los dos puntos "medios"
            var ind:Int=0
            for (p in zone) {
                if (p != cercano && p != lejano) {
                    puntosVista.add(p)
                    ind=ind+1
                }
            }

            //Ahora vamos a calcular si nuestro punto de mira se encuenta entre esos dos puntos
            var degg = -deg
            var ang1: Double = anguloLatLon(puntosVista[0].x, puntosVista[0].y, latit, longit)
            var ang2: Double = anguloLatLon(puntosVista[1].x, puntosVista[1].y, latit, longit)
            if (ang1 >= ang2) {
                if (toRadians(degg.toDouble()) < ang1 && toRadians(degg.toDouble()) > ang2) {
                    mirandoa.text =
                        mirandoa.text.toString()  + z.arrayNombres[numMirador]+ System.lineSeparator()
                }
            }
            numMirador = numMirador + 1
        }


    }

    fun calcularZona(){
        var z = Zona


        val a = Zona.Point(latit, longit)

        if(z.isInside(z.centro, 4, a))
            zona.text="Centro"
        else if (z.isInside(z.albaicin, 4, a))
            zona.text="Albaicin"
        else if (z.isInside(z.alhambra, 4, a))
            zona.text="Alhambra"
        else if (z.isInside(z.carreteraSierra, 4, a))
            zona.text="Carretera de la Sierra"
        else if (z.isInside(z.cartuja, 4, a))
            zona.text="Cartuja"
        else if (z.isInside(z.cerrillo, 4, a))
            zona.text="Cerrillo de Maracena"
        else if (z.isInside(z.chana, 4, a))
            zona.text="La Chana"
        else if (z.isInside(z.generalife, 4, a))
            zona.text="Dehesa del Generalife"
        else if (z.isInside(z.norte, 4, a))
            zona.text="Zona Norte"
        else if (z.isInside(z.plazaToros, 4, a))
            zona.text="Plaza de Toros"
        else if (z.isInside(z.realejo, 4, a))
            zona.text="Realejo"
        else if (z.isInside(z.sacromonte, 4, a))
            zona.text="Sacromonte"
        else if (z.isInside(z.vega, 4, a))
            zona.text="Vega de Granada"
        else if (z.isInside(z.zaidin, 4, a))
            zona.text="Zaidín"
        else
            zona.text="Fuera de Granada"
    }


}