package com.example.wifiapfinder;

import java.util.List;

import com.example.wifiapfinder.WifiApFinderPhase1.WifiScanReceiver;
import com.example.wifiapfinder.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_wifi_ap_finder_phase1);

		list = (ListView) findViewById(R.id.listView1);
		wifiMgrPhase1 = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiRecieverPhase1 = new WifiScanReceiver();
		wifiMgrPhase1.startScan();

	}

	protected void onPause() {
		unregisterReceiver(wifiRecieverPhase1);
		super.onPause();
	}

	protected void onResume() {
		registerReceiver(wifiRecieverPhase1, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
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
}
