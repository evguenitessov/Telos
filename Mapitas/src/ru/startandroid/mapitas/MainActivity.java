package ru.startandroid.mapitas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.graphics.Color;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends FragmentActivity {
	
	  SupportMapFragment mapFragment;
	  GoogleMap map;
	  final String TAG = "myLogs";
	  /*double laburoLat = -34.60375354430423;
	  double laburoLng = -58.39317753911019;
	  double destinoLat = -34.597211248470025;
	  double destinoLng = -58.390652574598796;*/
	  
	  private static final LatLng LABURO = new LatLng(-34.60375354430423, 58.39317753911019);
	  private static final LatLng DESTINO = new LatLng(-34.597211248470025, -58.390652574598796);	  

	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_path_google_map);

	    mapFragment = (SupportMapFragment) getSupportFragmentManager()
	        .findFragmentById(R.id.map);
	    map = mapFragment.getMap();
	    if (map == null) {
	      finish();
	      return;
	    }
	    
	    //init();
	    
	    MarkerOptions options = new MarkerOptions();
		options.position(LABURO);
		options.position(DESTINO);		
		map.addMarker(options);
		String url = getMapsApiDirectionsUrl();
		ReadTask downloadTask = new ReadTask();
		downloadTask.execute(url);

		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34.59953006089113, -58.39281443506479),
				15));
		addMarkers();
    
	  }

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
		        Log.d(TAG, "onCameraChange: " + camera.target.latitude + "," + camera.target.longitude);
		        Log.d(TAG, "onCameraChange: " + camera.zoom);
		      }
		    });
	  }

	  public void onClickTest(View view) {
		    //map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			  /*CameraPosition cameraPosition = new CameraPosition.Builder()
		        .target(new LatLng(-34.59953006089113, -58.39281443506479))
		        .zoom(15)	        	        
		        .build();		  
		    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
		    map.animateCamera(cameraUpdate);	
		    
		    map.addMarker(new MarkerOptions()
	        	.position(LABURO)
	        	.title("Hello world"));
		    
		    map.addMarker(new MarkerOptions()
	    	.position(DESTINO)
	    	.title("Chauuuu"));*/
		  }

	  private void addMarkers() {
			if (map != null) {
				map.addMarker(new MarkerOptions().position(LABURO)
						.title("Laburo"));
				map.addMarker(new MarkerOptions().position(DESTINO)
						.title("Destino"));				
			}
		}
	  
	  private String getMapsApiDirectionsUrl() {
			String waypoints = "waypoints=optimize:true|"
					+ LABURO.latitude + "," + LABURO.longitude
					+ "|" + "|" + DESTINO.latitude + ","
					+ DESTINO.longitude;

			String sensor = "sensor=false";
			String params = waypoints + "&" + sensor;
			String output = "json";
			String url = "https://maps.googleapis.com/maps/api/directions/"
					+ output + "?" + params;
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
	  
	  private class ParserTask extends
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
