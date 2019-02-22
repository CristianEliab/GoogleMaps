package cristian.proyecto.com.googlemaps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {


    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String TAG = "Mapas";
    private static final String API_KEY = "AIzaSyB3j7p5yXui2Ds8uHdvjK2dOwF_vGmT7t0";
    private GoogleMap mMap;
    private LocationManager manager;
    private MarkerOptions concurrentMarker;
    private boolean first;
    private FloatingActionButton addMarker;

    private static final float DEFAULT_ZOOM = 15f;
    private Marker mMarker;
    private Boolean mLocationPermissionsGranted = false;
    private ArrayList<MarkerOptions> lista_Markres;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setSubtitle("Auto Complete");
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        addMarker = findViewById(R.id.open_dialog);
        addMarker.setOnClickListener(this);

        lista_Markres = new ArrayList<MarkerOptions>();
        getLocationPermission();
    }

    /**
     *
     */
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        metodo2();
    }


    /**
     *
     */
    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                            Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED){
                        initMap();
                    }else{
                        ActivityCompat.requestPermissions(this,
                                permissions,
                                LOCATION_PERMISSION_REQUEST_CODE);
                    }
                }else{
                    ActivityCompat.requestPermissions(this,
                            permissions,
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void metodo2(){
        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), API_KEY);
        }


        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                agregarMarcador();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }


    private void agregarMarcador() {

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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng posicion = new LatLng(location.getLatitude(), location.getLongitude());
                if (first == false) {
                    concurrentMarker = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_person)).position(posicion).title("I");
                    mMap.addMarker(concurrentMarker);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));
                    first = true;
                } else {
                    concurrentMarker.position(posicion);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));
                    String lugar = lugarCercano();
                    DialogFragment dialog = new Dialogo();
                    ((Dialogo) dialog).lugarCercano = lugar;
                    dialog.show(getSupportFragmentManager(), "MyCustomDialog");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        });
    }

    public double distancia = 0.0;
    public static String lugar;
    public int contador = 0;


    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = (double) (earthRadius * c);

        return dist;
    }

    public void masCercano(double lat1, double lng1, double lat2, double lng2){
        double distance = distFrom(lat1, lng1, lat2, lng2);
        if(this.contador == 0){
            this.distancia = distance;
            this.contador++;
        }else{
            if(distance <= this.distancia){
                this.distancia = distance;
            }
        }
    }

    private String lugarCercano(){
        LatLng posicion0 = new LatLng(-53.2255, 4.395);
        MarkerOptions mk_1 = new MarkerOptions().position(posicion0);
        LatLng posicion1 = new LatLng(-53.2255, 4.395);
        MarkerOptions mk_2 = new MarkerOptions().position(posicion1);
        LatLng posicion2 = new LatLng(-53.2255, 4.395);
        MarkerOptions mk_3 = new MarkerOptions().position(posicion2);
        LatLng posicion3 = new LatLng(-53.2255, 4.395);
        MarkerOptions mk_4 = new MarkerOptions().position(posicion3);
        lista_Markres.add(mk_1);
        lista_Markres.add(mk_2);
        lista_Markres.add(mk_3);
        lista_Markres.add(mk_4);
        String lugar = "";
        for (MarkerOptions marker: lista_Markres) {
            lugar = marker.getTitle();
            LatLng posicion = marker.getPosition();
            LatLng concurret = concurrentMarker.getPosition();
            masCercano(posicion.latitude, posicion.longitude,  concurret.latitude, concurret.longitude);
        }
        return lugar + "- ditancia: " + this.distancia;
    }

    /**
     *
     */
    @Override
    public void onMyLocationClick(Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }


    /**
     *
     */
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }

    /**
     *
     */
    @Override
    public void onClick(View v) {
        DialogFragment dialog = new Dialogo();
        dialog.show(getSupportFragmentManager(), "MyCustomDialog");
    }

    /**
     *
     */
    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

   /* *//**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     *//*
    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.clear();
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this));
        if(placeInfo != null){
            try{
                String snippet = "Address: " + placeInfo.getAddress() + "\n" +
                        "Phone Number: " + placeInfo.getPhoneNumber() + "\n" +
                        "Website: " + placeInfo.getWebsiteUri() + "\n" +
                        "Price Rating: " + placeInfo.getRating() + "\n";
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);
                mMarker = mMap.addMarker(options);
            }catch (NullPointerException e){
            }
        }else{
            mMap.addMarker(new MarkerOptions().position(latLng));
        }

        hideSoftKeyboard();
    }
*/
    /**
     *
     */
    private void moveCamera(LatLng latLng, float zoom, String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }
        hideSoftKeyboard();
    }

    /**
     *
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
