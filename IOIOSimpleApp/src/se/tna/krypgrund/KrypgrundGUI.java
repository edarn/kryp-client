package se.tna.krypgrund;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;

import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class KrypgrundGUI extends Activity {
	private SeekBar seekFuktInne;
	private SeekBar seekTempInne;
	private SeekBar seekFuktUte;
	private SeekBar seekTempUte;
	private TextView textFuktInne;
	private TextView textFuktUte;
	private TextView textTempInne;
	private TextView textTempUte;
	private TextView debugText;
	private TextView initializedText;
	private TextView fanStatus;
	private TextView phoneId;

	private ToggleButton toggleButton_;
	private ToggleButton debugButton;
	private ToggleButton forceSendDataButton;
	private ToggleButton toggleFanButton;

	private KrypgrundsService kryp = null;
	private ServiceConnection mConnection = null;
	private TextView textFanOn;
	private TextView textWindSpeed;
	private TextView textWindDirection;
	private SeekBar windSeekBar;
	private TextView textAnalogInput;

	@Override
	public void onPause() {

		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings, menu);
		/*
		 * preferences = getSharedPreferences("Menus", Activity.MODE_PRIVATE);
		 * MenuItem item = null; item = menu.findItem(R.id.setup); if (null !=
		 * item) { item.setChecked(preferences.getBoolean("useSurfvind",
		 * false)); // item.setChecked(true); } item =
		 * menu.findItem(R.id.useKrypgrund); if (null != item) {
		 * item.setChecked(preferences.getBoolean("useKrypgrund", false)); }
		 */
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection

		/*
		 * preferences = getSharedPreferences("Menus", Activity.MODE_PRIVATE);
		 * 
		 * PopupMenu popup = new PopupMenu(this, forceSendDataButton);
		 * MenuInflater inflater = popup.getMenuInflater(); Editor s =
		 * preferences.edit(); switch (item.getItemId()) { case
		 * R.id.useKrypgrund: inflater.inflate(R.menu.krypgrund_actions,
		 * popup.getMenu()); s.putBoolean("useKrypgrund", true); break; case
		 * R.id.useSurfvind: inflater.inflate(R.menu.surfvind_actions,
		 * popup.getMenu()); s.putBoolean("useSurfvind",
		 * !preferences.getBoolean("useSurfvind", false)); break; default:
		 * return super.onOptionsItemSelected(item); } s.apply();
		 * 
		 * popup.show();
		 */
		startActivity(new Intent(this, SetupActivity.class));
		return true;
	}

	public void showPopup(View v) {

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (serviceBound && mConnection != null) {
			unbindService(mConnection);
			mConnection = null;

		}
	}

	boolean serviceBound = false;
	SharedPreferences preferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				kryp = ((KrypgrundsService.MyBinder) service).getService();
				Toast.makeText(KrypgrundGUI.this, "Connected", Toast.LENGTH_SHORT).show();
				serviceBound = true;
				kryp.updateSettings();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				kryp = null;
				serviceBound = false;
				Toast.makeText(KrypgrundGUI.this, "DisConnected", Toast.LENGTH_SHORT).show();

			}
		};

		preferences = getSharedPreferences("Menus", Activity.MODE_PRIVATE);
		preferences.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				preferences = sharedPreferences;

			}
		});

		setContentView(R.layout.main);

		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String id = telephonyManager.getDeviceId();

		// debug
		id = "358848043355882";

//		WebView myWebView = (WebView) findViewById(R.id.webView);
//		myWebView.loadUrl("http://www.surfvind.se/Applet/" + id + "/graph_0.png");
//		myWebView.setBackgroundColor(0x00000000);
		WebView compassView = (WebView) findViewById(R.id.webViewCompass);
		compassView.loadUrl("http://www.surfvind.se/Images/" + id + "_img_compass.png");
		compassView.setBackgroundColor(0x00000000);
		WebView compassViewBackground = (WebView) findViewById(R.id.webViewCompassBackground);
		compassViewBackground.loadUrl("http://www.surfvind.se/Images/ws_compass.png");
		compassViewBackground.setBackgroundColor(0x00000000);

		WebView speedView = (WebView) findViewById(R.id.webViewSpeed);
		speedView.loadUrl("http://www.surfvind.se/Images/" + id + "_img_speed.png");
		speedView.setBackgroundColor(0x00000000);
		WebView speedViewBackground = (WebView) findViewById(R.id.webViewSpeedBackground);
		speedViewBackground.loadUrl("http://www.surfvind.se/Images/ws_speed.png");
		speedViewBackground.setBackgroundColor(0x00000000);

		textWindSpeed = (TextView) findViewById(R.id.textWindSpeed);
		textWindDirection = (TextView) findViewById(R.id.textWindDirection);
		textAnalogInput = (TextView) findViewById(R.id.textAnalogInput);
		windSeekBar = (SeekBar) findViewById(R.id.seekAnalogInput);
		windSeekBar.setMax(330);

		fanStatus = (TextView) findViewById(R.id.fanStatus);
		debugText = (TextView) findViewById(R.id.debugText);
		seekFuktInne = (SeekBar) findViewById(R.id.seekFuktInne);
		seekFuktUte = (SeekBar) findViewById(R.id.seekFuktUte);
		seekTempInne = (SeekBar) findViewById(R.id.seekTempInne);
		seekTempUte = (SeekBar) findViewById(R.id.seekTempUte);

		textFuktInne = (TextView) findViewById(R.id.textFuktInne);
		textFuktUte = (TextView) findViewById(R.id.textFuktUte);
		textTempInne = (TextView) findViewById(R.id.textTempInne);
		textTempUte = (TextView) findViewById(R.id.textTempUte);
		textFanOn = (TextView) findViewById(R.id.textFanOn);
		initializedText = (TextView) findViewById(R.id.connectedText);
		phoneId = (TextView) findViewById(R.id.phoneId);
		seekFuktInne.setMax(400);
		seekFuktUte.setMax(100);
		seekTempInne.setMax(60);
		seekTempUte.setMax(60);
		toggleButton_ = (ToggleButton) findViewById(R.id.ToggleButton);
		toggleFanButton = (ToggleButton) findViewById(R.id.toggleFanButton);
		debugButton = (ToggleButton) findViewById(R.id.DebugButton1);
		forceSendDataButton = (ToggleButton) findViewById(R.id.ForceSendDataButton);

		enableUi(false);

		Intent service = new Intent(this, KrypgrundsService.class);
		this.startService(service);

		bindService(new Intent(this, KrypgrundsService.class), mConnection, Context.BIND_AUTO_CREATE);

		Timer timer = new Timer();

		TimerTask t = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (null != kryp) {
					updateUI();
					// setStatusText(kryp.statusText, kryp.isInitialized);
					kryp.setDebugMode(debugButton.isChecked());
					enableUi(debugButton.isChecked());
					if (debugButton.isChecked()) {

						KrypgrundStats debugData = null;
						// Must this be run on the UI thread?
						debugData = new KrypgrundStats();
						debugData.moistureInne = seekFuktInne.getProgress();
						debugData.moistureUte = seekFuktUte.getProgress();
						debugData.temperatureInne = seekTempInne.getProgress();
						debugData.temperatureUte = seekTempUte.getProgress();

						kryp.setDebugStats(debugData);
						kryp.setForceFan(toggleFanButton.isChecked());

					}
					kryp.setForceSendData(forceSendDataButton.isChecked());

				}
			}

		};

		timer.scheduleAtFixedRate(t, 0, 400);

	}

	private void enableUi(final boolean enable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				toggleButton_.setEnabled(enable);
				seekTempUte.setEnabled(enable);
				seekTempInne.setEnabled(enable);
				seekFuktInne.setEnabled(enable);
				seekFuktUte.setEnabled(enable);

			}
		});
	}

	private void updateUI() {
		if (null != kryp) {
			final StatusOfService status = kryp.getStatus();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (status.fanOn) {
						fanStatus.setText("Fan is RUNNING");
					} else {
						fanStatus.setText("Fan is OFF");

					}

					textAnalogInput.setText("Analog Input: " + String.format("%.2f", status.analogInput));
					textWindDirection.setText("Vindriktning: " + status.windDirection);
					textWindSpeed.setText("Vindhastighet: " + String.format("%.2f", status.windSpeed));
					windSeekBar.setProgress(status.windDirection);

					textTempUte.setText("Temp Ute: " + String.format("%.2f", status.temperatureUte));
					textTempInne.setText("Temp Inne: " + String.format("%.2f", status.temperatureInne));
					textFuktUte.setText("Fukt Ute: " + String.format("%.2f", status.moistureUte));
					textFuktInne.setText("Fukt Inne: " + String.format("%.2f", status.moistureInne));
					textFanOn.setText("Fan On =" + status.fanOn);

					// Is ioio chip initialized etc
					initializedText.setText(status.statusMessage);
					phoneId.setText("IMEI:" + status.deviceId);
					StringBuilder sb = new StringBuilder();
					sb.append("HistorySize: ");
					sb.append(status.historySize);
					sb.append("\n");
					sb.append("ReadingSize: ");
					sb.append(status.readingSize);
					sb.append("\n");

					sb.append("TimeOfCreation: ");

					Time tt = new Time();
					tt.set(status.timeOfCreation);
					sb.append(tt.format2445());
					sb.append("\nTimeSinceLastSend: ");
					tt.set(status.timeForLastSendData);
					sb.append(tt.format2445());

					debugText.setText(sb.toString());
					// debugText.setText(status.)

					if (!debugButton.isChecked()) {

						seekTempUte.setProgress((int) status.temperatureUte + 20);
						seekTempInne.setProgress((int) status.temperatureInne + 20);
						seekFuktInne.setProgress((int) status.moistureInne);
						seekFuktUte.setProgress((int) status.moistureUte);
					}

				}
			});
		}
	}

}
