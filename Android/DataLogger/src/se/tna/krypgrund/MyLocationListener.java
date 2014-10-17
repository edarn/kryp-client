package se.tna.krypgrund;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class MyLocationListener implements LocationListener {

	public double longitude = 0;
	public double latitude = 0;
	public float accuracy = 0;
	SetupActivity setupActivity = null;

	public MyLocationListener(SetupActivity act) {
		setupActivity = act;
	}

	@Override
	public void onLocationChanged(Location loc) {
		longitude = loc.getLongitude();
		latitude = loc.getLatitude();
		accuracy = loc.getAccuracy();
		setupActivity.setPosition(latitude,longitude);
		System.out.println("Long: " + longitude + " Latitude: " + latitude + " Acc: " + accuracy);
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