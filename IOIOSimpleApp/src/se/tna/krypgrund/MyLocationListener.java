package se.tna.krypgrund;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class MyLocationListener implements LocationListener {

	public double longitude = 0;
	public double latitude = 0;
	public float accuracy = 0;

	public MyLocationListener() {
	}

	@Override
	public void onLocationChanged(Location loc) {
		longitude = loc.getLongitude();
		latitude = loc.getLatitude();
		accuracy = loc.getAccuracy();
	}
	

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}