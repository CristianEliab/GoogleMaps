package cristian.proyecto.com.googlemaps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapClickListener, NoticeDialogFragment.NoticeDialogListener,
        GoogleMap.OnMarkerClickListener {


    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String TAG = "Mapas";
    private static final String API_KEY = "AIzaSyB3j7p5yXui2Ds8uHdvjK2dOwF_vGmT7t0";
    private static final int DISTANCIA_MINIMA = 500;
    private GoogleMap mMap;
    private LocationManager manager;
    private MarkerOptions concurrentMarker;
    private boolean first;
    private ImageView imagen_info;
    private Boolean mLocationPermissionsGranted = false;
    private ArrayList<MarkerOptions> lista_Markres;
    private ImageView agregar_Marcador;
    private RelativeLayout buscador;
    private LatLng posicionNewMarker;
    private static final float DEFAULT_ZOOM = 15;
    public EditText nombreLugar;
    public double distancia = 0.0;
    public static String lugar;
    public int contador = 0;
    private boolean visible;
    private TextView text_description;


    /**
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar tb = findViewById(R.id.toolbar);
        lista_Markres = new ArrayList<MarkerOptions>();
        text_description = findViewById(R.id.text_place);

        setSupportActionBar(tb);
        tb.setSubtitle("Auto Complete");
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        visible = true;
        getLocationPermission();
        activarInfo();
        agregar_Marcador_Opcion2();
    }

    /**
     *
     */
    private void agregar_Marcador_Opcion2() {
        agregar_Marcador = findViewById(R.id.add_marker);
        buscador = findViewById(R.id.relLayout1);
        agregar_Marcador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visible) {
                    buscador.setVisibility(View.VISIBLE);
                    visible = false;
                } else {
                    buscador.setVisibility(View.GONE);
                    visible = true;
                }
            }
        });
    }

    /**
     *
     */
    private void activarInfo() {
        imagen_info = findViewById(R.id.place_info);
        imagen_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new Dialogo();
                String lugar = lugarCercano();
                ((Dialogo) dialog).lugarCercano = lugar;
                dialog.show(getSupportFragmentManager(), "MyCustomDialog");
            }
        });
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
    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                            Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
                        initMap();
                    } else {
                        ActivityCompat.requestPermissions(this,
                                permissions,
                                LOCATION_PERMISSION_REQUEST_CODE);
                    }
                } else {
                    ActivityCompat.requestPermissions(this,
                            permissions,
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     *
     */
    private void metodo2() {
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
                agregarMarcador(place);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    /**
     *
     */
    private void agregarMarcador(Place place) {
        MarkerOptions newMarKer = new MarkerOptions();
        LatLng posicion = place.getLatLng();
        newMarKer.position(posicion)
                .infoWindowAnchor(100, 100)
                .title(place.getName());
        mMap.addMarker(newMarKer);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));
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

        mMap.setOnMapClickListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMarkerClickListener(this);

        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng posicion = new LatLng(location.getLatitude(), location.getLongitude());
                if (first == false) {
                    concurrentMarker = new MarkerOptions()
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.mipmap.ic_person))
                            .position(posicion)
                            .title("I");
                    mMap.addMarker(concurrentMarker);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));
                    first = true;
                } else {
                    concurrentMarker.position(posicion);
                    obtenerCercanos(DISTANCIA_MINIMA);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));
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

    /**
     *
     */
    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = (double) (earthRadius * c);

        return dist;
    }

    /**
     *
     */
    public void masCercano(double lat1, double lng1, double lat2, double lng2) {
        double distance = distFrom(lat1, lng1, lat2, lng2);
        if (this.contador == 0) {
            this.distancia = distance;
            this.contador++;
        } else {
            if (distance <= this.distancia) {
                this.distancia = distance;
            }
        }
    }

    /**
     *
     */
    private String lugarCercano() {
        DecimalFormat format = new DecimalFormat();
        for (MarkerOptions marker : lista_Markres) {
            lugar = marker.getTitle();
            LatLng posicion = marker.getPosition();
            LatLng concurret = concurrentMarker.getPosition();
            format.setMaximumFractionDigits(2);
            masCercano(posicion.latitude, posicion.longitude, concurret.latitude, concurret.longitude);
        }
        if (lugar != null) {
            String conversion = format.format(this.distancia);
            return "The nearest place is \n" + lugar + " at a distance of: \n" + conversion + "meters";
        } else {
            String conversion = format.format(this.distancia);
            return "Has no marker on the map";
        }
    }

    /**
     *
     */
    @Override
    public void onMyLocationClick(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String direccion = "";
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address address = addresses.get(0);
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            direccion += address.getAddressLine(i) + "\n";
        }
    }


    /**
     *
     */
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation clicked", Toast.LENGTH_SHORT).show();
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
    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    /**
     *
     */
    private void moveCamera(LatLng latLng, float zoom, String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (!title.equals("I")) {
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

    @Override
    public void onMapClick(LatLng latLng) {
        showNoticeDialog();
        posicionNewMarker = latLng;
    }

    public void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new NoticeDialogFragment();
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(String namePlace) {
        if (namePlace != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionNewMarker, DEFAULT_ZOOM));
            MarkerOptions options = new MarkerOptions()
                    .position(posicionNewMarker).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_add_place))
                    .title(namePlace);
            mMap.addMarker(options);
            lista_Markres.add(options);
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    private String obtenerDireccion() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String direccion = "";
        List<Address> addresses = null;
        try {
            // My position
            LatLng posicion = concurrentMarker.getPosition();
            addresses = geocoder.getFromLocation(posicion.latitude, posicion.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address address = addresses.get(0);
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            direccion += address.getAddressLine(i) + "\n";
        }
        return direccion;
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!marker.getTitle().equals("I")) {
            text_description.setText("");
            text_description.setText("You are in \n" + marker.getTitle() + " with" + obtenerDireccion());
        } else {
            text_description.setText("");
            text_description.setText(marker.getTitle() + " am in " + obtenerDireccion());
        }
        return false;
    }

    private void obtenerCercanos(int distanciaMinima) {
        DecimalFormat format = new DecimalFormat();
        for (MarkerOptions marker : lista_Markres) {
            LatLng posicion = marker.getPosition();
            LatLng concurret = concurrentMarker.getPosition();
            format.setMaximumFractionDigits(2);
            masCercano(posicion.latitude, posicion.longitude, concurret.latitude, concurret.longitude);
            if (distancia <= distanciaMinima) {
                String nombre = marker.getTitle();
                if (!nombre.equals("I")) {
                    text_description.setText("");
                    text_description.setText("You are in \n" + nombre);
                } else {
                    text_description.setText("");
                    text_description.setText(nombre +"\n" + obtenerDireccion());
                }
            } else {
                text_description.setText("");
                text_description.setText("This place is more than \n 500 meters away.");
            }
        }
    }


}
