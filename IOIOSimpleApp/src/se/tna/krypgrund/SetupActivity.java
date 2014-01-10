<<<<<<< HEAD
package se.tna.krypgrund;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class SetupActivity extends Activity {
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onContextItemSelected(item);
	}

	@Override
	public void onContextMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		super.onContextMenuClosed(menu);
	}

	MyLocationListener locListner = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		locListner = new MyLocationListener();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_COARSE);

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 5, locListner);

	}

	@Override
	protected void onStop() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(locListner);
		super.onStop();
	}
}
=======
package se.tna.krypgrund;

import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SetupActivity extends Activity {

	private static final String UPDATE_FREQ = "Time_Between_Reads";
	private static final String SENSOR_TYPE = "Sensor_Type";
	protected static final String READ_INTERVAL = "Read_Interval";
	protected static final String STATION_NAME = "Station_Name";
	private static final String SENSOR_TYPE_RADIO = "Radio_Button_Id_Type";
	private EditText name;
	private RadioGroup updateFrequency;
	private RadioGroup sensorType;

	private long updateTime = 0;
	private int stationType = 0;
	private SharedPreferences prefs;
	private Editor prefsEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup);
		
		name = (EditText) findViewById(R.id.stationName);
		updateFrequency = (RadioGroup) findViewById(R.id.updateFrequency);
		sensorType = (RadioGroup) findViewById(R.id.sensorType);
		prefs = getSharedPreferences("TNA_Sensor", MODE_PRIVATE);
		prefsEditor = prefs.edit();
		int type = prefs.getInt(SENSOR_TYPE_RADIO, R.id.weatherStation);
		sensorType.check(type);
		int readInterval = prefs.getInt(UPDATE_FREQ, R.id.oneMinute);
		updateFrequency.check(readInterval);
		String sensorName = prefs.getString(STATION_NAME, "");
		name.setText(sensorName);
		Button b = (Button) findViewById(R.id.saveButton);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				prefsEditor.putInt(SENSOR_TYPE, stationType);
				prefsEditor.putLong(READ_INTERVAL, updateTime);
				prefsEditor.putString(STATION_NAME, name.getText().toString());
				prefsEditor.apply();
				finish();
			}
		});		
	}

	public static int getSensorType(int radioId)
	{
		int stationType = R.id.weatherStation; // Default
		if (radioId == R.id.weatherStation)
			stationType = KrypgrundsService.SURFVIND;
		else if (radioId == R.id.crawlspaceStation)
			stationType = KrypgrundsService.KRYPGRUND;
		return stationType;
	}
	public static long getUpdateInterval(int radioId)
	{
		long readInterval = TimeUnit.MINUTES.toMillis(1); //Default
		if (radioId == R.id.oneMinute)
			readInterval = TimeUnit.MINUTES.toMillis(1);
		else if (radioId == R.id.threeMinutes)
			readInterval = TimeUnit.MINUTES.toMillis(3);
		else if (radioId == R.id.fiveMinutes)
			readInterval = TimeUnit.MINUTES.toMillis(5);
		else if (radioId == R.id.tenMinutes)
			readInterval = TimeUnit.MINUTES.toMillis(10);
		return readInterval;
	}

	@Override
	protected void onDestroy() {
		prefsEditor.apply();
		super.onDestroy();
	}

	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		switch (view.getId()) {
		// Update interval
		case R.id.oneMinute:
			if (checked) {
				updateTime = TimeUnit.MINUTES.toMillis(1);
				prefsEditor.putInt(UPDATE_FREQ, view.getId());
			}
			break;
		case R.id.fiveMinutes:
			if (checked) {
				updateTime = TimeUnit.MINUTES.toMillis(5);
				prefsEditor.putInt(UPDATE_FREQ, view.getId());
			}
			break;
		case R.id.tenMinutes:
			if (checked) {
				updateTime = TimeUnit.MINUTES.toMillis(10);
				prefsEditor.putInt(UPDATE_FREQ, view.getId());
			}
			break;
		// Station type
		case R.id.weatherStation:
			if (checked) {
				stationType = KrypgrundsService.SURFVIND;
				prefsEditor.putInt(SENSOR_TYPE_RADIO, view.getId());
			}
			break;
		case R.id.crawlspaceStation:
			if (checked) {
				stationType = KrypgrundsService.KRYPGRUND;
				prefsEditor.putInt(SENSOR_TYPE_RADIO, view.getId());
			}
			break;
		}
	}
}
>>>>>>> 87234e83150471c07c2457b7e93730e1bc46cfd5
