package com.example.wifiapfinder;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.wifiapfinder.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class WifiApFinderPhase1 extends Activity {
	WifiManager wifiMgrPhase1;
	WifiScanReceiver wifiRecieverPhase1;
	ListView list;
	String wifis[];
	List<Location> geoLocations;
	GpsLocationReceiver gpsLoc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_ap_finder_phase1);

		// GPS location receiver
		gpsLoc = new GpsLocationReceiver();

		// geo location list
		geoLocations = new ArrayList<Location>();

		// wifi scan results
		list = (ListView) findViewById(R.id.listView1);
		wifiMgrPhase1 = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiRecieverPhase1 = new WifiScanReceiver();
	}

	public void recordReading(View view) {
		double latitude, longitude;
		Location loc;
		loc = gpsLoc.getLocation();
		latitude = loc.getLatitude();
		longitude = loc.getLongitude();

		geoLocations.add(loc);
		Toast.makeText(getApplicationContext(),
				"Lat:Long " + latitude + ":" + longitude, Toast.LENGTH_LONG)
				.show();

		alertBox("Wifi Scanning In Progres..");

		// once you get the location read the wifi signals..
		wifiMgrPhase1.startScan();
		registerReceiver(wifiRecieverPhase1, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	public void computeP1(View view) {
		// alertBox("Compute Module TBD");
		double gLatitude, gLongitude;
		String label = "AP Pos";
		String zoomSetting = "&z=23";
		String locations[] = new String[geoLocations.size()];
		for (int i = 0; i < geoLocations.size(); i++) {
			// locations[i] = ((geoLocations.get(i)).toString());
			gLatitude = (geoLocations.get(i)).getLatitude();
			gLongitude = (geoLocations.get(i)).getLongitude();
			String geoUriBegin = "geo:" + gLatitude + ","
					+ gLongitude;
			String geoUriQuery = ""+gLatitude+","+gLongitude+"(" + label + ")";
			String encodedQuery = Uri.encode( geoUriQuery  );
			String uriString = geoUriBegin + "?q=" + encodedQuery + zoomSetting ;
			locations[i] = uriString;
		}

		list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, locations));
		list.setOnItemClickListener(mMessageClickedHandler);
	}

	// Create a message handling object as an anonymous class.
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
		public void onItemClick(AdapterView parent, View v, int position,
				long id) {
			// Display a messagebox.
			Toast.makeText(getApplicationContext(), "You've got an event",
					Toast.LENGTH_SHORT).show();
			showMap((String) list.getItemAtPosition(position));
		}
	};

	public void showMap(String geoUriStr) {
		// String geoUriStr =
		// "geo:"+geoLocation.getLatitude()+","+geoLocation.getLongitude();
		Uri geoLoc = Uri.parse(geoUriStr);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(geoLoc);
		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivity(intent);
		}
	}

	/* Request updates at startup */
	@Override
	protected void onResume() {
		super.onResume();
	}

	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
		try {
			unregisterReceiver(wifiRecieverPhase1);
		} catch (IllegalArgumentException e) {
			; // DO nothing..
		}
	}

	public void alertBox(String text) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Info");
		alert.setMessage(text);
		alert.setPositiveButton("OK", null);
		alert.show();
	}

	class WifiScanReceiver extends BroadcastReceiver {
		@SuppressLint("UseValueOf")
		public void onReceive(Context c, Intent intent) {
			List<ScanResult> wifiScanList = wifiMgrPhase1.getScanResults();
			wifis = new String[wifiScanList.size()];
			for (int i = 0; i < wifiScanList.size(); i++) {
				wifis[i] = ((wifiScanList.get(i)).toString());
			}

			list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
					android.R.layout.simple_list_item_1, wifis));
		}
	}

	class GpsLocationReceiver implements LocationListener {
		// flag for GPS status
		boolean isGPSEnabled = false;

		// flag for network status
		boolean isNetworkEnabled = false;

		boolean canGetLocation = false;

		Location location; // location
		double latitude; // latitude
		double longitude; // longitude

		// The minimum distance to change Updates in meters
		private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10
																		// meters

		// The minimum time between updates in milliseconds
		private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1
																		// minute

		// Declaring a Location Manager
		protected LocationManager locationManager;

		public Location getLocation() {
			try {
				locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

				// getting GPS status
				isGPSEnabled = locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER);

				// getting network status
				isNetworkEnabled = locationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

				if (!isGPSEnabled && !isNetworkEnabled) {
					// no network provider is enabled
					// TODO: Open connectivity settings.
				} else {
					this.canGetLocation = true;
					// First get location from Network Provider
					if (isNetworkEnabled) {
						locationManager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER,
								MIN_TIME_BW_UPDATES,
								(float) MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("Network", "Network");
						if (locationManager != null) {
							location = locationManager
									.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
					// if GPS Enabled get lat/long using GPS Services
					if (isGPSEnabled) {
						if (location == null) {
							locationManager.requestLocationUpdates(
									LocationManager.GPS_PROVIDER,
									MIN_TIME_BW_UPDATES,
									MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
							Log.d("GPS Enabled", "GPS Enabled");
							if (locationManager != null) {
								location = locationManager
										.getLastKnownLocation(LocationManager.GPS_PROVIDER);
								if (location != null) {
									latitude = location.getLatitude();
									longitude = location.getLongitude();
								}
							}
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return location;
		}

		@Override
		public void onLocationChanged(Location location) {

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(), "Status changed for GPS",
					Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(getApplicationContext(),
					"Enabled new provider " + provider, Toast.LENGTH_SHORT)
					.show();

		}

		@Override
		public void onProviderDisabled(String provider) {
			Toast.makeText(getApplicationContext(),
					"Disabled provider " + provider, Toast.LENGTH_SHORT).show();
		}

	}

}
