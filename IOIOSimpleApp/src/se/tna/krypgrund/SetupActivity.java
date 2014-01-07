package se.tna.krypgrund;

import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SetupActivity extends Activity {

	private static final String UPDATE_FREQ = "Time_Between_Reads";
	private static final String SENSOR_TYPE = "Sensor_Type";
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
		name = (EditText) findViewById(R.id.stationName);
		updateFrequency = (RadioGroup) findViewById(R.id.updateFrequency);
		sensorType = (RadioGroup) findViewById(R.id.sensorType);
		prefs = getSharedPreferences("TNA_Sensor", MODE_PRIVATE);
		prefsEditor = prefs.edit();
		int sensorType = prefs.getInt(SENSOR_TYPE, R.id.weatherStation);
		updateFrequency.check(sensorType);
		int readInterval = prefs.getInt(UPDATE_FREQ, R.id.oneMinute);
		updateFrequency.check(readInterval);
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

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
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
				prefsEditor.putInt(SENSOR_TYPE, view.getId());
			}
			break;
		case R.id.crawlspaceStation:
			if (checked) {
				stationType = KrypgrundsService.SURFVIND;
				prefsEditor.putInt(SENSOR_TYPE, view.getId());
			}
			break;
		}
	}
}
