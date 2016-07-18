package se.tna.crawlspacemonitor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import se.tna.commonloggerservice.KrypgrundsService;
import se.tna.commonloggerservice.LocationData;

public class SetupActivity extends Activity {

	public static final String SEND_TO_SERVER_DELAY_VIEW = "Time_Between_Send_To_Server_View";
	public static final String SEND_TO_SERVER_DELAY_MS = "Time_Between_Send_To_Server_Ms";

	public static final String SENSOR_TYPE = "Sensor_Type";
	public static final String MEASUREMENT_DELAY_VIEW = "Time_Between_Each_Measurement_View";
	public static final String MEASUREMENT_DELAY_MS = "Time_Between_Each_Measurement_Ms";
	// public static final String READ_INTERVAL =
	// "Time_Between_Each_Measurement";
	public static final String STATION_NAME = "Station_Name";
	public static final String SENSOR_TYPE_RADIO = "Radio_Button_Id_Type";
	private static final String GPS_LONGITUDE = "Longitude";
	private static final String GPS_LATITUDE = "Latitude";
    private String stationType=KrypgrundsService.SURFVIND;
	private EditText name;
	private EditText latitude;
	private EditText longitude;

	private long sendToServerDelayMs = 0;
	private long measurementDelayMs = 0;

	private SharedPreferences prefs;
	private Editor prefsEditor;

	MyLocationListener locationListener = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*setContentView(R.layout.setup);

		RadioGroup updateFrequency;
		RadioGroup measurementDelay;

		// Find views
		name = (EditText) findViewById(R.id.stationName);
		latitude = (EditText) findViewById(R.id.latPosition);
		longitude = (EditText) findViewById(R.id.longPosition);
		updateFrequency = (RadioGroup) findViewById(R.id.updateFrequency);
		measurementDelay = (RadioGroup) findViewById(R.id.measurementDelay);

		// Find stored settings
		prefs = getSharedPreferences("TNA_Sensor", MODE_PRIVATE);
		prefsEditor = prefs.edit();

        updateFrequency.check(prefs.getInt(SEND_TO_SERVER_DELAY_VIEW,
				R.id.oneMinute));
		measurementDelay.check(prefs.getInt(MEASUREMENT_DELAY_VIEW,
				R.id.twoSecondsMeasurement));

		String sensorName = prefs.getString(STATION_NAME, "");

		if (sensorName.isEmpty()) {
			BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
			sensorName = myDevice.getName();
		}

		name.setText(sensorName);

		String lat = prefs.getString(GPS_LATITUDE, "55");
		String lon = prefs.getString(GPS_LONGITUDE, "13");

		latitude.setText(lat);
		longitude.setText(lon);

		Button b = (Button) findViewById(R.id.saveButton);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				prefsEditor.putString(KrypgrundsService.LOGGER_MODE, stationType);
				prefsEditor.putLong(KrypgrundsService.SEND_TO_SERVER_DELAY_MS,
						sendToServerDelayMs);
				prefsEditor.putLong(KrypgrundsService.MEASUREMENT_DELAY_MS, measurementDelayMs);
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
		locationListener = new MyLocationListener(this);
		*/

	}

	private void SendLocationDataToServer(final LocationData locData) {
		new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    Toast.makeText(getApplicationContext(), "Settings saved successfully", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "FAILED to save settings", Toast.LENGTH_LONG).show();
                    //TODO: Google Analytics
                }
            }

            @Override
			protected Boolean doInBackground(Void... params) {
				OkHttpClient client = null;
				JSONObject data;
                Boolean sendSuccess = false;
				try {
					data = new JSONObject();


					client = new OkHttpClient();
					MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

					data.put("Imei", locData.Imei);
					data.put("Latitude", locData.Latitude);
					data.put("Longitude", locData.Longitude);
					data.put("SensorName", locData.SensorName);
					data.put("Version", "Setup1.0");


					Request request = new Request.Builder()
							.url("http://www.surfvind.se/AddSurfvindLocationIOIOv1.php")
							.post(RequestBody.create(MEDIA_TYPE_MARKDOWN, data.toString()))
							.build();

					Response response = client.newCall(request).execute();
					if (response.isSuccessful()) {
						sendSuccess = true;
					} else {
						sendSuccess = false;
					}

				} catch (Exception e) {
                    //TODO: Add Google Analytics

				}
				return sendSuccess;
			}
		}.execute();

	}

    @Override
	protected void onDestroy() {
		prefsEditor.apply();
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_MEDIUM);
		locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
		locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, TimeUnit.SECONDS.toMillis(5), 5 , locationListener);
		

	}

	@Override
	protected void onStop() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(locationListener);
		super.onStop();
	}
/*
	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		switch (view.getId()) {
		// Update interval
		case R.id.oneMinute:
			if (checked) {
				sendToServerDelayMs = TimeUnit.MINUTES.toMillis(1);
				prefsEditor.putLong(SEND_TO_SERVER_DELAY_VIEW, sendToServerDelayMs);
			}
			break;
        case R.id.threeMinutes:
            if (checked) {
                sendToServerDelayMs = TimeUnit.MINUTES.toMillis(3);
                prefsEditor.putLong(SEND_TO_SERVER_DELAY_VIEW, sendToServerDelayMs);
            }
            break;
		case R.id.fiveMinutes:
			if (checked) {
				sendToServerDelayMs = TimeUnit.MINUTES.toMillis(5);
				prefsEditor.putLong(SEND_TO_SERVER_DELAY_VIEW, sendToServerDelayMs);
			}
			break;
		case R.id.tenMinutes:
			if (checked) {
				sendToServerDelayMs = TimeUnit.MINUTES.toMillis(10);
				prefsEditor.putLong(SEND_TO_SERVER_DELAY_VIEW, sendToServerDelayMs);
			}
			break;
		// Measurement speed
		case R.id.twoSecondsMeasurement:
			if (checked) {
				measurementDelayMs = TimeUnit.SECONDS.toMillis(2);
				prefsEditor.putLong(MEASUREMENT_DELAY_VIEW, measurementDelayMs);
			}
			break;
		case R.id.tenSecondsMeasurement:
			if (checked) {
				measurementDelayMs = TimeUnit.SECONDS.toMillis(10);
				prefsEditor.putLong(MEASUREMENT_DELAY_VIEW, measurementDelayMs);
			}
			break;
		case R.id.oneMinuteMeasurement:
			if (checked) {
				measurementDelayMs = TimeUnit.MINUTES.toMillis(1);
				prefsEditor.putLong(MEASUREMENT_DELAY_VIEW, measurementDelayMs);
			}
			break;
		}
	}
	*/

	public void setPosition(double lat, double longi) {
		latitude.setText(String.valueOf(lat));
		longitude.setText(String.valueOf(longi));
	}
}