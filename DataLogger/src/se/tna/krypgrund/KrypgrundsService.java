package se.tna.krypgrund;

import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import se.tna.krypgrund.Helper.ChipCap2;
import se.tna.krypgrund.Helper.SensorLocation;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class KrypgrundsService extends IOIOService {
	private static final String version = "IOIO_R1A";
	protected long TIME_BETWEEN_SEND_DATA = TimeUnit.MINUTES.toMillis(5);
	protected long TIME_BETWEEN_ADD_TO_HISTORY = TimeUnit.MINUTES.toMillis(2);
	protected long TIME_BETWEEN_READING = TimeUnit.SECONDS.toMillis(5);
	protected long TIME_BETWEEN_FAN_ON_OFF = TimeUnit.MINUTES.toMillis(3);

	private long timeForLastSendData = 0;
	private long timeBetweenSendingDataToServer = TIME_BETWEEN_SEND_DATA;
	private long timeForLastAddToHistory;
	private long timeBetweenAddToHistory = TIME_BETWEEN_ADD_TO_HISTORY;
	private long timeBetweenReading = TIME_BETWEEN_READING;
	private long timeForLastFanControl = 0;

	private final IBinder mBinder = new MyBinder();

	ArrayList<KrypgrundStats> rawMeasurements = new ArrayList<KrypgrundStats>();
	ArrayList<SurfvindStats> rawSurfvindsMeasurements = new ArrayList<SurfvindStats>();

	public String debugText = "";
	public String statusText = "";
	boolean isInitialized = false;
	private Helper helper;
	private String id = "";
	boolean clockwise = true;
	public final static int SURFVIND = R.id.weatherStation;
	public final static int KRYPGRUND = R.id.crawlspaceStation;

	private long watchdog_TimeSinceLastOkData;

	public enum ServiceMode {
		Survfind, Krypgrund;

		public String toString() {
			if (this == ServiceMode.Survfind)
				return "Surfvind";
			else
				return "Krypgrund";
		}
	};

	public ServiceMode serviceMode = ServiceMode.Krypgrund;

	public enum HumidSensor {
		OldAnalog, Capacitive, ChipCap2, Random
	}

	Stats oneMeasurement;

	private HumidSensor krypgrundSensor = HumidSensor.ChipCap2;
	@SuppressLint("UseSparseArrays")
	ArrayList<KrypgrundStats> krypgrundHistory = new ArrayList<KrypgrundStats>();
	ArrayList<SurfvindStats> surfvindHistory = new ArrayList<SurfvindStats>();
	protected static boolean isIOIOConnected = false;
	static {

	}

	@Override
	protected IOIOLooper createIOIOLooper() {

		return new BaseIOIOLooper() {

			@Override
			public void disconnected() {
				super.disconnected();
				isIOIOConnected = false;
				isInitialized = false;
				helper = null;
				id = "123456789";
				Helper.appendLog("*** IOIO disconnected. ***");
			}

			@Override
			public void setup() throws ConnectionLostException,
					InterruptedException {
				timeForLastSendData = System.currentTimeMillis();
				timeForLastAddToHistory = System.currentTimeMillis();
				timeForLastFanControl = System.currentTimeMillis();
				isIOIOConnected = true;
				TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				id = telephonyManager.getDeviceId();
				updateSettings();
				helper = new Helper(ioio_, KrypgrundsService.this, id, version,
						serviceMode);
				isInitialized = true;
				watchdog_TimeSinceLastOkData = System.currentTimeMillis();
				Helper.appendLog("*** IOIO connected OK. ***");
				Helper.appendLog("Service mode is: " + serviceMode.toString());
			}

			@Override
			public void loop() {
				try {
					if (System.currentTimeMillis()
							- watchdog_TimeSinceLastOkData > TimeUnit.MINUTES
								.toMillis(10)) {
						Helper.appendLog("Restarting ioio due to no good data for a long time.");
						ioio_.hardReset();
					}

					if (serviceMode == ServiceMode.Krypgrund) {

						// Always create a new object, as this is added to the
						// list.
						oneMeasurement = new KrypgrundStats();

						if (krypgrundSensor == HumidSensor.ChipCap2) {
							KrypgrundStats oneKrypgrundMeasurement = (KrypgrundStats) oneMeasurement;
							ChipCap2 inne = helper
									.GetChipCap2TempAndHumidity(SensorLocation.SensorInne);
							ChipCap2 ute = helper
									.GetChipCap2TempAndHumidity(SensorLocation.SensorUte);
							oneKrypgrundMeasurement.temperatureInne = inne.temperature;
							oneKrypgrundMeasurement.moistureInne = inne.humidity;
							oneKrypgrundMeasurement.temperatureUte = ute.temperature;
							oneKrypgrundMeasurement.moistureUte = ute.humidity;
							if (inne.okReading && ute.okReading) {
								// Update the watchdog.
								watchdog_TimeSinceLastOkData = System
										.currentTimeMillis();
							}

							/*
							 * y = 4.632248129 e6.321315927�10-2 x y=max fukt
							 * i gram/m3
							 */

							oneKrypgrundMeasurement.absolutFuktUte = (float) (4.632248129 * (Math
									.expm1(0.06321315927 * oneKrypgrundMeasurement.temperatureUte) + 1))
									* oneKrypgrundMeasurement.moistureUte;
							oneKrypgrundMeasurement.absolutFuktInne = (float) (4.632248129 * (Math
									.expm1(0.06321315927 * oneKrypgrundMeasurement.temperatureInne) + 1))
									* oneKrypgrundMeasurement.moistureInne;
							rawMeasurements.add(oneKrypgrundMeasurement);
						}
					} else if (serviceMode == ServiceMode.Survfind) {

						// Always create a new object, as this is added to the
						// list.
						SurfvindStats temp = new SurfvindStats();
						oneMeasurement = temp;
						temp.windDirectionAvg = helper.queryIOIO(Helper.ANALOG);
						temp.windSpeedAvg = helper.queryIOIO(Helper.FREQ);
						if (temp.windDirectionAvg != -1
								&& temp.windSpeedAvg != -1) {
							rawSurfvindsMeasurements.add(temp);
						}

					}
					oneMeasurement.batteryVoltage = helper.getBatteryVoltage();
					oneMeasurement.temperature = helper.getTemp();

					Thread.sleep(timeBetweenReading);

				} catch (Exception e) {
					e.printStackTrace();
					if (helper != null) {
						try {
							helper.ControlFan(false);
						} catch (Exception ee) {
							ee.printStackTrace();
						}
					}
				}
			}
		};
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

	}

	/**
	 * Starts or stops the fan depending on the Stats data.
	 * 
	 * - We should not change the On/Off to often. - If inside temperature is
	 * less than 1 degree, fan should not run. - If the moisture inside is at
	 * least 2 % larger than outside, then start the fan.
	 * 
	 * @param data
	 */
	public void ControlFan(KrypgrundStats data) {

		if (System.currentTimeMillis() - timeForLastFanControl > TIME_BETWEEN_FAN_ON_OFF) {
			timeForLastFanControl = System.currentTimeMillis();
			// Dont start fans if inside temp is close to
			// freezeingpoint.
			if (data.temperatureInne < 0.5) {
				helper.ControlFan(false);
			} else {
				// Start the fans!!

				if (data.absolutFuktInne > data.absolutFuktUte + 15) {
					helper.ControlFan(true);
				} else {
					helper.ControlFan(false);
				}
			}
		}
	}

	private TimerTask mConnectTask = null;
	private TimerTask mSendDataTask = null;
	private TimerTask mAddToHistoryTask = null;
	Timer addToHistoryTimer;
	Timer ioioConnectorTimer;
	Timer dataSenderTimer;

	class SendDataTask extends TimerTask {
		@Override
		public void run() {
			if (serviceMode == ServiceMode.Krypgrund) {
				debugText = helper.SendDataToServer(krypgrundHistory,
						ServiceMode.Krypgrund);
			} else if (serviceMode == ServiceMode.Survfind) {
				debugText += helper.SendDataToServer(surfvindHistory,
						ServiceMode.Survfind);
			}
			timeForLastSendData = System.currentTimeMillis();
		}

	}

	class AddDataTask extends TimerTask {
		@Override
		public void run() {
			// Reset time, so that we will soon add a new value to
			// the history
			timeForLastAddToHistory = System.currentTimeMillis();

			if (serviceMode == ServiceMode.Krypgrund) {

				KrypgrundStats average = KrypgrundStats
						.getAverage(rawMeasurements);
				ControlFan(average);
				average.fanOn = helper.IsFanOn();
				krypgrundHistory.add(average);
				rawMeasurements.clear();
			}

			if (serviceMode == ServiceMode.Survfind) {
				SurfvindStats average = SurfvindStats
						.getAverage(rawSurfvindsMeasurements);
				surfvindHistory.add(average);
				rawSurfvindsMeasurements.clear();
			}

			rawMeasurements = new ArrayList<KrypgrundStats>();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Helper.appendLog("\n\n\n\nKrypgrundService Started");
		UncaughtExceptionHandler s = new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				StringBuffer sb = new StringBuffer();
				sb.append("Uncaught excepion:\n");
				sb.append("ThreadName: " + thread.getName() + "\n");
				sb.append("Message: " + ex.getMessage() + "\n");
				Writer writer = new StringWriter();
				PrintWriter printWriter = new PrintWriter(writer);
				ex.printStackTrace(printWriter);
				sb.append("StackTrace: \n" + writer.toString());
				Helper.appendLog(sb.toString());
			}
		};
		Thread.setDefaultUncaughtExceptionHandler(s);

		watchdog_TimeSinceLastOkData = System.currentTimeMillis();
		if (mConnectTask == null) {
			mConnectTask = new TimerTask() {

				@Override
				public void run() {
					if (!isIOIOConnected) {
						Helper.appendLog("IOIO is not connected, lets restart to try to connect.");
						// KrypgrundsService.this.restart();
					}
				}
			};
			ioioConnectorTimer = new Timer("IOIOConnector");
			ioioConnectorTimer.scheduleAtFixedRate(mConnectTask, 10000, 60000);
		}

		if (mSendDataTask == null) {
			mSendDataTask = new SendDataTask();
			dataSenderTimer = new Timer("DataSender");
			dataSenderTimer.scheduleAtFixedRate(mSendDataTask,
					timeBetweenSendingDataToServer,
					timeBetweenSendingDataToServer);
		}

		if (mAddToHistoryTask == null) {
			mAddToHistoryTask = new AddDataTask();
			addToHistoryTimer = new Timer("AddToHistory");
			addToHistoryTimer.scheduleAtFixedRate(mAddToHistoryTask,
					timeBetweenAddToHistory, timeBetweenAddToHistory);
		}
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class MyBinder extends Binder {
		KrypgrundsService getService() {
			return KrypgrundsService.this;
		}
	}

	public void updateSettings() {
		SharedPreferences prefs = getSharedPreferences("TNA_Sensor",
				MODE_PRIVATE);
		int type = prefs.getInt(SetupActivity.SENSOR_TYPE_RADIO, SURFVIND);
		if (type == SURFVIND)
			serviceMode = ServiceMode.Survfind;
		else if (type == KRYPGRUND)
			serviceMode = ServiceMode.Krypgrund;
		long readInterval = prefs.getLong(SetupActivity.READ_INTERVAL,
				TimeUnit.MINUTES.toMillis(5));
		timeBetweenSendingDataToServer = readInterval;
		Helper.appendLog("Settings refreshed in Service:");
		Helper.appendLog("ServiceMode =" + serviceMode.toString());
		Helper.appendLog("TimeBetweenSendingDataToServer = "
				+ timeBetweenSendingDataToServer);
		Helper.appendLog("ReadInterval = " + readInterval);
		// helper.Destroy();
		// helper

		if (dataSenderTimer != null) {
			dataSenderTimer.cancel();
			dataSenderTimer = new Timer("DataSenderTimer");
			dataSenderTimer.scheduleAtFixedRate(new SendDataTask(),
					timeBetweenSendingDataToServer,
					timeBetweenSendingDataToServer);
		}
		if (addToHistoryTimer != null) {
			addToHistoryTimer.cancel();
			addToHistoryTimer = new Timer("AddToHistoryTimer");
			addToHistoryTimer.scheduleAtFixedRate(new AddDataTask(),
					timeBetweenAddToHistory, timeBetweenAddToHistory);
		}
	}

	public void setForceFan(boolean on) {
		if (helper != null) {
			helper.ControlFan(on);
		}
	}

	StatusOfService status = new StatusOfService();

	public StatusOfService getStatus() {

		synchronized (KrypgrundsService.this) {
			int pos = rawMeasurements.size() - 1;
			if (pos >= 0) {
				KrypgrundStats oneReading = rawMeasurements.get(pos);
				status.moistureInne = oneReading.moistureInne;
				status.moistureUte = oneReading.moistureUte;
				status.temperatureUte = oneReading.temperatureUte;
				status.temperatureInne = oneReading.temperatureInne;
				status.absolutFuktInne = oneReading.absolutFuktInne;
				status.absolutFuktUte = oneReading.absolutFuktUte;
			}
			pos = rawSurfvindsMeasurements.size() - 1;
			if (pos >= 0) {
				SurfvindStats oneReading = rawSurfvindsMeasurements.get(pos);
				status.windDirection = (int) oneReading.windDirectionAvg;
				status.windSpeed = oneReading.windSpeedAvg;
				status.analogInput = oneReading.windDirectionAvg * 3.3f / 360f;
			}
		}
		if (helper != null)
			status.fanOn = helper.IsFanOn();
		if (surfvindHistory != null)
			status.historySize = surfvindHistory.size();
		if (rawSurfvindsMeasurements != null)
			status.readingSize = rawSurfvindsMeasurements.size();
		if (krypgrundHistory != null)
			status.historySize = krypgrundHistory.size();
		if (rawMeasurements != null)
			status.readingSize = rawMeasurements.size();

		status.statusMessage = debugText;
		status.timeForLastSendData = timeForLastSendData;
		status.timeBetweenSendingDataToServer = timeBetweenSendingDataToServer;
		status.timeForLastAddToHistory = timeForLastAddToHistory;
		status.timeBetweenAddToHistory = timeBetweenAddToHistory;
		status.timeBetweenReading = timeBetweenReading;
		status.timeForLastFanControl = timeForLastFanControl;
		status.deviceId = id;

		return status;
	}
}
