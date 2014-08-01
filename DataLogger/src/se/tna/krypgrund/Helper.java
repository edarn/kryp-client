package se.tna.krypgrund;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.CapSense;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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

import se.tna.krypgrund.KrypgrundsService.ServiceMode;
import android.util.Log;

public class Helper {
	IOIO ioio = null;

	DigitalOutput B1 = null;
	DigitalOutput B2 = null;
	PwmOutput BSpeed = null;

	CapSense humidityInside = null;
	CapSense humidityOutside = null;

	DigitalOutput Standby = null;
	TwiMaster i2cInne = null;
	TwiMaster i2cUte = null;

	KrypgrundsService krypService = null;

	PulseInput pulseCounter = null;
	AnalogInput anemometer = null;
	private AnalogInput power;
	private AnalogInput temp;
	private AnalogInput mAnalogPulsecounter;

	private static final int ANEMOMETER_WIND_VANE = 45;
	private static final int ANEMOMETER_SPEED = 46;

	private static final int chipCap2Adress = 0x50;
	private String imei = "123456789";
	private String version = "NotSet";

	private enum FrequencyReading {
		Continuos_Reading, OpenClose_Reading, Analogue_Reading
	};

	public enum SensorLocation {
		SensorInne, SensorUte;
	}

	public class ChipCap2 {
		float humidity = 0;
		float temperature = 0;
		boolean okReading = true;
	}

	private static final FrequencyReading GET_SPEED_VERSION = FrequencyReading.Continuos_Reading;

	public Helper(IOIO _ioio, KrypgrundsService kryp, String id, String ver,
			ServiceMode mode) {
		ioio = _ioio;
		krypService = kryp;
		imei = id;
		version = ver;
		if (ioio != null) {
			try {
				// if (mode == ServiceMode.Survfind) {
				anemometer = ioio.openAnalogInput(ANEMOMETER_WIND_VANE);
				if (GET_SPEED_VERSION == FrequencyReading.Analogue_Reading) {
					mAnalogPulsecounter = ioio
							.openAnalogInput(ANEMOMETER_SPEED);
				} else if (GET_SPEED_VERSION == FrequencyReading.Continuos_Reading) {
					Spec spec = new Spec(ANEMOMETER_SPEED);
					spec.mode = Mode.PULL_UP;
					pulseCounter = ioio.openPulseInput(spec,
							ClockRate.RATE_16MHz, PulseMode.FREQ, true);
				} else if (GET_SPEED_VERSION == FrequencyReading.OpenClose_Reading) {
					// Do nothing as open and close will be done at every
					// call.
				}
				// } else if (mode == ServiceMode.Krypgrund) {
				i2cInne = ioio.openTwiMaster(2, TwiMaster.Rate.RATE_100KHz,
						false);
				i2cUte = ioio.openTwiMaster(1, TwiMaster.Rate.RATE_100KHz,
						false);
				// }

				// On board sensors. Are they used?
				power = ioio.openAnalogInput(42);
				temp = ioio.openAnalogInput(43);

				B2 = ioio.openDigitalOutput(20);
				B1 = ioio.openDigitalOutput(19);
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
		if (anemometer != null)
			anemometer.close();
		if (pulseCounter != null)
			pulseCounter.close();
		B1.close();
		B2.close();
		power.close();
		temp.close();
	}

	static File logFile = null;
	static BufferedWriter bufWriter;

	public static void appendLog(String text) {
		System.out.println(text);
		try {
			if (logFile == null) {
				logFile = new File("sdcard/krypgrund_log.file");
				if (!logFile.exists()) {
					try {
						logFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			if (bufWriter == null) {
				// BufferedWriter for performance, true to set append to file
				// flag
				bufWriter = new BufferedWriter(new FileWriter(logFile, true));
			}

			bufWriter.append(text);
			bufWriter.newLine();
			bufWriter.flush();

		} catch (IOException e) {
			e.printStackTrace();

			try {
				if (bufWriter != null) {
					bufWriter.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				bufWriter = null;
				logFile = null;
			}
		}
	}

	private ChipCap2 GetChipCap2(SensorLocation type) {
		ChipCap2 result = new ChipCap2();

		byte toSend[] = new byte[1];
		byte toReceive[] = new byte[4];
		toSend[0] = (byte) 0;
		System.out.println("Humid: ++");

		try {
			Thread.sleep(200);
			if (type == SensorLocation.SensorInne) {
				i2cInne.writeRead(chipCap2Adress / 2, false, toSend, 1,
						toReceive, 4);
			} else if (type == SensorLocation.SensorUte) {
				i2cUte.writeRead(chipCap2Adress / 2, false, toSend, 1,
						toReceive, 4);
			}
		} catch (ConnectionLostException e) {
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}

		float humid = ((toReceive[0] & 0x3F) * 256 + (toReceive[1] & 0xFF));
		humid /= Math.pow(2, 14);
		humid *= 100;
		float temp = (toReceive[2] & 0xFF) * 64 + ((toReceive[3] >> 2) & 0x3F)
				/ 4;
		temp /= Math.pow(2, 14);
		temp *= 165;
		temp -= 40;

		result.humidity = humid;
		result.temperature = temp;
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
				tempAndHumidity.humidity = -100;
				tempAndHumidity.temperature = -100;
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
			e.printStackTrace();
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
					Log.e("Helper", "An IOIO command failed: Command = "
							+ command);
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

	public float getWindDirection() throws ConnectionLostException {
		float direction = 0;
		try {
			Thread.sleep(200);
			anemometer = ioio.openAnalogInput(ANEMOMETER_WIND_VANE);
			Thread.sleep(200);
			float voltage = anemometer.getVoltage();
			System.out.println("Volt: " + voltage + " Rate: "
					+ anemometer.getSampleRate());
			voltage *= 360 / 3.3f;
			direction = voltage;
		} catch (InterruptedException e) {
			System.out.println("------- GetWindDirection is interrupted----");
			e.printStackTrace();
			Log.e("Helper", "Now issuing a hard reset on IOIO");
			ioio.hardReset();
		} finally {
			System.out
					.println("======= GetWindDirection anemometer close.----");
			anemometer.close();
		}
		return direction;
	}

	public float getWindDirection2() throws ConnectionLostException {
		float direction = 0;
		try {
			// Thread.sleep(200);

			float voltage = anemometer.getVoltage();
			System.out.println("Volt: " + voltage + " Rate: "
					+ anemometer.getSampleRate());
			voltage *= 360 / 3.3f;
			direction = voltage;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return direction;
	}

	public int getBatteryVoltage() throws ConnectionLostException {
		float voltage = 0;
		try {

			voltage = power.getVoltage();

			voltage += voltage / 4400 * 24000;
			System.out.println("VBatt: " + voltage);
			voltage *= 100;

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return (int) voltage;
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
	public String SendDataToServer(ArrayList<?> measurements, ServiceMode mode) {
		StringBuilder sb = new StringBuilder();
		sb.append("Trying to send ");
		sb.append(measurements.size());
		sb.append(" items. \n");
		DefaultHttpClient client = null;
		JSONObject data;
		boolean SendSuccess = true;

		try {
			/*
			 * Keep sending data until there is a send failure or the history is
			 * empty.
			 */
			while (SendSuccess && measurements.size() > 0) {
				data = new JSONObject();

				client = new DefaultHttpClient();

				String postUrl = "";
				if (mode == ServiceMode.Krypgrund) {
					postUrl = "http://www.surfvind.se/Krypgrund.php";
				} else if (mode == ServiceMode.Survfind) {
					postUrl = "http://www.surfvind.se/AddSurfvindDataIOIOv1.php";
				}

				HttpPost message = new HttpPost(postUrl);
				message.addHeader("content-type",
						"application/x-www-form-urlencoded");

				JSONArray dataArray = new JSONArray();

				int nbrOfItemsToSend = Math.min(measurements.size(), 50);
				/* Create a JSON Array that contains the data. */
				// Dont send more than 50 measures in one post.
				for (int i = 0; i < nbrOfItemsToSend; i++) {
					Stats temp = (Stats) measurements.get(i);
					if (temp != null) {
						dataArray.put(temp.getJSON());
					}
				}
				data.put("measure", dataArray);
				data.put("id", imei);
				data.put("version", version);
				message.setEntity(new StringEntity(data.toString()));
				HttpResponse response = client.execute(message);
				if (response != null) {
					StatusLine line = response.getStatusLine();
					if (line != null) {
						if (line.getStatusCode() == HttpStatus.SC_OK) {
							// Delete the reading that are sent.
							measurements.subList(0, nbrOfItemsToSend).clear();
							sb.append(" Success");
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

	public float GetTemperatureNew(SensorLocation type) {
		float temperature = -1; // Result in %
		// float supply = 5;

		int high = 0, low = 0, total = 0;
		float voltage = (float) 4.93;

		if (type == SensorLocation.SensorInne)
			SendI2CCommand(0x40, 0, 0x3); // Request ADC measurement from AD0
											// channel
		else if (type == SensorLocation.SensorUte)
			SendI2CCommand(0x40, 0, 0x5); // Request ADC measurement from AD2
											// channel

		high = ReadI2CData(0x40, 1); // Read Highbyte
		low = ReadI2CData(0x40, 2); // Read Lowbyte
		total = (high << 8) + low; // Raw measurement 0-1023 value representing
									// 0-5V
		voltage = (float) ((float) total * (float) voltage / (float) 1023);
		temperature = 100 * voltage - 50;
		return temperature;
	}

	public float GetTemperature(SensorLocation type) {
		float temperature = -1; // Result in %
		float supply = 5;

		int high = 0, low = 0, total = 0;
		float voltage = (float) 4.93;

		if (type == SensorLocation.SensorInne)
			SendI2CCommand(0x40, 0, 0x3); // Request ADC measurement from AD0
											// channel
		// SendI2CCommand(0x40,0,0x4); //Request ADC measurement from AD0
		// channel
		else if (type == SensorLocation.SensorUte)
			SendI2CCommand(0x40, 0, 0x5); // Request ADC measurement from AD2
											// channel
		// SendI2CCommand(0x40,0,0x6); //Request ADC measurement from AD2
		// channel

		high = ReadI2CData(0x40, 1); // Read Highbyte
		// ClearScreen();
		// CursorHome();
		// WriteText("High:" + Integer.toString(high));
		// NewLine();
		low = ReadI2CData(0x40, 2); // Read Lowbyte
		// WriteText("Low :" + Integer.toString(low));
		// NewLine();
		total = (high << 8) + low; // Raw measurement 0-1023 value representing
									// 0-5V
		// WriteText("Tot :" + Integer.toString(total));
		// NewLine();
		voltage = (float) ((float) total * (float) voltage / (float) 1024);
		// WriteText("Volt:" + Float.toString(voltage));
		// temperature =
		// (float)(((float)((float)voltage/(float)supply)-(float)0.16)/(float)0.0062);
		temperature = ((float) ((float) (voltage / ((float) supply / (float) 5)) - 1.375))
				/ (float) 0.0225;
		// Temperature compensation for moisture
		// int temperature = xxx;
		// moisture =
		// (float)((float)rawMoisture/(float)((1.0546-0.00216*temperature)));
		// = rawMoisture;

		return temperature;
	}

	public final static float CalibrationDataHumidity = 0.00000000000330f;
	public final static float CalibrationDataHumiditySensitivity = 0.000000000000006f;

	public float GetMoistureCap(SensorLocation type, float temperature) {
		float moisture = -1; // Result in %

		float capacitance = 0;
		try {
			if (type == SensorLocation.SensorInne) {
				capacitance = humidityInside.read();
			} else if (type == SensorLocation.SensorUte) {
				capacitance = humidityOutside.read();
			}
			moisture = (capacitance - CalibrationDataHumidity)
					/ CalibrationDataHumiditySensitivity + 55;

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return moisture;
	}

	public float GetMoisture(SensorLocation type, float temperature) {
		float moisture = -1; // Result in %
		float rawMoisture = -1;
		float supply = (float) 4.93;

		int high = 0, low = 0, total = 0;
		float voltage = 0;

		if (type == SensorLocation.SensorInne)
			SendI2CCommand(0x40, 0, 0x4); // Request ADC measurement from AD1
											// channel
		else if (type == SensorLocation.SensorUte)
			SendI2CCommand(0x40, 0, 0x6); // Request ADC measurement from AD3
											// channel

		high = ReadI2CData(0x40, 1); // Read Highbyte
		// ClearScreen();
		// CursorHome();
		// WriteText("High:" + Integer.toString(high));
		// NewLine();
		low = ReadI2CData(0x40, 2); // Read Lowbyte
		// WriteText("Low :" + Integer.toString(low));
		// NewLine();
		total = (high << 8) + low; // Raw measurement 0-1023 value representing
									// 0-5V
		// WriteText("Tot :" + Integer.toString(total));
		// NewLine();
		voltage = (float) ((float) total * (float) 4.93 / (float) 1024);
		// WriteText("Volt:" + Float.toString(voltage));
		rawMoisture = (float) (((float) ((float) voltage / (float) supply) - (float) 0.16) / (float) 0.0062);

		// Temperature compensation for moisture
		// int temperature = xxx;
		moisture = (float) ((float) rawMoisture / (float) ((1.0546 - 0.00216 * temperature)));
		// moisture = rawMoisture;

		return moisture;
	}

	public void ClearScreen() {
		SendI2CCommand(0xC6, 0, 12);
	}

	public void TurnOnBacklight() {
		SendI2CCommand(0xC6, 0, 19);
	}

	public void NewLine() {
		SendI2CCommand(0xC6, 0, 13);
	}

	public void CursorHome() {
		SendI2CCommand(0xC6, 0, 1);
	}

	public void WriteText(String text) {
		for (char b : text.toCharArray()) {
			SendI2CCommand(0xC6, 0, b);
		}
	}

	public int getTemp() throws ConnectionLostException {
		float voltage = 0;
		try {
			voltage = temp.getVoltage();
			System.out.println("Tempraw: " + voltage);

			voltage = 100 * voltage - 50;
			System.out.println("Tempcalc: " + voltage);
			voltage *= 10;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return (int) voltage;
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
			freq = NBR_READINGS_TO_ANALYZE - (endPulse - startPulse)
					/ (float) (anemometer.getSampleRate() * nbrPulses);
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

			pulseCounter = ioio.openPulseInput(spec, ClockRate.RATE_62KHz,
					PulseMode.FREQ, true);
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
						i2cInne.writeRead(adress / 2, false, toSend, 2,
								receive, 0);
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

	/*
	 * static int GetTemperature(AnalogInput port){ float SensorTemp=0; try {
	 * float SensorVolt = port.getVoltage(); float SupplyVolt = 5; //SensorTemp
	 * = (float) ((SupplyVolt-0.16)/(0.0062*SensorVolt));
	 * 
	 * //Temperature compensation True RH = (Sensor RH)/(1.0546 � 0.00216T), T
	 * in �C //int TrueRH = SensorRF/(1.0546-0.00216*temperature); } catch
	 * (InterruptedException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (ConnectionLostException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } return (int)
	 * SensorTemp; }
	 * 
	 * 
	 * static int GetAnalogueMoisture(AnalogInput port){ float SensorRF=0; try {
	 * float SensorVolt = port.getVoltage(); float SupplyVolt = 5; SensorRF =
	 * (float) ((SupplyVolt-0.16)/(0.0062*SensorVolt));
	 * 
	 * 
	 * // Voltage output (1 // st // order curve fit) VOUT // =(VSUPPLY //
	 * )(0.0062(sensor RH) + 0.16), typical at 25 �C // Temperature
	 * compensation True RH = (Sensor RH)/(1.0546 � 0.00216T), T in �C //int
	 * TrueRH = SensorRF/(1.0546-0.00216*temperature); } catch
	 * (InterruptedException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (ConnectionLostException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } return (int) SensorRF;
	 * }
	 */
}