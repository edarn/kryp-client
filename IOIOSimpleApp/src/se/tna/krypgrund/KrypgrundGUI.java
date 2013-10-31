package se.tna.krypgrund;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
		mConnection = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.settings, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.useKrypgrund:
	        	
	          showPopup(seekFuktInne);
	            return true;
	        case R.id.useSurfvind:
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	public void showPopup(View v) {
	    PopupMenu popup = new PopupMenu(this, v);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.menu.actions, popup.getMenu());
	    popup.show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				kryp = ((KrypgrundsService.MyBinder) service).getService();
				Toast.makeText(KrypgrundGUI.this, "Connected",
						Toast.LENGTH_SHORT).show();

			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				kryp = null;

			}
		};

		setContentView(R.layout.main);
		textWindSpeed = (TextView) findViewById(R.id.textWindSpeed);
		textWindDirection = (TextView) findViewById(R.id.textWindDirection);
		textAnalogInput = (TextView) findViewById(R.id.textAnalogInput);
		windSeekBar = (SeekBar) findViewById(R.id.seekAnalogInput);
		windSeekBar.setMax(330);
		
		fanStatus = (TextView)  findViewById(R.id.fanStatus);
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

		bindService(new Intent(this, KrypgrundsService.class), mConnection,
				Context.BIND_AUTO_CREATE);

		Timer timer = new Timer();

		TimerTask t = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (null != kryp && null != kryp.data) {
					updateUI();
					//setStatusText(kryp.statusText, kryp.isInitialized);
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

		timer.scheduleAtFixedRate(t, 0, 2000);

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
					if (status.fanOn)
					{
					fanStatus.setText("Fan is RUNNING");
					}
					else
					{
						fanStatus.setText("Fan is OFF");
							
					}
					
					textAnalogInput.setText("Analog Input: " + String.format("%.2f",status.analogInput));
					textWindDirection.setText("Vindriktning: " +status.windDirection);
					textWindSpeed.setText("Vindhastighet: " + String.format("%.2f",status.windSpeed));
					windSeekBar.setProgress((int) status.analogInput);
					
					textTempUte.setText("Temp Ute: "
							+ String.format("%.2f", status.temperatureUte));
					textTempInne.setText("Temp Inne: "
							+ String.format("%.2f", status.temperatureInne));
					textFuktUte.setText("Fukt Ute: "
							+ String.format("%.2f", status.moistureUte));
					textFuktInne.setText("Fukt Inne: "
							+ String.format("%.2f", status.moistureInne));
					textFanOn.setText("Fan On =" + status.fanOn);

					//Is ioio chip initialized etc
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
					//debugText.setText(status.)
					
					if (!debugButton.isChecked()) {

						seekTempUte
								.setProgress((int) status.temperatureUte + 20);
						seekTempInne
								.setProgress((int) status.temperatureInne + 20);
						seekFuktInne.setProgress((int) status.moistureInne);
						seekFuktUte.setProgress((int) status.moistureUte);
					}

				}
			});
		}
	}



}
