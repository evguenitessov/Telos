package ohyeah.ohyeah;

/**
 * Created by etessov on 03/03/2015.
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class PathGoogleMapActivity extends FragmentActivity {

    SupportMapFragment mapFragment;
    GoogleMap map;
    Marker marker;
    final String TAG = "myLogs";
    private LocationManager locationManager;

    private static final LatLng MI_CASITA = new LatLng(-34.63117694937363,-58.478600196540356);
    private static final LatLng DESTINO = new LatLng(-34.63883238482591, -58.52886848151683);
    private static final LatLng PARQUE_NORTE = new LatLng(-34.545931355014005, -58.438594713807106);
    private static final LatLng PUERTO_MADERO = new LatLng(-34.59999398119388, -58.36435686796904);
    private static final LatLng P1 = new LatLng(-34.63074548078885, -58.469833731651306);
    private static final LatLng P2 = new LatLng(-34.63251934303525, -58.476584851741784);

    @Override/**/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_google_map);

        //Obtengo el fragmento del Activity
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //Obtengo el objeto "map" del fragmento anterior
        map = mapFragment.getMap();
        if (map == null) {
            finish();
            return;
        }

        //Agrego las acciones que pasan cuando hago click en el mapa
        init();

        //Agrego los puntos de "origen" y "destino" al mapa
        MarkerOptions options = new MarkerOptions();
        options.position(MI_CASITA);
        options.position(PARQUE_NORTE);
        map.addMarker(options);

        //Dibujo el trayecto desde el origen hasta destino en el mapa
        String url = getMapsApiDirectionsUrl();
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);

        //Agrego los makers de origen y destino al mapa
        addMarkers();

        //Busco y agrego maker en base a una direccion
        List<Address> fromLocationName = new ArrayList<Address>();
        Geocoder geocoder = new Geocoder(this);
        try {
            fromLocationName = geocoder.getFromLocationName("Rivadavia 8000", 20);
            for (Address address : fromLocationName) {
                if((address.getSubLocality() != null) && (address.getSubLocality().toLowerCase().equals("floresta"))) {
                    map.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude()))
                            .title("Direccion de prueba"));
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Agrego el manager para el provider de localizacion
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //Calculo la distancia
        float[] currentDistance = new float[1];
        Location.distanceBetween(MI_CASITA.latitude, MI_CASITA.longitude, PARQUE_NORTE.latitude, PARQUE_NORTE.longitude, currentDistance);

        //Enfoco la camara a mi casa
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(MI_CASITA,13));
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 0, 10,
                locationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };


    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            if (marker != null) {
                marker.remove();
            }

            marker = map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("Direccion casa"));
        } else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER)) {
            map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("Direccion casa"));
        }
    }

    public void onClickLocationSettings(View view) {
        startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    };

    private void init() {
        map.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "onMapClick: " + latLng.latitude + "," + latLng.longitude);
            }
        });

        map.setOnMapLongClickListener(new OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.d(TAG, "onMapLongClick: " + latLng.latitude + "," + latLng.longitude);
            }
        });

        map.setOnCameraChangeListener(new OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition camera) {
                Log.d(TAG, "onCameraChange: " + camera.target.latitude + "," + camera.target.longitude + "," + camera.zoom);
            }
        });
    }

    private void addMarkers() {
        if (map != null) {
            map.addMarker(new MarkerOptions().position(MI_CASITA)
                    .title("First Point"));
            map.addMarker(new MarkerOptions().position(PARQUE_NORTE)
                    .title("Second Point"));
        }
    }

    public void onClickTest(View view) {
        //Seteo el tipo de mapa
        //map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        //Agrego un marker al mapa para probar
	  /*map.addMarker(new MarkerOptions()
      .position(new LatLng(0, 0))
      .title("Hello world"));*/

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(
                new LatLngBounds(MI_CASITA, PARQUE_NORTE), 600, 400, 50);
        map.animateCamera(cameraUpdate);

    }


    private String getMapsApiDirectionsUrl() {
        String waypoints = "waypoints=optimize:true|"
                + MI_CASITA.latitude + "," + MI_CASITA.longitude
                + "|" + "|" + PARQUE_NORTE.latitude + ","
                + PARQUE_NORTE.longitude;

        String sensor = "sensor=false";
        String originDestiny = "origin=" + MI_CASITA.latitude + "," + MI_CASITA.longitude + "&" + "destination=" + PARQUE_NORTE.latitude + "," + PARQUE_NORTE.longitude;
        String params = originDestiny + "&" + waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        //https://maps.googleapis.com/maps/api/directions/json?origin=52.28582710000001,-1.1141665&destination=52.2777244,-1.1581097&waypoints=optimize:true|-34.63084231312819,-58.47995035350323||-34.63883238482591,-58.52886848151683&sensor=false
        return url;
    }

    public class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    public class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(2);
                polyLineOptions.color(Color.BLUE);
            }

            map.addPolyline(polyLineOptions);
        }
    }
}




