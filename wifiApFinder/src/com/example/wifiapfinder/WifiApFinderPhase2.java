package com.example.wifiapfinder;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.wifiapfinder.WifiApFinderPhase1.WifiScanReceiver;

public class WifiApFinderPhase2 extends Activity {
	WifiManager wifiMgrPhase2;
	WifiScanReceiver wifiRecieverPhase2;
	ListView list, result;
	ArrayList<String> wifis;
	String displayList[];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_ap_finder_phase2);

		displayList = new String[] { "praveen", "kumar", "shanmugam",
				"praveen", "kumar", "shanmugam", "praveen", "kumar",
				"shanmugam" };

		list = (ListView) findViewById(R.id.listView3);
		result = (ListView) findViewById(R.id.listView4);

		list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, displayList));
		
		wifis = new ArrayList<String>();
		wifiMgrPhase2 = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	}

	public void recordReadingP2(View view) {
//		alertBox("Record P2");
		String dis[] = new String[1];
		WifiInfo w = wifiMgrPhase2.getConnectionInfo();
		dis[0] = w.toString();
		wifis.add(w.toString());
		list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, dis));
	}

	public void computeP2(View view) {
//		alertBox("Compute P2");
		String dis[] = new String[wifis.size()];
		for(int i=0;i < wifis.size(); i++){
			dis[i] = wifis.get(i);
		}
		result.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, dis));
	}

	public void alertBox(String text) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Info");
		alert.setMessage(text);
		alert.setPositiveButton("OK", null);
		alert.show();
	}

}
