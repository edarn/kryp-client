package se.tna.krypgrund;

import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SetupActivity extends Activity {

	public static final String SEND_TO_SERVER_DELAY_VIEW = "Time_Between_Send_To_Server_View";
	public static final String SEND_TO_SERVER_DELAY_MS = "Time_Between_Send_To_Server_Ms";

	// public static final String SENSOR_TYPE = "Sensor_Type";
	public static final String MEASUREMENT_DELAY_VIEW = "Time_Between_Each_Measurement_View";
	public static final String MEASUREMENT_DELAY_MS = "Time_Between_Each_Measurement_Ms";
	// public static final String READ_INTERVAL =
	// "Time_Between_Each_Measurement";
	public static final String STATION_NAME = "Station_Name";
	public static final String SENSOR_TYPE_RADIO = "Radio_Button_Id_Type";
	private static final String GPS_LONGITUDE = "Longitude";
	private static final String GPS_LATITUDE = "Latitude";
	private EditText name;
	private EditText latitude;
	private EditText longitude;

	private long sendToServerDelayMs = 0;
	private long measuremetDelayMs = 0;

	private SharedPreferences prefs;
	private Editor prefsEditor;

	MyLocationListener locListner = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup);

		RadioGroup updateFrequency;
		RadioGroup sensorType;
		RadioGroup measurementDelay;

		// Find views
		name = (EditText) findViewById(R.id.stationName);
		latitude = (EditText) findViewById(R.id.latPosition);
		longitude = (EditText) findViewById(R.id.longPosition);
		updateFrequency = (RadioGroup) findViewById(R.id.updateFrequency);
		measurementDelay = (RadioGroup) findViewById(R.id.measurementDelay);
		sensorType = (RadioGroup) findViewById(R.id.sensorType);

		// Find stored settings
		prefs = getSharedPreferences("TNA_Sensor", MODE_PRIVATE);
		prefsEditor = prefs.edit();
		int type = prefs.getInt(SENSOR_TYPE_RADIO, R.id.crawlspaceStation);
		sensorType.check(type);
		updateFrequency.check(prefs.getInt(SEND_TO_SERVER_DELAY_VIEW,
				R.id.oneMinute));
		measurementDelay.check(prefs.getInt(MEASUREMENT_DELAY_VIEW,
				R.id.twoSecondsMeasurement));

		String sensorName = prefs.getString(STATION_NAME, "");
		name.setText(sensorName);

		String lat = prefs.getString(GPS_LATITUDE, "55");
		String lon = prefs.getString(GPS_LONGITUDE, "13");

		latitude.setText(lat);
		longitude.setText(lon);

		Button b = (Button) findViewById(R.id.saveButton);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// prefsEditor.putInt(SENSOR_TYPE, stationType);
				prefsEditor.putLong(SEND_TO_SERVER_DELAY_MS,
						sendToServerDelayMs);
				prefsEditor.putLong(MEASUREMENT_DELAY_MS, measuremetDelayMs);

				prefsEditor.putString(STATION_NAME, name.getText().toString());
				prefsEditor.putString(GPS_LATITUDE, latitude.getText()
						.toString());
				prefsEditor.putString(GPS_LONGITUDE, longitude.getText()
						.toString());

				prefsEditor.apply();
				prefsEditor.commit();

				LocationData l = new LocationData();

				TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				l.Imei = telephonyManager.getDeviceId();
				l.Latitude = Double.parseDouble(latitude.getText().toString());
				l.Longitude = Double
						.parseDouble(longitude.getText().toString());
				l.SensorName = name.getText().toString();
				SendLocationDataToServer(l);
				finish();
			}
		});
		locListner = new MyLocationListener(this);

	}

	private void SendLocationDataToServer(final LocationData locData) {
		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				HttpClient client = null;
				JSONObject data;

				try {
					data = new JSONObject();

					client = new DefaultHttpClient();
					HttpPost message = new HttpPost(
							"http://www.surfvind.se/AddSurfvindLocationIOIOv1.php");
					message.addHeader("content-type",
							"application/x-www-form-urlencoded");
					data.put("Imei", locData.Imei);
					data.put("Latitude", locData.Latitude);
					data.put("Longitude", locData.Longitude);
					data.put("SensorName", locData.SensorName);
					data.put("Version", "Setup1.0");
					message.setEntity(new StringEntity(data.toString()));
					HttpResponse response = client.execute(message);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						InputStreamReader r = new InputStreamReader(response
								.getEntity().getContent());
						char c[] = new char[100];
						while (r.read() != -1) {
							r.read(c);
							System.out.println(c);
						}

					} else {
						// / SendSuccess = false;
					}
				} catch (Exception e) {

				} finally {
					if (null != client)
						client.getConnectionManager().shutdown();
				}
				return true;
			}
		}.execute();

	}

	public static int getSensorType(int radioId) {
		int stationType = R.id.weatherStation; // Default
		if (radioId == R.id.weatherStation)
			stationType = KrypgrundsService.SURFVIND;
		else if (radioId == R.id.crawlspaceStation)
			stationType = KrypgrundsService.KRYPGRUND;
		return stationType;
	}

	public static long getUpdateInterval(int radioId) {
		long readInterval = TimeUnit.MINUTES.toMillis(1); // Default
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

	@Override
	protected void onStart() {
		super.onStart();
		System.out.println("Jodå");
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_MEDIUM);
		locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,locListner, null);
		locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, TimeUnit.SECONDS.toMillis(5), 5 , locListner);
		

	}

	@Override
	protected void onStop() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(locListner);
		super.onStop();
	}

	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		switch (view.getId()) {
		// Update interval
		case R.id.oneMinute:
			if (checked) {
				sendToServerDelayMs = TimeUnit.MINUTES.toMillis(1);
				prefsEditor.putInt(SEND_TO_SERVER_DELAY_VIEW, view.getId());
			}
			break;
		case R.id.fiveMinutes:
			if (checked) {
				sendToServerDelayMs = TimeUnit.MINUTES.toMillis(5);
				prefsEditor.putInt(SEND_TO_SERVER_DELAY_VIEW, view.getId());
			}
			break;
		case R.id.tenMinutes:
			if (checked) {
				sendToServerDelayMs = TimeUnit.MINUTES.toMillis(10);
				prefsEditor.putInt(SEND_TO_SERVER_DELAY_VIEW, view.getId());
			}
			break;
		// Station type
		case R.id.weatherStation:
			if (checked) {
				prefsEditor.putInt(SENSOR_TYPE_RADIO, view.getId());
			}
			break;
		case R.id.crawlspaceStation:
			if (checked) {
				prefsEditor.putInt(SENSOR_TYPE_RADIO, view.getId());
			}
			break;
		// Measurement speed
		case R.id.twoSecondsMeasurement:
			if (checked) {
				measuremetDelayMs = TimeUnit.SECONDS.toMillis(2);
				prefsEditor.putInt(MEASUREMENT_DELAY_VIEW, view.getId());
			}
			break;
		case R.id.tenSecondsMeasurement:
			if (checked) {
				measuremetDelayMs = TimeUnit.SECONDS.toMillis(10);
				prefsEditor.putInt(MEASUREMENT_DELAY_VIEW, view.getId());
			}
			break;
		case R.id.oneMinuteMeasurement:
			if (checked) {
				measuremetDelayMs = TimeUnit.MINUTES.toMillis(1);
				prefsEditor.putInt(MEASUREMENT_DELAY_VIEW, view.getId());
			}
			break;
		}
	}

	public void setPosition(double lat, double longi) {
		latitude.setText(String.valueOf(lat));
		longitude.setText(String.valueOf(longi));
	}
}