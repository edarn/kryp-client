package se.tna.krypgrund;

import ioio.lib.api.exception.ConnectionLostException;
import java.util.ArrayList;
import java.util.HashMap;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DebuggerActivity extends Activity{
	
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
	private ToggleButton toggleButton_;
	private ToggleButton debugButton;
	private ToggleButton forceSendDataButton;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		textView_ = (TextView)findViewById(R.id.TextView);
		seekBar_ = (SeekBar)findViewById(R.id.SeekBar);
		seekFuktInne =  (SeekBar)findViewById(R.id.seekFuktInne);
		seekFuktUte =  (SeekBar)findViewById(R.id.seekFuktUte);
		seekTempInne =  (SeekBar)findViewById(R.id.seekTempInne);
		seekTempUte =  (SeekBar)findViewById(R.id.seekTempUte);

		textFuktInne = (TextView)findViewById(R.id.textFuktInne);
		textFuktUte = (TextView)findViewById(R.id.textFuktUte);
		textTempInne = (TextView)findViewById(R.id.textTempInne);
		textTempUte = (TextView)findViewById(R.id.textTempUte);

		seekBar_.setMax(100);
		seekFuktInne.setMax(100);
		seekFuktUte.setMax(100);
		seekTempInne.setMax(60);
		seekTempUte.setMax(60);



		toggleButton_ = (ToggleButton)findViewById(R.id.ToggleButton);
		debugButton = (ToggleButton)findViewById(R.id.DebugButton1);
		forceSendDataButton = (ToggleButton)findViewById(R.id.ForceSendDataButton);
		try {
			setup();
			Thread a = new Thread()
			{
				public void run()
				{
					for (int i = 0; i< 1000; i++)
					{
						try {
							loop();
						} catch (ConnectionLostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
							
			};
			a.start();
			
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//enableUi(false);
	}

	private Helper helper;

	public void setup() throws ConnectionLostException {
		//try {
		//if (!debug)
		{

			//helper = new Helper(null);
			//helper.TurnOnBacklight();
			//helper.SetupGpioChip();
			/*input_ = ioio_.openAnalogInput(40);
				pwmOutput_ = ioio_.openPwmOutput(12, 100);
				led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);*/
		} 
		//enableUi(true);
		//Helper.WriteText(i2c, "test");

		maximalFukt.put(-15, (float) 1);
		maximalFukt.put(-10, 2f);
		maximalFukt.put(-5, 3f);
		maximalFukt.put(-2, 4f);

		maximalFukt.put(0, 5f);
		maximalFukt.put(3,6f);
		maximalFukt.put(5, 7f);
		maximalFukt.put(8, 8f);

		maximalFukt.put(10, 9f);
		maximalFukt.put(11, 9.5f);
		maximalFukt.put(12, 10f);
		maximalFukt.put(13, 11f);
		maximalFukt.put(14, 12f);

		maximalFukt.put(15, 13f);
		maximalFukt.put(16, 14f);
		maximalFukt.put(17, 15f);
		maximalFukt.put(18, 16f);
		maximalFukt.put(19, 17f);
		maximalFukt.put(20, 18f);
		maximalFukt.put(21, 18.5f);
		maximalFukt.put(22, 19f);
		maximalFukt.put(23, 20f);
		maximalFukt.put(24, 21f);
		maximalFukt.put(25, 22f);
		maximalFukt.put(26, 22.5f);
		maximalFukt.put(27, 23f);
		maximalFukt.put(28, 24f);
		maximalFukt.put(29, 25f);
		maximalFukt.put(30, 26f);
		/*

		} catch (ConnectionLostException e) {
			enableUi(false);
			helper.Destroy();
			throw e;
		}*/
	}
	boolean clockwise = true;
	@SuppressLint("UseSparseArrays")
	HashMap<Integer,Float> maximalFukt = new HashMap<Integer, Float>();
	ArrayList<Stats> history = new ArrayList<Stats>();
	Stats data;
	int delay = 0;
	final int TIME_BETWEEN_ON_OFF = 1;//60*5;

	public void loop() throws ConnectionLostException {
		try {
			data = new Stats();
			{
				data.temperatureUte = seekTempUte.getProgress()-20;
				data.temperatureInne = seekTempInne.getProgress()-20;
				data.moistureInne = seekFuktInne.getProgress();
				data.moistureUte = seekFuktUte.getProgress();
			}	
			
			setText(data);

			/*final float reading = input_.read();
			pwmOutput_.setPulseWidth(500 + seekBar_.getProgress() * 2);
			led_.write(!toggleButton_.isChecked());
			 */
			//helper.ClearScreen();

			for (int i = (int) data.temperatureInne; i< 40; i++){ 
				if (maximalFukt.containsKey(i)){
					data.absolutFuktInne = maximalFukt.get(i);
					//f�rb�ttra!!
					break;
				}
			}

			for (int i = (int) data.temperatureUte; i< 40; i++){ 
				if (maximalFukt.containsKey(i)){
					data.absolutFuktUte = maximalFukt.get(i);
					break;
				}
			}

			data.absolutFuktInne = data.moistureInne * data.absolutFuktInne;
			data.absolutFuktUte = data.moistureUte * data.absolutFuktUte;
			setStatusText("AInne: " + data.absolutFuktInne+" AUte: "+ data.absolutFuktUte);

			if (delay>0) delay--;
			ControlFan();
			data.fanOn =helper.IsFanOn();
			history.add(data);
			Thread.sleep(1000); 
			
		///	String res= helper.SendDataToServer(history,forceSendDataButton);
		///	setStatusText(res);
			
			
			//helper.Destroy();
			//ioio_.disconnect();
		}catch(Exception e){

		}
	}
	public void ControlFan() {
		if (data.temperatureInne < 0.9) {
			helper.ControlFan(Helper.FanStop, true);
		}
		else{
			if (data.absolutFuktUte*1.10 < data.absolutFuktInne && delay == 0 && data.temperatureInne > 1) { //Start the fans!!
				delay = TIME_BETWEEN_ON_OFF;
				float speed = (float)(Helper.FanMaxSpeed*seekBar_.getProgress()/99);
				helper.ControlFan((int) speed,clockwise );
			}
			else if (data.absolutFuktUte*1.05 > data.absolutFuktInne && delay == 0) {
				delay = TIME_BETWEEN_ON_OFF;
				helper.ControlFan(Helper.FanStop, true);
			}
		}
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
				textFuktUte.setText("Fukt Ute: " + data.moistureUte);
				textFuktInne.setText("Fukt Inne: " + data.moistureInne);
				if(!debugButton.isChecked()){

					seekTempUte.setProgress((int) data.temperatureUte+20);
					seekTempInne.setProgress((int) data.temperatureInne+20);
					seekFuktInne.setProgress((int) data.moistureInne);
					seekFuktUte.setProgress((int) data.moistureUte);
				}

			}
		});
	}
	private void setStatusText(final String text ) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView_.setText(text);

			}
		});
	}


}
