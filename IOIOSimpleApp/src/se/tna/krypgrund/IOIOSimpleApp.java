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

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class IOIOSimpleApp extends Activity {
	private TextView textView_;
	private SeekBar seekBar_;
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
	
	private ToggleButton toggleButton_;
	private ToggleButton debugButton;
	private ToggleButton forceSendDataButton;
	private ToggleButton toggleFanButton; 

	private KrypgrundsService kryp = null;
	private ServiceConnection mConnection =null;

	@Override
	public void onPause() {
	
		super.onPause();
		mConnection=null;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mConnection = new ServiceConnection(){

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				kryp = ((KrypgrundsService.MyBinder) service).getService();
				Toast.makeText(IOIOSimpleApp.this, "Connected", Toast.LENGTH_SHORT).show();
			
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				kryp = null;
				
			}
		};
		
		setContentView(R.layout.main);

		textView_ = (TextView)findViewById(R.id.TextView);
		
		debugText = (TextView)findViewById(R.id.debugText);
		seekBar_ = (SeekBar)findViewById(R.id.SeekBar);
		seekFuktInne =  (SeekBar)findViewById(R.id.seekFuktInne);
		seekFuktUte =  (SeekBar)findViewById(R.id.seekFuktUte);
		seekTempInne =  (SeekBar)findViewById(R.id.seekTempInne);
		seekTempUte =  (SeekBar)findViewById(R.id.seekTempUte);

		textFuktInne = (TextView)findViewById(R.id.textFuktInne);
		textFuktUte = (TextView)findViewById(R.id.textFuktUte);
		textTempInne = (TextView)findViewById(R.id.textTempInne);
		textTempUte = (TextView)findViewById(R.id.textTempUte);
		initializedText = (TextView)findViewById(R.id.connectedText);
		seekBar_.setMax(100);
		seekFuktInne.setMax(100);
		seekFuktUte.setMax(100);
		seekTempInne.setMax(60);
		seekTempUte.setMax(60);
		toggleButton_ = (ToggleButton)findViewById(R.id.ToggleButton);
		toggleFanButton = (ToggleButton)findViewById(R.id.toggleFanButton);
		debugButton = (ToggleButton)findViewById(R.id.DebugButton1);
		forceSendDataButton = (ToggleButton)findViewById(R.id.ForceSendDataButton);

		enableUi(false);
		
		Intent service = new Intent(this, KrypgrundsService.class);
		this.startService(service); 
		
		bindService(new Intent(this, KrypgrundsService.class),mConnection, Context.BIND_AUTO_CREATE);
		
		
		Timer timer = new Timer();
	
		  	
	
		TimerTask t = new TimerTask()
		{

			@Override
			public void run() {
				// TODO Auto-generated method stub
				 if (null != kryp && null != kryp.data)
		  	 {
					 setText(kryp.data);
					 setStatusText(kryp.statusText, kryp.isInitialized);
					 setDebugText(kryp.debugText);
					 kryp.setDebugMode(debugButton.isChecked());
					 enableUi(debugButton.isChecked());
					 if (debugButton.isChecked())
					 {
						
						 Stats debugData = null;
						//Must this be run on the UI thread?
						 debugData = new Stats();
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
				seekBar_.setEnabled(enable);
				toggleButton_.setEnabled(enable);
				seekTempUte.setEnabled(enable);
				seekTempInne.setEnabled(enable);
				seekFuktInne.setEnabled(enable);
				seekFuktUte.setEnabled(enable);

			}
		});
	}

	private void setText(final Stats data) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//textView_.setText(str);
				textTempUte.setText("Temp Ute: " + String.format("%.2f",data.temperatureUte));
				textTempInne.setText("Temp Inne: " + String.format("%.2f",data.temperatureInne));
				textFuktUte.setText("Fukt Ute: " + String.format("%.2f",data.moistureUte));
				textFuktInne.setText("Fukt Inne: " + String.format("%.2f",data.moistureInne));
				if(!debugButton.isChecked()){

					seekTempUte.setProgress((int) data.temperatureUte+20);
					seekTempInne.setProgress((int) data.temperatureInne+20);
					seekFuktInne.setProgress((int) data.moistureInne);
					seekFuktUte.setProgress((int) data.moistureUte);
				}

			}
		});
	}
	private void setStatusText(final String text, final boolean initialized ) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView_.setText(text);
				initializedText.setText("IoIo is initialized = " + initialized);

			}
		});
	}
	private void setDebugText(final String text ) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				debugText.setText(text);

			}
		});
	}



}
