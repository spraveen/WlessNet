package com.example.wifiapfinder;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
	LocationManager locationManager;
	GpsLocationReceiver gpsLoc;
	String gpsProvider;
	Location location;

	private static final String TAG = "Phase1";

	/* GPS Location update parameters */
	public long minTime = 10;
	public float minDistance = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_wifi_ap_finder_phase1);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
		gpsProvider = locationManager.getBestProvider(criteria, false);
		gpsLoc = new GpsLocationReceiver();
		location = locationManager.getLastKnownLocation(gpsProvider);

		// wifi scan results
		list = (ListView) findViewById(R.id.listView1);
		wifiMgrPhase1 = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiRecieverPhase1 = new WifiScanReceiver();
		Log.v(TAG, "PHase1 activity created..");

	}

	public void closeActivity(View view) {
		locationManager.removeUpdates(gpsLoc);
		this.finish();
	}

	/* Request updates at startup */
	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(gpsProvider, minTime,
				minDistance, gpsLoc);
	}

	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(gpsLoc);
		unregisterReceiver(wifiRecieverPhase1);
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
		public double latitude;
		public double longitude;

		@Override
		public void onLocationChanged(Location location) {
			latitude = (location.getLatitude());
			longitude = (location.getLongitude());
			Toast.makeText(getApplicationContext(),
					"Lat:Long " + latitude + ":" + longitude, Toast.LENGTH_LONG)
					.show();

			alertBox("Wifi Scanning In Progres..");

			// once you get the location read the wifi signals..
			wifiMgrPhase1.startScan();
			registerReceiver(wifiRecieverPhase1, new IntentFilter(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

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
