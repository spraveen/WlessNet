package com.example.wifiapfinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	HashMap<String, wifiApLocObject> apLocMap;
	ArrayList<wifiApLocObject> sortApList;
	GpsLocationReceiver gpsLoc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_ap_finder_phase1);

		// GPS location receiver
		gpsLoc = new GpsLocationReceiver();

		// ap hashmap creation
		apLocMap = new HashMap<String, wifiApLocObject>();

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

	public void computeP1(View view) {
		// alertBox("Compute Module TBD");

		sortApList = new ArrayList<wifiApLocObject>();
		String displayList[], locations[];
		int i = 0;

		// put all the hashmap values into a list
		for (Map.Entry<String, wifiApLocObject> map : apLocMap.entrySet()) {
			sortApList.add(map.getValue());
		}

		// sort the new list based upon the power value
		Collections.sort(sortApList, new Comparator<wifiApLocObject>() {
			public int compare(wifiApLocObject o1, wifiApLocObject o2) {
				return (o1.getPower()).compareTo(o2.getPower());
			}
		});
		// Collections.reverse(sortApList);

		displayList = new String[sortApList.size()];
		locations = new String[sortApList.size()];

		for (i = 0; i < sortApList.size(); i++) {
			displayList[i] = sortApList.get(i).getDisplayString(i + 1);
			locations[i] = (sortApList.get(i).getQueryStrFromLocation(i));
		}

		// list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
		// android.R.layout.simple_list_item_1, locations));

		suggestions.setAdapter(new ArrayAdapter<String>(
				getApplicationContext(), android.R.layout.simple_list_item_1,
				displayList));
		suggestions.setOnItemClickListener(computeListListener);
	}

	// Create a message handling object as an anonymous class.
	private OnItemClickListener computeListListener = new OnItemClickListener() {
		public void onItemClick(
				@SuppressWarnings("rawtypes") AdapterView parent, View v,
				int position, long id) {
			// open the maps for the position
			showMap((String) sortApList.get(position).getQueryStrFromLocation(
					position));
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

		private static final int WIFI_CHANNEL_UNKNOWN = -1;

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
			int channelUsed[] = new int[15]; // 0 index unused.
			for (int i = 0; i < wifiScanList.size(); i++) {
				double dBm = (double) wifiScanList.get(i).level;
				dBmToWatt[i] = (Math.pow(10, (dBm / 10))) / 1000;
				totaldBm += dBmToWatt[i];
			}

			totaldBm = 10 * Math.log10(1000 * totaldBm);

			for (int i = 0; i < wifiScanList.size(); i++) {
				int channel = getChannelFromFrequency(wifiScanList.get(i).frequency);
				if (channel == WIFI_CHANNEL_UNKNOWN) {
					wifis[i] = ((wifiScanList.get(i)).toString());
				} else {
					wifis[i] = ((wifiScanList.get(i)).toString())
							+ ", (2.4Ghz@Ch:" + channel + ")";
					channelUsed[channel]++;
				}
				wifis[i] += ", Strength Level:"
						+ getSignalStrength(wifiScanList.get(i).level);
				wifis[i] += ", dBmLog:" + dBmToWatt[i] + ", totaldBm:"
						+ totaldBm;
			}

			String freeChannel = new String();
			for (int channelIndex = 1; channelIndex < channelUsed.length; channelIndex++) {
				if (channelUsed[channelIndex] == 0) {
					freeChannel += channelIndex + ",";
				}
			}

			if (freeChannel.endsWith(",")) {
				freeChannel = freeChannel
						.substring(0, freeChannel.length() - 1);
			}

			Location lastLoc = geoLocations.get(geoLocations.size() - 1);
			apObj = new wifiApLocObject(new Double(totaldBm), lastLoc);
			apObj.setChannel(freeChannel);

			String key = lastLoc.getLatitude() + "," + lastLoc.getLongitude();
			apLocMap.put(key, apObj);

			list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
					android.R.layout.simple_list_item_1, wifis));
			
			suggestions.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
					android.R.layout.simple_list_item_1, new String[]{apObj.toString()}));
			

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
		private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // always

		// The minimum time between updates in milliseconds
		private static final long MIN_TIME_BW_UPDATES = (long) (1000 * 10); // 10
																			// seconds
		// seconds

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
						if (location == null) {
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
			this.location = location;
//			Toast.makeText(getApplicationContext(), "GPS location updated"+location,
//					Toast.LENGTH_SHORT).show();
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
		Double dBmValue;
		Location coords;
		String freeChannel;

		public wifiApLocObject(Double dBmValue, Location coords) {
			this.dBmValue = dBmValue;
			this.coords = coords;
			this.freeChannel = "";
		}

		public String toString() {
			return ("GeoString:" + getQueryStrFromLocation(coords, "?") + ","
					+ "dBm:" + dBmValue + "," + "Freechannel:" + freeChannel);
		}

		String getDisplayString(int positionIndex) {
			return ("AP Scan Location[" + positionIndex + "]\nTotal dBm:"
					+ dBmValue + "\nFree Channels:" + freeChannel);
		}

		Double getPower() {
			return dBmValue;
		}

		Location getLoc() {
			return coords;
		}

		void setChannel(String ch) {
			this.freeChannel = ch;
		}

		String getChannel() {
			return this.freeChannel;
		}

		public String getQueryStrFromLocation(Location loc, String iLabel) {
			double gLatitude, gLongitude;
			String label = iLabel;
			String zoomSetting = "&z=23";
			gLatitude = loc.getLatitude();
			gLongitude = loc.getLongitude();
			String geoUriBegin = "geo:" + gLatitude + "," + gLongitude;
			String geoUriQuery = "" + gLatitude + "," + gLongitude + "("
					+ label + ")";
			String encodedQuery = Uri.encode(geoUriQuery);
			String uriString = geoUriBegin + "?q=" + encodedQuery + zoomSetting;
			return uriString;
		}

		String getQueryStrFromLocation(int index) {
			double gLatitude, gLongitude;
			String label = "AP[" + (index + 1) + "]";
			String zoomSetting = "&z=23";
			gLatitude = coords.getLatitude();
			gLongitude = coords.getLongitude();
			String geoUriBegin = "geo:" + gLatitude + "," + gLongitude;
			String geoUriQuery = "" + gLatitude + "," + gLongitude + "("
					+ label + ")";
			String encodedQuery = Uri.encode(geoUriQuery);
			String uriString = geoUriBegin + "?q=" + encodedQuery + zoomSetting;
			return uriString;
		}

	}

	public class dataComparator implements Comparator<wifiApLocObject> {
		@Override
		public int compare(wifiApLocObject o1, wifiApLocObject o2) {
			return o1.getPower().compareTo(o2.getPower());
		}
	}
}
