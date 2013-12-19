package com.example.ioiotest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.CapSense;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.DigitalInput.Spec;
import ioio.lib.api.DigitalInput.Spec.Mode;
import ioio.lib.api.PulseInput;
import ioio.lib.api.PulseInput.ClockRate;
import ioio.lib.api.PulseInput.PulseMode;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * This is the main activity of the HelloIOIO example application.
 * 
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link IOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 */
public class MainActivity extends IOIOActivity {
	private ToggleButton button_;
	private TextView text;

	public float freq = 1;

	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		button_ = (ToggleButton) findViewById(R.id.button);
		text = (TextView) findViewById(R.id.text);

		Timer tt = new Timer();
		TimerTask t = new TimerTask() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						text.setText(String.valueOf(freq));
					}
				});
			}
		};

		tt.schedule(t, 1000, 100);
	}

	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {
		/** The on-board LED. */
		private DigitalOutput led_;
		private AnalogInput anemometer;
		private PulseInput pulseCounter;
		private PulseInput pulseCounterFreqScale4;
		private PulseInput pulseCounterFreqScale16;

		private int mNbrReadings = 0;
		private CapSense capSense;
		private PwmOutput pwm;
		private PwmOutput pwmTest;
		private PwmOutput pwm3;

		public void appendLog(String text) {
			System.out.println(text);
			File logFile = new File("sdcard/thomas_log.file");
			if (!logFile.exists()) {
				try {
					logFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				// BufferedWriter for performance, true to set append to file
				// flag
				BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
				buf.append(text);
				buf.newLine();
				buf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {

			led_ = ioio_.openDigitalOutput(0, true);
			// anemometer = ioio_.openAnalogInput(40);
			// pwm = ioio_.openPwmOutput(40, 8000);
			// pwm.setDutyCycle(0.7f);
			pwmTest = ioio_.openPwmOutput(34, setFreq);
			pwmTest.setDutyCycle(dutyCycle);
			// pwm3 = ioio_.openPwmOutput(39, 8000);
			// pwm3.setDutyCycle(0.5f);

			// Spec spec = new Spec(35);
			// spec.mode = Mode.PULL_UP;
			// pulseCounter = ioio_.openPulseInput(spec, ClockRate.RATE_2MHz,
			// PulseMode.FREQ, true);
			// spec = new Spec(37);
			// spec.mode = Mode.PULL_UP;
			// pulseCounterFreqScale4 = ioio_.openPulseInput(spec,
			// ClockRate.RATE_2MHz, PulseMode.FREQ_SCALE_4, true);
			// spec = new Spec(39);
			// spec.mode = Mode.PULL_UP;
			// pulseCounterFreqScale16 = ioio_.openPulseInput(spec,
			// ClockRate.RATE_2MHz, PulseMode.FREQ_SCALE_16, true);

			// capSense = ioio_.openCapSense(39,200);

		}

		private void setPulse(int freq, float duty) throws ConnectionLostException {
			pwmTest.close();
			pwmTest = ioio_.openPwmOutput(34, freq);
			pwmTest.setDutyCycle(duty);
		}

		boolean on = false;
		Random r = new Random();

		/*
		 * private void toggle() throws ConnectionLostException {
		 * anemometer.close(); anemometer = ioio_.openAnalogInput(40);
		 * setPulse(r.nextInt(100));
		 * 
		 * }
		 */

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		int i = 0;
		int setFreq = 2;
		float dutyCycle = 1f;
		float oldDutyCycle = dutyCycle;

		public float getWindSpeed() throws ConnectionLostException {
			float speedMeterPerSecond = 0;

			try {
				dutyCycle -= 0.001;
				if (dutyCycle <= 0.01) {
					dutyCycle = 0.999f;
					setFreq++;
					setPulse(setFreq, dutyCycle);
					return 0;
				}

				setPulse(setFreq, dutyCycle);
				Thread.sleep(100);
				pulseCounter = ioio_.openPulseInput(35, PulseMode.FREQ);

				// pulseCounter = ioio.openPulseInput(spec,
				// ClockRate.RATE_16MHz, PulseMode.FREQ, true);
				Thread.sleep(200);
				float duration = pulseCounter.getDuration();
				// float duration = pulseCounter.getDuration();

				freq = 1 / duration;
				// freq = pulseCounter.getFrequency();
				pulseCounter.close();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				
			}

			appendLog("WindSpeed: " + freq + " Hz. Dutycycle: " + dutyCycle + " freq " + setFreq);
			speedMeterPerSecond = freq * 1.006f;
			return speedMeterPerSecond;
		}

		@Override
		public void loop() throws ConnectionLostException {
			led_.write(!button_.isChecked());

			try {
				getWindSpeed();
				// Test_FREQ_SCALING();

				// freq = pulseCounter.getFrequency();
				// i++;
				// System.out.print(String.format("%.2f", freq) + " ");
				// if (freq > 15 || Math.abs((float) (freq - setFreq)) > 0.2) {
				// mNbrReadings++;
				// appendLog("\n" + i + "LargeValue! setFreq= " + setFreq +
				// " readFreq: " + String.format("%.2f", freq) + " Hz");
				// runOnUiThread(new Runnable() {
				//
				// @Override
				// public void run() {
				// text.setText("set:" + setFreq + " read: " +
				// String.format("%.2f", freq));
				//
				// }
				// });
				// }
				// if (i % 20 == 0)
				// {
				// pwmTest.setDutyCycle(0);
				// }
				// else if (i % 10 == 0) {
				// oldDutyCycle = dutyCycle;
				// dutyCycle = 0.01f + r.nextInt(20) / 100f;
				// appendLog(i + " Changing dutycycle. OldValue = " +
				// String.format("%.2f", oldDutyCycle) + " newDutyCycle = " +
				// String.format("%.2f", dutyCycle));
				// pwmTest.setDutyCycle(dutyCycle);
				// //Sleep once cycle to make sure change is done!
				// Thread.sleep((long) (1/freq*1000));
				// }
				//
				//
				//
				// if (i % 3000 == 0) {
				// setFreq += 1;
				// setPulse(setFreq);
				// freq = pulseCounter.getFrequency();
				// appendLog(i + " Changing setFreq= " + setFreq + " readFreq: "
				// + String.format("%.2f", freq) + " Hz");
				// }
				Thread.sleep(100);

			} catch (InterruptedException e) {
			}
		}

		private void Test_FREQ_SCALING() {
			try {

				float freq_one_pulse = pulseCounter.getFrequency();

				float freq_4_pulses = pulseCounterFreqScale4.getFrequency();
				float freq_16_pulses = pulseCounterFreqScale16.getFrequency();

				if (Math.abs(freq_one_pulse - freq_4_pulses) > freq_one_pulse * 0.1 || Math.abs(freq_16_pulses - freq_4_pulses) > freq_one_pulse * 0.1
						|| Math.abs(freq_16_pulses - freq_4_pulses) > freq_4_pulses * 0.1) // Error
																							// if
																							// diff
																							// is
																							// >
																							// 10%
				{
					appendLog("FREQ_SCALING_ERROR");
				}
				appendLog("                                                    " + "  1: " + String.format("%.2f", freq_one_pulse) + "  4: " + String.format("%.2f", freq_4_pulses)
						+ "  16: " + String.format("%.2f", freq_16_pulses));
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ConnectionLostException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

}

/*
 * public float getWindSpeed2() throws ConnectionLostException { float
 * speedMeterPerSecond = 0; float freq = 0; try { Thread.sleep(200); freq =
 * pulseCounter.getFrequency(); } catch (InterruptedException e) {
 * e.printStackTrace(); } System.out.println("WindSpeed: " + freq + " Hz");
 * appendLog("WindSpeed: " + freq + " Hz"); if (freq < 0.5 || freq > 60) freq =
 * 0; speedMeterPerSecond = freq * 1.006f; return speedMeterPerSecond; }
 * 
 * public float getWindDirection2() throws ConnectionLostException { float
 * direction = 0; try { Thread.sleep(200);
 * 
 * float voltage = anemometer.getVoltage(); System.out.println("Volt: " +
 * voltage + " Rate: " + anemometer.getSampleRate()); appendLog("Volt: " +
 * voltage); voltage *= 360 / 3.3f; direction = voltage; } catch
 * (InterruptedException e) { e.printStackTrace(); } return direction; }
 * 
 * public static final int FREQ = 0; public static final int ANALOG = 1;
 * 
 * float result = 0;
 * 
 * public synchronized float queryIOIO(final int command) { result = -1; Thread
 * commandExecutor = new Thread(new Runnable() {
 * 
 * @Override public void run() { try { switch (command) { case FREQ: result =
 * getWindSpeed2(); break;
 * 
 * case ANALOG: result = getWindDirection2(); break; } } catch (Exception e) {
 * Log.e("Helper", "An IOIO command failed: Command = " + command);
 * e.printStackTrace();
 * 
 * } } }); commandExecutor.start(); try { // Give command 4 seconds for command
 * to finish commandExecutor.join(4000); if (commandExecutor.isAlive()) {
 * commandExecutor.interrupt(); commandExecutor = null;
 * 
 * return 0; } else { commandExecutor = null; }
 * 
 * } catch (InterruptedException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); } return result; }
 */
