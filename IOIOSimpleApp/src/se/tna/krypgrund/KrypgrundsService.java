package se.tna.krypgrund;

import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import android.telephony.TelephonyManager;

public class KrypgrundsService extends IOIOService {
	protected static final long TIME_BETWEEN_SEND_DATA = TimeUnit.MINUTES
			.toMillis(5);
	protected static final long TIME_BETWEEN_ADD_TO_HISTORY = TimeUnit.MINUTES
			.toMillis(2);
	protected static final long TIME_BETWEEN_READING = TimeUnit.SECONDS
			.toMillis(5);
	protected static final long TIME_BETWEEN_FAN_ON_OFF = TimeUnit.MINUTES
			.toMillis(3);

	private long timeForLastSendData = 0;
	private long timeBetweenSendingDataToServer = TIME_BETWEEN_SEND_DATA;
	private long timeForLastAddToHistory;
	private long timeBetweenAddToHistory = TIME_BETWEEN_ADD_TO_HISTORY;
	private long timeBetweenReading = TIME_BETWEEN_READING;
	private long timeForLastFanControl = 0;

	public Stats data;

	private final IBinder mBinder = new MyBinder();
	private boolean debugMode = false;
	private Stats debugStats = null;
	private boolean forceSendData = false;
	ArrayList<Stats> rawMeasurements = new ArrayList<Stats>();
	public String debugText = "";
	public String statusText = "";
	boolean isInitialized = false;
	private Helper helper;
	private String id = "";
	boolean clockwise = true;
	@SuppressLint("UseSparseArrays")
	ArrayList<Stats> history = new ArrayList<Stats>();

	public void setDebugMode(boolean enable) {
		debugMode = enable;
	}

	public void setDebugStats(Stats data) {
		debugStats = data;
	}

	@Override
	protected IOIOLooper createIOIOLooper() {

		return new BaseIOIOLooper() {

			@Override
			public void disconnected() {
				super.disconnected();
				isInitialized = false;
				helper.Destroy();
			}

			@Override
			public void setup() throws ConnectionLostException,
					InterruptedException {
				timeForLastSendData = System.currentTimeMillis();
				timeForLastAddToHistory = System.currentTimeMillis();
				timeForLastFanControl = System.currentTimeMillis();
				initialize();
			}

			private synchronized void initialize() {
				TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				id = telephonyManager.getDeviceId();

				helper = new Helper(ioio_, KrypgrundsService.this);
				if (helper.SetupGpioChip()) {
					isInitialized = true;
				}
				
			}

			@Override
			public void loop() {
				try {
					if (!isInitialized) {
						initialize();
					}
					// Always create a new object, as this is added to the list.
					Stats temp = new Stats();

					if (debugMode) {
						if (debugStats != null) {
							temp = debugStats;
						}
					} else {
						temp.temperatureUte = 10;// helper.GetTemperature(SensorType.SensorUte);
						temp.temperatureInne = 5;// helper.GetTemperature(SensorType.SensorInne);
						temp.moistureUte = 23;// helper.GetMoisture(SensorType.SensorUte,
												// temp.temperatureUte);
						temp.moistureInne = 56;// helper.GetMoisture(SensorType.SensorInne,
												// temp.temperatureInne);
						/*
						 * y = 4.632248129 e6.321315927ï¿½10-2 x y=max fukt i
						 * gram/m3
						 */

						temp.absolutFuktUte = (float) (4.632248129 * (Math
								.expm1(0.06321315927 * temp.temperatureUte) + 1))
								* temp.moistureUte;
						temp.absolutFuktInne = (float) (4.632248129 * (Math
								.expm1(0.06321315927 * temp.temperatureInne) + 1))
								* temp.moistureInne;

					}
					synchronized (KrypgrundsService.this) {
						data = temp;
					}
					rawMeasurements.add(temp);

					if (System.currentTimeMillis() - timeForLastAddToHistory > timeBetweenAddToHistory) {
						// Reset time, so that we will soon add a new value to
						// the history
						timeForLastAddToHistory = System.currentTimeMillis();
						// Always create a new object, as it is added to the
						// history list.
						Stats total = new Stats();

						// Calculate an averagevalue of all the readings.
						for (Stats stat : rawMeasurements) {
							total.moistureInne += stat.moistureInne;
							total.moistureUte += stat.moistureUte;
							total.temperatureInne += stat.temperatureInne;
							total.temperatureUte += stat.temperatureUte;
							total.absolutFuktInne += stat.absolutFuktInne;
							total.absolutFuktUte += stat.absolutFuktUte;
						}
						float size = rawMeasurements.size();
						total.moistureInne /= size;
						total.moistureUte /= size;
						total.temperatureInne /= size;
						total.temperatureUte /= size;
						total.absolutFuktInne /= size;
						total.absolutFuktUte /= size;

						ControlFan(total);
						total.fanOn = helper.IsFanOn();
						history.add(total);

						// How often should we connect to server?
						if (System.currentTimeMillis() - timeForLastSendData > timeBetweenSendingDataToServer) {
							debugText = helper.SendDataToServer(history,
									forceSendData, id);
							// Reset time even if it fails - don´t hesitate to
							// retry sending.
							timeForLastSendData = System.currentTimeMillis();
						}
						rawMeasurements = new ArrayList<Stats>();

					} else {
						// String text = "HSize=" + history.size() + " Succ="
						// + Boolean.toString(helper.GetSendSuccess())
						// + " fDelay:"
						// + Integer.toString(helper.GetFailureDelay());
						// debugText = text;
					}
					Thread.sleep(timeBetweenReading);

				} catch (Exception e) {
					e.printStackTrace();
					if (helper != null) {
						try {
							helper.ControlFan(0, clockwise); // will not work if
																// connection
																// Lost
																// exception.
						} catch (Exception ee) {
							ee.printStackTrace();
						}
					}
				}

			}

			/**
			 * Starts or stops the fan depending on the Stats data.
			 * 
			 * - We should not change the On/Off to often. - If inside
			 * temperature is less than 1 degree, fan should not run. - If the
			 * moisture inside is at least 2 % larger than outside, then start
			 * the fan.
			 * 
			 * @param data
			 */
			public void ControlFan(Stats data) {

				if (System.currentTimeMillis() - timeForLastFanControl > TIME_BETWEEN_FAN_ON_OFF) {
					timeForLastFanControl = System.currentTimeMillis();
					// Dont start fans if inside temp is close to
					// freezeingpoint.
					if (data.temperatureInne < 0.9) {
						helper.ControlFan(Helper.FanStop, true);
					} else {
						// Start the fans!!

						if (data.absolutFuktInne > data.absolutFuktUte * 1.02) {
							float speed = (float) (Helper.FanMaxSpeed);// /*seekBar_.getProgress()/99);
							helper.ControlFan((int) speed, clockwise);
						} else {
							helper.ControlFan(Helper.FanStop, true);
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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
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

	public void setForceSendData(boolean checked) {
		forceSendData = checked;

	}

	public void setForceFan(boolean on) {
		if (on) {
			helper.ControlFan(Helper.FanMaxSpeed, true);
		} else {
			helper.ControlFan(Helper.FanStop, true);
		}
	}

	public StatusOfService getStatus() {
		StatusOfService status = new StatusOfService();

		synchronized (KrypgrundsService.this) {
			int pos = rawMeasurements.size() - 1;
			if (pos >= 0) {
				Stats oneReading = rawMeasurements.get(pos);
				status.moistureInne = oneReading.moistureInne;
				status.moistureUte = oneReading.moistureUte;
				status.temperatureUte = oneReading.temperatureUte;
				status.temperatureInne = oneReading.temperatureInne;
				status.absolutFuktInne = oneReading.absolutFuktInne;
				status.absolutFuktUte = oneReading.absolutFuktUte;
			}
		}
		if (helper != null)
			status.fanOn = helper.IsFanOn();
		if (history != null)
			status.historySize = history.size();
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
