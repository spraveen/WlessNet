package com.example.wifiapfinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class WifiApFinderPhase2 extends Activity {
	ListView list;
	String displayList[];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_ap_finder_phase2);

		displayList = new String[] { "praveen", "kumar", "shanmugam",
				"praveen", "kumar", "shanmugam", "praveen", "kumar",
				"shanmugam" };

		list = (ListView) findViewById(R.id.listView3);

		list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, displayList));
	}

	public void recordReadingP2(View view) {
		alertBox("Record P2");
	}

	public void computeP2(View view) {
		alertBox("Compute P2");
	}

	public void alertBox(String text) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Info");
		alert.setMessage(text);
		alert.setPositiveButton("OK", null);
		alert.show();
	}

}
