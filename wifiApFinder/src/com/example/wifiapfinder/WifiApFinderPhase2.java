package com.example.wifiapfinder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import android.annotation.SuppressLint;
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
	ArrayList<WifiInfo> wifis;
	String displayList[];
	
	private final String wifiLevels[] = {"None", "Poor", "Bad", "Good", "Excellent"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_ap_finder_phase2);

//		displayList = new String[] { "praveen", "kumar", "shanmugam",
//				"praveen", "kumar", "shanmugam", "praveen", "kumar",
//				"shanmugam" };

		list = (ListView) findViewById(R.id.listView3);
		result = (ListView) findViewById(R.id.listView4);

//		list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
//				android.R.layout.simple_list_item_1, displayList));
		
		wifis = new ArrayList<WifiInfo>();
		wifiMgrPhase2 = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	}

	public void recordReadingP2(View view) {
//		alertBox("Record P2");
		String dis[] = new String[1];
		WifiInfo w = wifiMgrPhase2.getConnectionInfo();
		dis[0] = w.toString();
		wifis.add(w);
		list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, dis));
	}

	@SuppressLint("DefaultLocale")
	public void computeP2(View view) {
//		alertBox("Compute P2");
		String dis[] = new String[wifis.size()];
		for(int i=0;i < wifis.size(); i++){
			dis[i] = wifis.get(i).toString();
		}
		double avgdBm = 0;
		double[] dBmToWatt = new double[wifis.size()];
		for (int i = 0; i < wifis.size(); i++) {
			double dBm = (double) wifis.get(i).getRssi();
			dBmToWatt[i] = (Math.pow(10, (dBm / 10))) / 1000;
			avgdBm += dBmToWatt[i];
		}
		avgdBm = avgdBm/wifis.size();
		avgdBm = 10 * Math.log10(1000 * avgdBm);
		
		int levels = WifiManager.calculateSignalLevel((int)avgdBm, wifiLevels.length);
		
		ArrayList<String> computeResult = new ArrayList<String>();
		if(wifis.size() > 0){
			WifiInfo w = wifis.get(0);
			computeResult.add("SSID				:"+w.getSSID());
			computeResult.add("BSSID			:"+w.getBSSID());
			computeResult.add("MAC				:"+w.getMacAddress());
			int ipAddress = w.getIpAddress();
			String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
					(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
					(ipAddress >> 24 & 0xff));
			
			computeResult.add("IP Address		:"+ip);
			computeResult.add("Supplicant State	:"+w.getSupplicantState());
			
			DecimalFormat newFormat = new DecimalFormat("#.##");
			
			computeResult.add("Avg RSSI			:"+newFormat.format(avgdBm).toString() +" dBm");
			computeResult.add("Avg Signal Level	:"+wifiLevels[levels]);
			computeResult.add("Link Speed		:"+w.getLinkSpeed() +" Mbps");
			computeResult.add("Network ID		:"+w.getNetworkId());
			
			String finalResult[] = new String[computeResult.size()];
			for(int i =0 ;i < computeResult.size();i++)
				finalResult[i] = computeResult.get(i);
			
			result.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
					android.R.layout.simple_list_item_1, finalResult));
		}
		
		list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
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
