package se.tna.krypgrund;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DebuggerActivity extends Activity {

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

		textView_ = (TextView) findViewById(R.id.TextView);
	//	seekBar_ = (SeekBar) findViewById(R.id.SeekBar);
		seekFuktInne = (SeekBar) findViewById(R.id.seekFuktInne);
		seekFuktUte = (SeekBar) findViewById(R.id.seekFuktUte);
		seekTempInne = (SeekBar) findViewById(R.id.seekTempInne);
		seekTempUte = (SeekBar) findViewById(R.id.seekTempUte);

		textFuktInne = (TextView) findViewById(R.id.textFuktInne);
		textFuktUte = (TextView) findViewById(R.id.textFuktUte);
		textTempInne = (TextView) findViewById(R.id.textTempInne);
		textTempUte = (TextView) findViewById(R.id.textTempUte);

		seekBar_.setMax(100);
		seekFuktInne.setMax(100);
		seekFuktUte.setMax(100);
		seekTempInne.setMax(60);
		seekTempUte.setMax(60);

		toggleButton_ = (ToggleButton) findViewById(R.id.ToggleButton);
		debugButton = (ToggleButton) findViewById(R.id.DebugButton1);
		forceSendDataButton = (ToggleButton) findViewById(R.id.ForceSendDataButton);
		Thread a = new Thread() {
			public void run() {
				for (int i = 0; i < 1000000; i++) {
					loop();
				}
			}

		};
		a.start();

		// enableUi(false);
	}

	private Helper helper = new Helper(null,null);

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

	private void setText(final KrypgrundStats data) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// textView_.setText(str);
				textTempUte.setText("Temp Ute: "
						+ String.format("%.2f", data.temperatureUte));
				textTempInne.setText("Temp Inne: "
						+ String.format("%.2f", data.temperatureInne));
				textFuktUte.setText("Fukt Ute: " + data.moistureUte);
				textFuktInne.setText("Fukt Inne: " + data.moistureInne);
				if (!debugButton.isChecked()) {

					seekTempUte.setProgress((int) data.temperatureUte + 20);
					seekTempInne.setProgress((int) data.temperatureInne + 20);
					seekFuktInne.setProgress((int) data.moistureInne);
					seekFuktUte.setProgress((int) data.moistureUte);
				}

			}
		});
	}

	private void setStatusText(final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView_.setText(text);

			}
		});
	}

	public KrypgrundStats data;
	int delay = 0;
	int historyDelay = 0;
	final int TIME_BETWEEN_ON_OFF = 2 * 5; // 5 minutes - each itteration is 30
											// sec.

	private boolean forceSendData = false;
	ArrayList<KrypgrundStats> measurements = new ArrayList<KrypgrundStats>();
	public String debugText = "";
	public String statusText = "";
	boolean isInitialized = false;
	ArrayList<KrypgrundStats> history = new ArrayList<KrypgrundStats>();
	private String id = "123456789";

	public void loop() {
		KrypgrundStats temp = new KrypgrundStats();

		temp.moistureInne = seekTempInne.getProgress();
		temp.moistureUte = seekTempUte.getProgress();
		temp.temperatureInne = seekFuktInne.getProgress();
		temp.temperatureUte = seekFuktUte.getProgress();

		measurements.add(temp);

		historyDelay++;
		if (historyDelay > 5) {

			KrypgrundStats total = new KrypgrundStats();

			for (KrypgrundStats stat : measurements) {
				total.moistureInne += stat.moistureInne;
				total.moistureUte += stat.moistureUte;
				total.temperatureInne += stat.temperatureInne;
				total.temperatureUte += stat.temperatureUte;
			}
			total.moistureInne /= (float) measurements.size();
			total.moistureUte /= (float) measurements.size();
			total.temperatureInne /= (float) measurements.size();
			total.temperatureUte /= (float) measurements.size();

			total.absolutFuktUte = (float) (4.632248129 * (Math
					.expm1(0.06321315927 * total.temperatureUte) + 1));
			total.absolutFuktInne = (float) (4.632248129 * (Math
					.expm1(0.06321315927 * total.temperatureInne) + 1));

			total.absolutFuktUte = total.moistureUte * total.absolutFuktUte;
			total.absolutFuktInne = total.moistureInne * total.absolutFuktInne;
			statusText = "AInne: "
					+ String.format("%.2f", total.absolutFuktInne) + " AUte: "
					+ String.format("%.2f", total.absolutFuktUte);

			total.fanOn = helper.IsFanOn();
			history.add(total);
			historyDelay = 0;
			//String res = helper.SendDataToServer(history, forceSendData, id);

			measurements = new ArrayList<KrypgrundStats>();
			//debugText = res;
		} else {
			String text = "HSize=" + history.size() + " Succ=";
				//	+ Boolean.toString(helper.GetSendSuccess()) + " fDelay:"
				//	+ Integer.toString(helper.GetFailureDelay());
			debugText = text;
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
