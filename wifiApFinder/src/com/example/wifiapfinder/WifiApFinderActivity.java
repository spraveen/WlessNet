package com.example.wifiapfinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.wifiapfinder.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class WifiApFinderActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_wifi_ap_finder);
	}

	public void startPhase1(View view) {
		Intent intent = new Intent(this, WifiApFinderPhase1.class);
		startActivity(intent);
	}

}
