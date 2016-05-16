package se.tna.commonloggerservice;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.CapSense;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalInput.Spec;
import ioio.lib.api.DigitalInput.Spec.Mode;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PulseInput;
import ioio.lib.api.PulseInput.ClockRate;
import ioio.lib.api.PulseInput.PulseMode;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import se.tna.commonloggerservice.KrypgrundsService.ServiceMode;

public class Helper {
    private static Tracker mainTracker;
    private boolean watchValue=true;
    IOIO ioio = null;

	DigitalOutput B1 = null;
	DigitalOutput B2 = null;
    DigitalOutput WatchDog = null;
	PwmOutput BSpeed = null;

	CapSense humidityInside = null;
	CapSense humidityOutside = null;

	DigitalOutput Standby = null;
	TwiMaster i2cInne = null;
    TwiMaster i2cUte = null;
    TwiMaster i2cOnBoard = null;

    KrypgrundsService krypService = null;

	PulseInput pulseCounter = null;
	AnalogInput anemometer = null;
	private AnalogInput power;
	private AnalogInput mAnalogPulsecounter;

	private static final int ANEMOMETER_WIND_VANE = 41;
	private static final int ANEMOMETER_SPEED = 40;

	private static final int chipCap2Adress = 0x50;
	private static String imei = "123456789";
	private static String version = "NotSet";

	private int pcbVersion = 0;
	public final int FIRST_VERSION = 7;
	public final int VERSION_1_4_WITHOUT_HUMIDITY = 6;
	public final int VERSION_1_4_INCLUDING_HUMIDITY = 5;


	private enum FrequencyReading {
		Continuos_Reading, OpenClose_Reading, Analogue_Reading
	};

	public enum SensorLocation {
		SensorInne, SensorUte, SensorOnBoard;
	}

	public class ChipCap2 {
		float humidity = 0;
		float temperature = 0;
		boolean okReading = true;
	}

	private static final FrequencyReading GET_SPEED_VERSION = FrequencyReading.Continuos_Reading;
	private static Context mCtx = null;

	public Helper(IOIO _ioio, KrypgrundsService kryp, String id, String ver, ServiceMode mode) {
		ioio = _ioio;
		krypService = kryp;
		imei = id;
		version = ver;
		mCtx = kryp.getApplicationContext();
		if (ioio != null) {
			try {
				pcbVersion = 0;
				DigitalInput i1 = ioio.openDigitalInput(new Spec(13, Mode.PULL_UP));
				DigitalInput i2 = ioio.openDigitalInput(new Spec(14, Mode.PULL_UP));
				DigitalInput i3 = ioio.openDigitalInput(new Spec(15, Mode.PULL_UP));
				if (i1.read()) {
					pcbVersion += 4;
				}
				if (i2.read()) {
					pcbVersion += 2;
				}
				if (i3.read()) {
					pcbVersion += 1;
				}

				// if (mode == ServiceMode.Survfind) {
				anemometer = ioio.openAnalogInput(ANEMOMETER_WIND_VANE);
				if (GET_SPEED_VERSION == FrequencyReading.Analogue_Reading) {
					mAnalogPulsecounter = ioio.openAnalogInput(ANEMOMETER_SPEED);
				} else if (GET_SPEED_VERSION == FrequencyReading.Continuos_Reading) {
					Spec spec = new Spec(ANEMOMETER_SPEED);
					spec.mode = Mode.PULL_UP;
					pulseCounter = ioio.openPulseInput(spec, ClockRate.RATE_16MHz, PulseMode.FREQ, true);
				} else if (GET_SPEED_VERSION == FrequencyReading.OpenClose_Reading) {
					// Do nothing as open and close will be done at every
					// call.
				}
				// } else if (mode == ServiceMode.Krypgrund) {
				i2cInne = ioio.openTwiMaster(0, TwiMaster.Rate.RATE_100KHz, false);
				i2cUte = ioio.openTwiMaster(1, TwiMaster.Rate.RATE_100KHz, false);
                i2cOnBoard = ioio.openTwiMaster(2, TwiMaster.Rate.RATE_100KHz, false);
				// }

				// On board sensors. Are they used?
				power = ioio.openAnalogInput(42);

                WatchDog = ioio.openDigitalOutput(9,DigitalOutput.Spec.Mode.OPEN_DRAIN, watchValue);
				B2 = ioio.openDigitalOutput(20);
				B1 = ioio.openDigitalOutput(19);
                WatchDog.write(watchValue);
				B1.write(mFanOn);
				B2.write(mFanOn);

			} catch (Exception e) {
				e.printStackTrace();
			} // USE FALSE for I2C otherwise to high voltage!!!
		}
	}

	public void Destroy() {

		if (i2cInne != null)
			i2cInne.close();
        if (i2cUte != null)
            i2cUte.close();
        if (i2cOnBoard != null)
            i2cOnBoard.close();
        if (anemometer != null)
			anemometer.close();
		if (pulseCounter != null)
			pulseCounter.close();
		B1.close();
		B2.close();
        WatchDog.close();
		power.close();
	}
	public enum PcbVersion{
		OldVersion,
		Verison_1_4_Including_Humidity,
		Version_1_4_Withouth_Humidity
	}
	public PcbVersion getPcbVersion()
	{
		PcbVersion ver = PcbVersion.OldVersion;
		if (pcbVersion == VERSION_1_4_INCLUDING_HUMIDITY)
		{
			ver = PcbVersion.Verison_1_4_Including_Humidity;
		}else if (pcbVersion == VERSION_1_4_WITHOUT_HUMIDITY)
		{
			ver = PcbVersion.Version_1_4_Withouth_Humidity;
		}
		return  ver;
	}
	public static void setupGoogleAnalytics(Activity activity/*, String id */){
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(activity);
		mainTracker = analytics.newTracker(
                R.xml.global_tracker);
		mainTracker.enableAdvertisingIdCollection(true);
		mainTracker.enableExceptionReporting(true);
	}



	public static void trackScreenName(String name)
	{
		if (mainTracker != null) {
			mainTracker.setScreenName(name);
			mainTracker.send(new HitBuilders.AppViewBuilder().build());
		}
	}
	public static void trackEvent(String category, String action, String label, Long value)
	{
		if (mainTracker != null) {
			mainTracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).setValue(value).build());

		}
	}



	static File logFile = null;
	static BufferedWriter bufWriter;

    public void toggleWatchdog() throws ConnectionLostException
    {
        WatchDog.write(watchValue);
        watchValue=!watchValue;
    }

	public static void appendLog(String text) {
		System.out.println(text);
		// if (mCtx != null) {
/*
		try {
			if (logFile == null) {
				// logFile = new File(mCtx.getFilesDir() +
				// "/krypgrund_log.file");
				// Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
				// +
				logFile = new File(Environment.getExternalStorageDirectory().getPath() +"/krypgrund_log.txt");
				if (!logFile.exists()) {
					logFile.createNewFile();

				}
			}
			if (bufWriter == null) {
				// BufferedWriter for performance, true to set append to
				// file
				// flag
				bufWriter = new BufferedWriter(new FileWriter(logFile, true));
			}
			long timestamp = System.currentTimeMillis();
			CharSequence cs = DateFormat.format("yyyy-MM-dd - kk:mm:ss", timestamp);
			bufWriter.append(Long.toString(timestamp));
			bufWriter.append(" - ");
			bufWriter.append(cs.toString());

			bufWriter.append(text);
			bufWriter.newLine();
			bufWriter.flush();

		} catch (IOException e) {
			// e.printStackTrace();

			try {
				if (bufWriter != null) {
					bufWriter.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				// e1.printStackTrace();
			} finally {
				bufWriter = null;
				logFile = null;
			}
		}*/
	}

	private ChipCap2 GetChipCap2(SensorLocation type) {
		ChipCap2 result = new ChipCap2();

		byte toSend[] = new byte[1];
		byte toReceive[] = new byte[4];
		toSend[0] = (byte) 0;


		try {
			Thread.sleep(200);
			if (type == SensorLocation.SensorInne) {
				System.out.print("Inne: ");
				//i2cInne.writeRead(chipCap2Adress / 2, false, toSend, 1, toReceive, 0);
				//Thread.sleep(500);
				i2cInne.writeRead(chipCap2Adress / 2, false, toSend, 1, toReceive, 4);
			} else if (type == SensorLocation.SensorUte) {
				System.out.print("Ute: ");
				i2cUte.writeRead(chipCap2Adress / 2, false, toSend, 1, toReceive, 0);
				Thread.sleep(500);
				i2cUte.writeRead(chipCap2Adress / 2, false, toSend, 0, toReceive, 4);
			} else if (type == SensorLocation.SensorOnBoard) {
				System.out.print("OnBoard: ");
				i2cOnBoard.writeRead(chipCap2Adress / 2, false, toSend, 1, toReceive, 0);
				Thread.sleep(500);
				i2cOnBoard.writeRead(chipCap2Adress / 2, false, toSend, 0, toReceive, 4);
            }
			float status = (toReceive[0] &0xFF) >>> 6;

			float humid = ((toReceive[0] & 0x3F) << 8) + (toReceive[1] & 0xFF);
			humid /= 163.84;

			//double temp = (toReceive[2] & 0xFF) * 64 + ((toReceive[3] &0xFC) /4f) /4f;
			//double temp = ((toReceive[2] & 0xFF) << 6 | ((toReceive[3] &0xFC) >> 2)) & 0x3FFF;
			double temp = (((toReceive[2] & 0xFF) *64) + (((toReceive[3] &0xFC) / 4))) & 0x3FFF;

			temp /= 99.29;
			temp -= 40;
			temp -= 0.8; //Thomas own extra calibration as ChipCap seems to return aprox 0.8 degrees to high temperature.

            //if no sensor responds, make sure we don´t send -40.8 as response.
            if (temp == -40.8) temp = 0;

			result.humidity = humid;
			result.temperature = (float) temp;
			System.out.println("Humid: "+result.humidity +" temp: "+ result.temperature + " Status: " + status);
		} catch (ConnectionLostException e) {
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
            System.out.println("GetChipCap2 interrupted. Comm error.");
            return null;
		}

        return result;
	}

	public ChipCap2 GetChipCap2TempAndHumidity(final SensorLocation type) {
		final ChipCap2 tempAndHumidity = new ChipCap2();
		Thread commandExecutor = new Thread(new Runnable() {

			@Override
			public void run() {
				ChipCap2 temp = null;
				try {
					temp = GetChipCap2(type);

				} catch (Exception e) {
					Log.e("Helper", "An IOIO command failed: GetChipCap2");
					e.printStackTrace();

				}
				if (temp != null) {
					tempAndHumidity.humidity = temp.humidity;
					tempAndHumidity.temperature = temp.temperature;
					tempAndHumidity.okReading = true;
				}
			}
		});
		commandExecutor.start();
		try {
			// Give command 4 seconds for command to finish
			commandExecutor.join(4000);
			if (commandExecutor.isAlive()) {
				commandExecutor.interrupt();
				tempAndHumidity.humidity = 0;
				tempAndHumidity.temperature = 0;
				tempAndHumidity.okReading = false;
			}
			commandExecutor = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return tempAndHumidity;
	}

	private static boolean mFanOn = false;

	void ControlFan(boolean on) {

		try {
			if (on != mFanOn) {
				B1.write(on);
				B2.write(on);
				mFanOn = on;
			}
		} catch (ConnectionLostException e) {
			e.printStackTrace();
		}

	}

	public float getWindSpeed2() throws ConnectionLostException {
		float freq = 0;

		float speedMeterPerSecond = 0;
		try {
			// Thread.sleep(200);
			freq = pulseCounter.getFrequency();
			// pulseCounter.w
		} catch (InterruptedException e) {
            System.out.println("No wind detected!");
        }

		System.out.println("WindSpeed: " + String.format("%.2f", freq) + " Hz");
		if (freq < 0.5) {
			speedMeterPerSecond = 0;
		} else if (freq > 60) {
			speedMeterPerSecond = -1;
		} else {
			speedMeterPerSecond = freq * 1.006f;
		}
		return speedMeterPerSecond;
	}

	public static final int FREQ = 0;
	public static final int ANALOG = 1;

	private float result = 0;

	public synchronized float queryIOIO(final int command) {
		result = -1;
		Thread commandExecutor = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					switch (command) {
					case FREQ:
						switch (GET_SPEED_VERSION) {
						case Continuos_Reading:
							result = getWindSpeed2();
							break;
						case OpenClose_Reading:
							result = getWindSpeed();
							break;
						case Analogue_Reading:
							result = getWindSpeed3();
							break;
						}
						break;

					case ANALOG:
						result = getWindDirection2();
						break;
					}
				} catch (Exception e) {
					Log.e("Helper", "An IOIO command failed: Command = " + command);
					e.printStackTrace();

				}
			}
		});
		commandExecutor.start();
		try {
			// Give command 4 seconds for command to finish
			commandExecutor.join(4000);
			if (commandExecutor.isAlive()) {
				commandExecutor.interrupt();

			}
			commandExecutor = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}

	public float getWindDirection2() throws ConnectionLostException {
		float direction = 0;
		try {
			// Thread.sleep(200);

			float voltage = anemometer.getVoltage();
			System.out.println("Volt: " + voltage + " Rate: " + anemometer.getSampleRate());
			voltage *= 360 / 3.3f;
			direction = voltage;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return direction;
	}

	public float getBatteryVoltage() throws ConnectionLostException {
		float voltage = 0;
		try {

			voltage = power.getVoltage();

			voltage += voltage / 4700 * 24000 + 0.7; //0.7 is voltage drop over diode.
			System.out.println("VBatt: " + voltage);
			// voltage *= 100;

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return voltage;
	}

	public boolean IsFanOn() {
		return mFanOn;
	}

	/**
	 * Sends the measurements to the webserver. If there are many measurements
	 * this functions will send it as multiple requests.
	 *
	 * @param measurements
	 *            The measurements to send.
	 * @param mode
	 *            Which server to send to.
	 * @return A readable status line.
	 */
	public static String SendDataToServer(ArrayList<?> measurements, ServiceMode mode) {
		StringBuilder sb = new StringBuilder();
		sb.append("Trying to send ");
		sb.append(measurements.size());
		sb.append(" items. \n");
		DefaultHttpClient client = null;
		JSONObject data;
		String body ="";
		boolean SendSuccess = true;

		try {
			/*
			 * Keep sending data until there is a send failure or the history is
			 * empty.
			 */
			while (SendSuccess && measurements.size() > 0) {
				data = new JSONObject();

				client = new DefaultHttpClient();


				JSONArray dataArray = new JSONArray();

				int nbrOfItemsToSend = Math.min(measurements.size(), 50);
				/* Create a JSON Array that contains the data. */
				// Dont send more than 50 measures in one post.
				for (int i = 0; i < nbrOfItemsToSend; i++) {
					Stats temp = (Stats) measurements.get(i);
					if (temp != null) {

						dataArray.put(temp.getJSON());
						System.out.println("Sending data: "+temp.getJSON());

					}
				}
                HttpPost message = new HttpPost();
				String postUrl = "";
				if (mode == ServiceMode.Krypgrund) {
					postUrl = "http://www.surfvind.se/Krypgrund.php";
					data.put("measure", dataArray);
					data.put("id", "\"" + imei + "\"");
					data.put("version", version);
					body = data.toString();
                    message.addHeader("content-type", "application/x-www-form-urlencoded");

                } else if (mode == ServiceMode.Survfind) {
					postUrl = "http://www.surfvind.se/RestService/RestService1.svc/"+imei+"/SendSurfvindMeasurements";
					SurfvindPacket packet = new SurfvindPacket();
					packet.id = imei;
					packet.version = version;
					packet.surfvindMeasurements = (SurfvindStats[]) measurements.toArray(new SurfvindStats[measurements.size()]);
					Gson g = new Gson();
                    body = g.toJson(packet);
                    message.addHeader("content-type", "application/json");

                }
                message.setURI(new URI(postUrl));

				message.setEntity(new StringEntity(body));
				System.out.print(data.toString());
				HttpResponse response = client.execute(message);
				if (response != null) {
					StatusLine line = response.getStatusLine();
					if (line != null) {
						if (line.getStatusCode() == HttpStatus.SC_OK) {
							// Delete the reading that are sent.
							measurements.subList(0, nbrOfItemsToSend).clear();
							sb.append(" Success");
							sb.append(EntityUtils.toString(response.getEntity()));
						} else {
							SendSuccess = false;
							sb.append("Fail: ");
							sb.append(line.getStatusCode());
							sb.append(EntityUtils.toString(response.getEntity()));
						}
					}
				}
			}
		} catch (RuntimeException runtime) {
			sb.append("RuntimeException" + runtime.toString());
		} catch (ClientProtocolException e) {
			sb.append("ClientProtocolException" + e.toString());
		} catch (IOException io) {
			sb.append("IOException" + io.toString());
		} catch (Exception e) {
			sb.append("Ex:" + e.toString());
		} finally {
			if (null != client) {
				ClientConnectionManager manager = client.getConnectionManager();
				if (manager != null) {
					manager.shutdown();
				}
			}
		}
		return sb.toString();
	}


	public float getWindSpeed3() throws ConnectionLostException {
		ArrayList<Boolean> values = new ArrayList<Boolean>();
		float speedMeterPerSecond = 0;
		final int NBR_READINGS_TO_ANALYZE = 2000;
		float freq = 0;
		values.clear();
		try {
			while (values.size() < NBR_READINGS_TO_ANALYZE) {
				for (int i = 0; i < mAnalogPulsecounter.available(); i++) {
					if (mAnalogPulsecounter.readBuffered() > 0.9) // HIGH
					{
						values.add(true);
					} else {
						values.add(false);
					}
				}
			}
			boolean currentValue = values.get(0);
			int i = 1;
			int startPulse = i;
			int endPulse = 0;
			for (; i < values.size(); i++) // Detect an edge.
			{
				if (currentValue != values.get(i)) {
					currentValue = values.get(i);
					break;
				}
			}
			int nbrPulses = 0;
			boolean status = false;
			for (; i < values.size(); i++) // Detect an edge.
			{
				if (currentValue != values.get(i)) {
					status = true;
				} else if (status) {
					nbrPulses++;
					endPulse = i; // Save the value for last known complete
									// pulse
					status = false;
				}
			}
			freq = NBR_READINGS_TO_ANALYZE - (endPulse - startPulse) / (float) (anemometer.getSampleRate() * nbrPulses);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("WindSpeed: " + freq + " Hz");
		if (freq < 0.5 || freq > 60)
			freq = 0;
		speedMeterPerSecond = freq * 1.006f;
		return speedMeterPerSecond;
	}

	static Thread a = null;

	public float getWindSpeed() throws ConnectionLostException {
		float speedMeterPerSecond = 0;
		float freq = 0;
		Spec spec = new Spec(ANEMOMETER_SPEED);
		spec.mode = Mode.PULL_UP;

		try {

			pulseCounter = ioio.openPulseInput(spec, ClockRate.RATE_62KHz, PulseMode.FREQ, true);
			Thread.sleep(500);
			float duration = pulseCounter.waitPulseGetDuration();
			freq = 1 / duration;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			pulseCounter.close();
		}

		System.out.println("WindSpeed: " + freq + " Hz");
		// if (freq < 0.5) {
		// speedMeterPerSecond = 0;
		// } else if (freq > 60) {
		// speedMeterPerSecond = -1;
		// } else {
		speedMeterPerSecond = freq * 1.006f;
		// }
		return speedMeterPerSecond;
	}

	byte receive[];

	public boolean SendI2CCommand(final int adress, int register, int data) {
		final byte toSend[] = new byte[2];
		receive = new byte[1];
		toSend[0] = (byte) register;
		toSend[1] = (byte) data;

		if (i2cInne == null) {
			krypService.isInitialized = false;
			return false;
		}
		Thread a = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if (i2cInne != null) {
						i2cInne.writeRead(adress / 2, false, toSend, 2, receive, 0);
					} else {
						Log.e("Helper", "I2C is null, no command sent!");
					}
				} catch (ConnectionLostException e) { // TODO Auto-generated
														// catch block
					e.printStackTrace();
				} catch (InterruptedException e) { // TODO Auto-generated catch
													// block
					e.printStackTrace();
				}
			}
		});
		a.start();

		try {
			a.join(5000);
			if (a.isAlive()) {
				a.interrupt();
				krypService.isInitialized = false;
				return false;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;

	}

	public int ReadI2CData(int adress, int register) {
		byte toSend[] = new byte[1];
		byte toReceive[] = new byte[1];
		toSend[0] = (byte) register;

		try {
			i2cInne.writeRead(adress / 2, false, toSend, 1, toReceive, 1);
		} catch (ConnectionLostException e) {
			e.printStackTrace();
			return -1;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}

		return (int) (toReceive[0] & 0xFF);
	}


}
