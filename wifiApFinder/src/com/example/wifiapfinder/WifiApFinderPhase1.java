package com.example.wifiapfinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
	ListView list, suggestions;
	String wifis[];
	List<Location> geoLocations;
	ArrayList<HashMap<String, wifiApLocObject>> apList;
	GpsLocationReceiver gpsLoc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_ap_finder_phase1);

		// GPS location receiver
		gpsLoc = new GpsLocationReceiver();

		// ap hashmap creation
		apList = new ArrayList<HashMap<String, wifiApLocObject>>();

		// geo location list
		geoLocations = new ArrayList<Location>();

		// wifi scan results
		list = (ListView) findViewById(R.id.listView1);
		suggestions = (ListView) findViewById(R.id.listView2);
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
				"Reading beacons @ Lat:Long " + latitude + ":" + longitude,
				Toast.LENGTH_SHORT).show();

		// alertBox("Wifi Scanning In Progres..");

		// once you get the location read the wifi signals..
		wifiMgrPhase1.startScan();
		registerReceiver(wifiRecieverPhase1, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	public String getQueryStrFromLocation(Location loc) {
		double gLatitude, gLongitude;
		String label = "AP Pos";
		String zoomSetting = "&z=23";
		gLatitude = loc.getLatitude();
		gLongitude = loc.getLongitude();
		String geoUriBegin = "geo:" + gLatitude + "," + gLongitude;
		String geoUriQuery = "" + gLatitude + "," + gLongitude + "(" + label
				+ ")";
		String encodedQuery = Uri.encode(geoUriQuery);
		String uriString = geoUriBegin + "?q=" + encodedQuery + zoomSetting;
		return uriString;
	}

	public void computeP1(View view) {
		// alertBox("Compute Module TBD");

		String locations[] = new String[geoLocations.size()];
		for (int i = 0; i < geoLocations.size(); i++) {
			// locations[i] = ((geoLocations.get(i)).toString());
			locations[i] = getQueryStrFromLocation(geoLocations.get(i));
		}

		list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, locations));
		list.setOnItemClickListener(mMessageClickedHandler);

		String displayList[] = new String[apList.size()];
		for(int i =0;i<apList.size();i++){
			displayList[i] = apList.get(i).toString();
		}
		suggestions.setAdapter(new ArrayAdapter<String>(
				getApplicationContext(), android.R.layout.simple_list_item_1,
				displayList));
	}

	// Create a message handling object as an anonymous class.
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
		public void onItemClick(
				@SuppressWarnings("rawtypes") AdapterView parent, View v,
				int position, long id) {
			// open the maps for the position
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

		// number of levels the wifi signal is calculated for.
		private static final int WIFI_RSSI_STRENGTH_LEVELS = 5;

		@SuppressWarnings("boxing")
		private final ArrayList<Integer> channel_2_4Ghz = new ArrayList<Integer>(
				Arrays.asList(0, 2412, 2417, 2422, 2427, 2432, 2437, 2442,
						2447, 2452, 2457, 2462, 2467, 2472, 2484));

		public int getChannelFromFrequency(int frequency) {
			return channel_2_4Ghz.indexOf(Integer.valueOf(frequency));
		}

		public int getSignalStrength(int rssi) {
			return WifiManager.calculateSignalLevel(rssi,
					WIFI_RSSI_STRENGTH_LEVELS);
		}

		@SuppressLint("UseValueOf")
		public void onReceive(Context c, Intent intent) {
			List<ScanResult> wifiScanList = wifiMgrPhase1.getScanResults();
			wifis = new String[wifiScanList.size()];
			double dBmToWatt[] = new double[wifiScanList.size()];
			double totaldBm = 0;
			wifiApLocObject apObj;

			for (int i = 0; i < wifiScanList.size(); i++) {
				double dBm = (double) wifiScanList.get(i).level;
				dBmToWatt[i] = (Math.pow(10, (dBm / 10))) / 1000;
				totaldBm += dBmToWatt[i];
			}

			totaldBm = 10 * Math.log10(1000 * totaldBm);

			for (int i = 0; i < wifiScanList.size(); i++) {
				int channel = getChannelFromFrequency(wifiScanList.get(i).frequency);
				if (channel == -1)
					wifis[i] = ((wifiScanList.get(i)).toString());
				else
					wifis[i] = ((wifiScanList.get(i)).toString())
							+ ", (2.4Ghz@Ch:" + channel + ")";
				wifis[i] += ", Strength Level:"
						+ getSignalStrength(wifiScanList.get(i).level);
				wifis[i] += ", dBmLog:" + dBmToWatt[i] + ", totaldBm:"
						+ totaldBm;
			}

			Location lastLoc = geoLocations.get(geoLocations.size() - 1);
			apObj = new wifiApLocObject(
					getQueryStrFromLocation(lastLoc), totaldBm);
			
			String key = lastLoc.getLatitude()+","+lastLoc.getLongitude();
			HashMap<String, wifiApLocObject> map = new HashMap<String, wifiApLocObject>();
			map.put(key, apObj);
			apList.add(map);
			
			list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
					android.R.layout.simple_list_item_1, wifis));

			// Toast.makeText(getApplicationContext(), "Wifi Scan Completed",
			// Toast.LENGTH_SHORT).show();
			// unregister the service after first reading..
			try {
				unregisterReceiver(wifiRecieverPhase1);
			} catch (IllegalArgumentException e) {
				; // DO nothing..
			}
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

	class wifiApLocObject {
		String geoUriStr;
		double dBmValue;

		public wifiApLocObject(String geoUriStr, double dBmValue) {
			this.geoUriStr = new String(geoUriStr);
			this.dBmValue = dBmValue;
		}

		public String toString() {
			return ("GeoString:" + geoUriStr + "," + "dBm:" + dBmValue);
		}
	}
}
