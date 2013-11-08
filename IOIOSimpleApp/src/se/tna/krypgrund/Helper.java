package se.tna.krypgrund;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

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

public class Helper {
	IOIO ioio = null;
	DigitalOutput A1 = null;
	DigitalOutput A2 = null;
	PwmOutput ASpeed = null;

	DigitalOutput B1 = null;
	DigitalOutput B2 = null;
	PwmOutput BSpeed = null;

	CapSense humidityInside = null;
	CapSense humidityOutside = null;

	DigitalOutput Standby = null;
	TwiMaster i2c = null;
	KrypgrundsService krypService = null;

	PulseInput pulseCounter = null;
	AnalogInput anemometer = null;

	private static final int ANEMOMETER_WIND_VANE = 40;
	private static final int ANEMOMETER_SPEED = 28;

	public Helper(IOIO _ioio, KrypgrundsService kryp) {

		ioio = _ioio;
		krypService = kryp;
		if (ioio != null) {
			try {
				ioio.softReset();
				// Thread.sleep(1000);
				Standby = ioio.openDigitalOutput(6);
				Standby.write(true); // Activate chip

				A1 = ioio.openDigitalOutput(5);
				A1.write(false);
				A2 = ioio.openDigitalOutput(4);
				A2.write(false);

				// ASpeed = ioio.openPwmOutput(3, 1000000);
				// ASpeed.setDutyCycle(0); // Enginge off - 1 = Full on

				// i2c = ioio.openTwiMaster(1, TwiMaster.Rate.RATE_100KHz,
				// false);

				humidityOutside = ioio.openCapSense(31);
				humidityInside = ioio.openCapSense(32);

				Spec spec = new Spec(ANEMOMETER_SPEED);
				spec.mode = Mode.FLOATING;
				pulseCounter = ioio.openPulseInput(spec, ClockRate.RATE_62KHz,
						PulseMode.FREQ, false);
				// pulseCounter =
				// ioio.openPulseInput(ANEMOMETER_SPEED,PulseMode.FREQ);
				anemometer = ioio.openAnalogInput(ANEMOMETER_WIND_VANE);

			} catch (Exception e) {
				e.printStackTrace();
			} // USE FALSE for I2C otherwise to high voltage!!!
		}
	}

	public void Destroy() {
		if (i2c != null)
			i2c.close();
		if (Standby != null)
			Standby.close();
		if (A1 != null)
			A1.close();
		if (A2 != null)
			A2.close();
		if (ASpeed != null)
			ASpeed.close();
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
	public enum SensorType {
		SensorInne, SensorUte;
	}

	byte receive[];

	public boolean SendI2CCommand(final int adress, int register, int data) {
		return true;
		/*
		 * final byte toSend[] = new byte[2]; receive = new byte[1]; toSend[0] =
		 * (byte) register; toSend[1] = (byte) data;
		 * 
		 * if (i2c == null) { krypService.isInitialized = false; return false; }
		 * Thread a = new Thread(new Runnable() {
		 * 
		 * @Override public void run() { try { if (i2c != null) {
		 * i2c.writeRead(adress / 2, false, toSend, 2, receive, 0); } else {
		 * Log.e("Helper", "I2C is null, no command sent!"); } } catch
		 * (ConnectionLostException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (InterruptedException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } } }); a.start();
		 * 
		 * try { a.join(5000); if (a.isAlive()) { a.interrupt();
		 * krypService.isInitialized = false; return false; } } catch
		 * (InterruptedException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } return true;
		 */
	}

	public int ReadI2CData(int adress, int register) {
		byte toSend[] = new byte[1];
		byte toReceive[] = new byte[1];
		toSend[0] = (byte) register;

		try {
			i2c.writeRead(adress / 2, false, toSend, 1, toReceive, 1);
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}

		return (int) (toReceive[0] & 0xFF);
	}

	public boolean SetupGpioChip() {

		if (!SendI2CCommand(0x40, 1, 255))
			return false;
		if (!SendI2CCommand(0x40, 2, 255))
			return false;
		if (!SendI2CCommand(0x40, 3, 0xC0))
			return false;

		return true;
	}

	public float GetTemperatureNew(SensorType type) {
		float temperature = -1; // Result in %
		// float supply = 5;

		int high = 0, low = 0, total = 0;
		float voltage = (float) 4.93;

		if (type == SensorType.SensorInne)
			SendI2CCommand(0x40, 0, 0x3); // Request ADC measurement from AD0
											// channel
		else if (type == SensorType.SensorUte)
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

	public float GetTemperature(SensorType type) {
		float temperature = -1; // Result in %
		float supply = 5;

		int high = 0, low = 0, total = 0;
		float voltage = (float) 4.93;

		if (type == SensorType.SensorInne)
			SendI2CCommand(0x40, 0, 0x3); // Request ADC measurement from AD0
											// channel
		// SendI2CCommand(0x40,0,0x4); //Request ADC measurement from AD0
		// channel
		else if (type == SensorType.SensorUte)
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

	public float GetMoistureCap(SensorType type, float temperature) {
		float moisture = -1; // Result in %

		float capacitance = 0;
		try {
			if (type == SensorType.SensorInne) {
				capacitance = humidityInside.read();
			} else if (type == SensorType.SensorUte) {
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

	public float GetMoisture(SensorType type, float temperature) {
		float moisture = -1; // Result in %
		float rawMoisture = -1;
		float supply = (float) 4.93;

		int high = 0, low = 0, total = 0;
		float voltage = 0;

		if (type == SensorType.SensorInne)
			SendI2CCommand(0x40, 0, 0x4); // Request ADC measurement from AD1
											// channel
		else if (type == SensorType.SensorUte)
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

	public static int FanMaxSpeed = 100;
	public static int Fan30Percent = (int) (FanMaxSpeed * 0.3);
	public static int FanStop = 0;
	public static boolean FanOn = false;

	boolean ControlFan(int Speed, boolean Clockwise) {
		boolean retVal = true;
		if (A1 != null && A2 != null) {
			try {
				if (Speed == FanStop) {
					A1.write(false);
					A2.write(false);
					FanOn = false;
				} else if (Clockwise) {
					A1.write(true);
					A2.write(false);
					FanOn = true;
				} else {
					A1.write(false);
					A2.write(true);
					FanOn = true;
				}
				ASpeed.setDutyCycle((float) (((float) Speed) / FanMaxSpeed));
			} catch (ConnectionLostException e) {
				retVal = false;
				e.printStackTrace();
			}
		} else {
			retVal = false;
		}
		return retVal;
	}

	public float getWindSpeed() {
		float speedMeterPerSecond = 0;
		try {
			float freq = pulseCounter.getFrequency();
			speedMeterPerSecond = freq * 1.006f;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ConnectionLostException e) {
			e.printStackTrace();
		}
		return speedMeterPerSecond;
	}

	public float getWindDirection() {
		float direction = 0;
		try {
			// float voltage = anemometer.getVoltage();
			float voltage = anemometer.read();
			voltage *= 3.3;
			direction = voltage;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ConnectionLostException e) {
			e.printStackTrace();
		}
		return direction * 360f / 3.3f;
	}

	public boolean IsFanOn() {
		return FanOn;
	}

	public String SendKrypgrundsDataToServer(ArrayList<KrypgrundStats> history,
			boolean forceSendData, String id) {
		String retVal = "Trying to send " + history.size() + " items.\n";
		HttpClient client = null;
		JSONObject data;
		boolean SendSuccess = true;

		try {
			/*
			 * Keep sending data until there is a send failure or the history is
			 * emptied.
			 */
			while (SendSuccess && history.size() > 0) {
				data = new JSONObject();

				client = new DefaultHttpClient();
				HttpPost message = new HttpPost(
						"http://www.surfvind.se/Krypgrund.php");
				message.addHeader("content-type",
						"application/x-www-form-urlencoded");
				JSONArray dataArray = new JSONArray();

				int itemsToSend = Math.min(history.size(), 50);
				/* Create a JSON Array that contains the data. */
				// Dont send more than 50 measures in one post.
				for (int i = 0; i < itemsToSend; i++) {
					Stats temp = history.get(i);
					dataArray.put(temp.getJSON());
				}
				data.put("measure", dataArray);
				data.put("id", id);
				message.setEntity(new StringEntity(data.toString()));
				HttpResponse response = client.execute(message);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// Delete the reading that are sent.
					for (int i = 0; i < itemsToSend; i++) {
						history.remove(0);
					}
					retVal += "Success";
				} else {
					SendSuccess = false;
					retVal += "F: " + response.getStatusLine().getStatusCode();
				}
			}
		} catch (Exception e) {
			retVal += "Ex:" + e.toString();

		} finally {
			if (null != client)
				client.getConnectionManager().shutdown();
		}
		return retVal;
	}

	public String SendSurfvindDataToServer(ArrayList<SurfvindStats> history,
			boolean forceSendData, String id, String version) {
		String retVal = "Trying to send " + history.size() + " items.\n";
		HttpClient client = null;
		JSONObject data;
		boolean SendSuccess = true;

		try {
			/*
			 * Keep sending data until there is a send failure or the history is
			 * emptied.
			 */
			while (SendSuccess && history.size() > 0) {
				data = new JSONObject();

				client = new DefaultHttpClient();
				HttpPost message = new HttpPost(
						"http://www.surfvind.se/AddSurfvindDataIOIOv1.php");
				message.addHeader("content-type",
						"application/x-www-form-urlencoded");
				JSONArray dataArray = new JSONArray();

				int itemsToSend = Math.min(history.size(), 50);
				/* Create a JSON Array that contains the data. */
				// Dont send more than 50 measures in one post.
				for (int i = 0; i < itemsToSend; i++) {
					Stats temp = history.get(i);
					dataArray.put(temp.getJSON());
				}
				data.put("measure", dataArray);
				data.put("id", id);
				data.put("version", version);
				message.setEntity(new StringEntity(data.toString()));
				HttpResponse response = client.execute(message);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// Delete the reading that are sent.
					for (int i = 0; i < itemsToSend; i++) {
						history.remove(0);
					}
					retVal += "Success";
				} else {
					SendSuccess = false;
					retVal += "F: " + response.getStatusLine().getStatusCode();
				}
			}
		} catch (Exception e) {
			retVal += "Ex:" + e.toString();

		} finally {
			if (null != client)
				client.getConnectionManager().shutdown();
		}
		return retVal;
	}
}