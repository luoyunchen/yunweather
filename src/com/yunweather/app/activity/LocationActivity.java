package com.yunweather.app.activity;

import com.yunweather.app.R;
import com.yunweather.app.model.Location;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class LocationActivity extends Activity{
	private Location location;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.location_layout);
		
		location = new Location(null);
	}

	
}
