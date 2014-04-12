package com.example.wifiapfinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */

public class WifiApFinderActivity extends Activity {
	ListView menuList;

	String menuItems[] = { "Phase1 Test", "Phase2 Test" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_wifi_ap_finder);
		menuList = (ListView) findViewById(R.id.menuList1);

		menuList.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, menuItems));

		menuList.setOnItemClickListener(mMessageClickedHandler);
	}

	// Create a message handling object as an anonymous class.
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
		public void onItemClick(
				@SuppressWarnings("rawtypes") AdapterView parent, View v,
				int position, long id) {
			if(position == 0){
				startPhase1(v);
			}else if(position == 1){
				startPhase2(v);
			}
		}
	};

	public void startPhase1(View view) {
		Intent intent = new Intent(this, WifiApFinderPhase1.class);
		startActivity(intent);
	}
	
	public void startPhase2(View view) {
		Intent intent = new Intent(this, WifiApFinderPhase2.class);
		startActivity(intent);
	}

}
