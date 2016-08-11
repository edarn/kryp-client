package se.tna.commonloggerservice;

import android.content.SharedPreferences;

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

import se.tna.commonloggerservice.Helper.ChipCap2;
import se.tna.commonloggerservice.Helper.SensorLocation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class KrypgrundsService extends IOIOService {
    private static final String version = "IOIO_R1A06";
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

    public static final String SURFVIND = "WeatherStation";
    public static final String KRYPGRUND = "CrawlspaceMonitor";

    public static final String LOGGER_MODE = "Logger Mode";
    public static final String MEASUREMENT_DELAY_MS = "Measurement Delay";
    public static final String SEND_TO_SERVER_DELAY_MS = "Send To Server Delay";

    ConcurrentMaxSizeArray<KrypgrundStats> rawKrypgrundMeasurements = new ConcurrentMaxSizeArray<KrypgrundStats>();
    ConcurrentMaxSizeArray<SurfvindStats> rawSurfvindsMeasurements = new ConcurrentMaxSizeArray<SurfvindStats>();

    public String debugText = "";
    public String statusText = "";
    boolean isInitialized = false;
    private Helper helper;
    private String id = "";
    boolean clockwise = true;

    private long watchdog_TimeSinceLastOkData;
    private long mWatchdogTime;

    private String preferenceFileName = "TNA_Sensor";

    public enum ServiceMode {
        Survfind, Krypgrund;

        public String toString() {
            if (this == ServiceMode.Survfind)
                return "Surfvind";
            else
                return "Krypgrund";
        }
    }

    ;

    public ServiceMode serviceMode = ServiceMode.Krypgrund;

    public enum HumidSensor {
        OldAnalog, Capacitive, ChipCap2, Random
    }


    private HumidSensor krypgrundSensor = HumidSensor.ChipCap2;
    @SuppressLint("UseSparseArrays")
    ArrayList<KrypgrundStats> krypgrundHistory = new ArrayList<KrypgrundStats>(500);
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
                id = "";
                Helper.appendLog("*** IOIO disconnected. ***");
                Helper.trackEvent("LoggerService", "Lifecycle", "IOIO Disconnect", 0L);

            }

            @Override
            public void setup() throws ConnectionLostException, InterruptedException {
                isIOIOConnected = true;
                Helper.trackEvent("LoggerService", "Lifecycle", "IOIO Connected", 0L);

                timeForLastSendData = System.currentTimeMillis();
                timeForLastAddToHistory = System.currentTimeMillis();
                timeForLastFanControl = System.currentTimeMillis();
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                id = telephonyManager.getDeviceId();
                updateSettings(preferenceFileName);
                helper = new Helper(ioio_, KrypgrundsService.this, id, version, serviceMode);
                watchdog_TimeSinceLastOkData = System.currentTimeMillis();
                Helper.appendLog("*** IOIO connected OK. ***");
                Helper.appendLog("Service mode is: " + serviceMode.toString());
                isInitialized = true;
                nbrRainPulses = 0;
                rainMeasurementTime = System.currentTimeMillis();
            }

            float nbrRainPulses = 0;
            long rainMeasurementTime = 0;

            @Override
            public void loop() {
                try {
                    //Keep this watchdog at top. Other dog will catch if no good data is returned.
                    mWatchdogTime = System.currentTimeMillis();
                    System.out.println("Toggelling watchdog 1");
                    helper.toggleWatchdog();
                    System.out.println("Toggelling watchdog 2");
                    if (System.currentTimeMillis() - watchdog_TimeSinceLastOkData > TimeUnit.MINUTES.toMillis(60)) {
                        Helper.appendLog("Restarting ioio due to no good data for a long time.");
                        ioio_.hardReset();
                    }

                    if (serviceMode == ServiceMode.Krypgrund) {
                        // Always create a new object, as this is added to the
                        // list.

                        if (krypgrundSensor == HumidSensor.ChipCap2) {
                            KrypgrundStats oneKrypgrundMeasurement = new KrypgrundStats();
                            ChipCap2 inne = helper.GetChipCap2TempAndHumidity(SensorLocation.SensorOnBoard);
                            ChipCap2 ute = helper.GetChipCap2TempAndHumidity(SensorLocation.SensorUte);
                            oneKrypgrundMeasurement.temperatureInne = inne.temperature;
                            oneKrypgrundMeasurement.moistureInne = inne.humidity;
                            oneKrypgrundMeasurement.temperatureUte = ute.temperature;
                            oneKrypgrundMeasurement.moistureUte = ute.humidity;
                            oneKrypgrundMeasurement.batteryVoltage = helper.getBatteryVoltage();
                            if (inne.okReading || ute.okReading) {
                                // Update the watchdog.
                                watchdog_TimeSinceLastOkData = System.currentTimeMillis();
                            }

							/*
                             * y = 4.632248129 e6.321315927�10-2 x y=max fukt
							 * i gram/m3
							 */

                            oneKrypgrundMeasurement.absolutFuktUte = (float) (4.632248129 * (Math.expm1(0.06321315927 * oneKrypgrundMeasurement.temperatureUte) + 1))
                                    * oneKrypgrundMeasurement.moistureUte;
                            oneKrypgrundMeasurement.absolutFuktInne = (float) (4.632248129 * (Math.expm1(0.06321315927 * oneKrypgrundMeasurement.temperatureInne) + 1))
                                    * oneKrypgrundMeasurement.moistureInne;
                            rawKrypgrundMeasurements.add(oneKrypgrundMeasurement);
                        }
                    } else if (serviceMode == ServiceMode.Survfind) {

                        // Always create a new object, as this is added to the
                        // list.

                        SurfvindStats oneMeasurement = new SurfvindStats();
                        oneMeasurement.windDirectionAvg = helper.queryIOIO(Helper.ANALOG);
                        oneMeasurement.windSpeedAvg = helper.queryIOIO(Helper.FREQ);
                        oneMeasurement.batteryVoltage = helper.getBatteryVoltage();
                        ChipCap2 tempAndHumidity = helper.GetChipCap2TempAndHumidity(SensorLocation.SensorOnBoard);
                        oneMeasurement.onBoardHumidity = tempAndHumidity.humidity;
                        oneMeasurement.onBoardTemperature = tempAndHumidity.temperature;

                        tempAndHumidity = helper.GetChipCap2TempAndHumidity(SensorLocation.SensorInne);
                        oneMeasurement.firstExternalHumidity = tempAndHumidity.humidity;
                        oneMeasurement.firstExternalTemperature = tempAndHumidity.temperature;

                        float frequency = helper.queryIOIO(Helper.RAIN);
                        if (frequency > 0) {
                            System.out.println("REGN frequency = " + frequency);
                            if (frequency < 0.5) {
                                nbrRainPulses = 2;
                            } else {
                                nbrRainPulses = frequency * (System.currentTimeMillis() - rainMeasurementTime) / 1000;
                            }
                        }
                        rainMeasurementTime = System.currentTimeMillis();

                        System.out.println("REGN Antal pulser: " + nbrRainPulses);
                        oneMeasurement.rainFall = nbrRainPulses;


                        Helper.BarometricPressure preassure = helper.GetBarometric();

                        if (preassure.okReading) {
                            System.out.println("Preassure is: " + preassure.pressure);
                            oneMeasurement.airPressure = preassure.pressure;
                        }

                        //tempAndHumidity = helper.GetChipCap2TempAndHumidity(SensorLocation.SensorUte);
                        System.out.println(oneMeasurement.getJSON());

                        if (oneMeasurement.windDirectionAvg != -1 || oneMeasurement.windSpeedAvg != -1 || oneMeasurement.onBoardHumidity != 0 || oneMeasurement.onBoardTemperature
                                != -40  || oneMeasurement.airPressure != 0) {
                            if (oneMeasurement.windSpeedAvg == -1) oneMeasurement.windSpeedAvg = 0;

                            rawSurfvindsMeasurements.add(oneMeasurement);
                            // Update the watchdog.
                            watchdog_TimeSinceLastOkData = System.currentTimeMillis();
                        }

                    }

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


    /**
     * Starts or stops the fan depending on the Stats data.
     * <p>
     * - We should not change the On/Off to often. - If inside onBoardTemperature is
     * less than 1 degree, fan should not run. - If the onBoardHumidity inside is at
     * least 2 % larger than outside, then start the fan.
     *
     * @param data
     */
    public void ControlFan(KrypgrundStats data) {
        if (data != null && helper != null) {
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
    }

    private TimerTask mConnectTask = null;
    private TimerTask mSendDataTask = null;
    private TimerTask mAddToHistoryTask = null;
    private TimerTask mPreventIOIOHWLockTask = null;
    private Timer addToHistoryTimer;
    private Timer ioioConnectorTimer;
    private Timer dataSenderTimer;
    private Timer preventIOIOHWLockTimer;

    class SendDataTask extends TimerTask {
        @Override
        public void run() {
            if (helper != null) {
                debugText = "";
                if (serviceMode == ServiceMode.Krypgrund) {
                    debugText = helper.SendDataToServer(krypgrundHistory, ServiceMode.Krypgrund);
                    timeForLastSendData = System.currentTimeMillis();
                } else if (serviceMode == ServiceMode.Survfind) {
                    debugText = helper.SendDataToServer(surfvindHistory, ServiceMode.Survfind);
                    timeForLastSendData = System.currentTimeMillis();
                }

            }

        }

    }

    static long IOIOrestarted = 0;

    class PreventIOIOHWLockTask extends TimerTask {
        @Override
        public void run() {
            if (isIOIOConnected && helper != null && helper.ioio != null) {
                try {
                    Helper.trackEvent("LoggerService", "Timer", "IOIOHWLock thread restarted IOIO device", IOIOrestarted);
                    IOIOrestarted++;
                    helper.ioio.hardReset();

                } catch (ConnectionLostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    class AddDataTask extends TimerTask {
        @Override
        public void run() {
            // Reset time, so that we will soon add a new value to
            // the history
            timeForLastAddToHistory = System.currentTimeMillis();

            if (serviceMode == ServiceMode.Krypgrund) {
                if (rawKrypgrundMeasurements != null && rawKrypgrundMeasurements.size() > 0) {
                    KrypgrundStats average = KrypgrundStats.getAverage(rawKrypgrundMeasurements);
                    if (average != null) {
                        ControlFan(average);
                        if (helper != null) {
                            average.fanOn = helper.IsFanOn();
                        }
                        krypgrundHistory.add(average);
                    }
                    rawKrypgrundMeasurements.clear();
                }
                rawKrypgrundMeasurements = new ConcurrentMaxSizeArray<KrypgrundStats>();
            }

            if (serviceMode == ServiceMode.Survfind) {
                if (rawSurfvindsMeasurements != null && rawSurfvindsMeasurements.size() > 0) {
                    SurfvindStats average = SurfvindStats.getAverage(rawSurfvindsMeasurements);
                    surfvindHistory.add(average);
                    rawSurfvindsMeasurements.clear();
                }
                rawSurfvindsMeasurements = new ConcurrentMaxSizeArray<SurfvindStats>();
            }

        }
    }

    static long started = 0;
    static long restarted = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        Helper.appendLog("\n\n\n\nKrypgrundService Started");
        Helper.trackEvent("LoggerService", "Lifecycle", "onStartCommand", started);
        started++;

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

        mWatchdogTime = watchdog_TimeSinceLastOkData = System.currentTimeMillis();
		/*
		if (mConnectTask == null) {
			mConnectTask = new TimerTask() {

				@Override
				public void run() {
					if (!isIOIOConnected && System.currentTimeMillis() - mWatchdogTime > TimeUnit.SECONDS.toMillis(15)) {
						Helper.appendLog("IOIO is not connected, lets restart to try to connect.");
                        Helper.trackEvent("LoggerService","Timer","IOIOConnector thread restarted service",restarted);
                        restarted++;

                    //    KrypgrundsService.this.restart();
					}
				}
			};
			ioioConnectorTimer = new Timer("IOIOConnector");
			ioioConnectorTimer.scheduleAtFixedRate(mConnectTask, TimeUnit.SECONDS.toMillis(14), TimeUnit.SECONDS.toMillis(10));
		} */

        if (mSendDataTask == null) {
            mSendDataTask = new SendDataTask();
            dataSenderTimer = new Timer("DataSender");
            dataSenderTimer.scheduleAtFixedRate(mSendDataTask, timeBetweenSendingDataToServer, timeBetweenSendingDataToServer);
        }

        if (mAddToHistoryTask == null) {
            mAddToHistoryTask = new AddDataTask();
            addToHistoryTimer = new Timer("AddToHistory");
            addToHistoryTimer.scheduleAtFixedRate(mAddToHistoryTask, timeBetweenAddToHistory, timeBetweenAddToHistory);
        }
        if (mPreventIOIOHWLockTask != null) {
            mPreventIOIOHWLockTask = new PreventIOIOHWLockTask();
            preventIOIOHWLockTimer = new Timer("preventIOIOHWLockTimer");
            preventIOIOHWLockTimer.scheduleAtFixedRate(mPreventIOIOHWLockTask, TimeUnit.DAYS.toMillis(1), TimeUnit.DAYS.toMillis(1));
        }

        Helper.appendLog("ServiceMode =" + serviceMode.toString());
        Helper.appendLog("TimeBetweenSendingDataToServer = " + timeBetweenSendingDataToServer);
        Helper.appendLog("TimeBetweenReading = " + timeBetweenReading);

        return result;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        public KrypgrundsService getService() {
            return KrypgrundsService.this;
        }
    }


    public void updateSettings(String prefName) {

        SharedPreferences prefs;
        if (prefName != null && !prefName.isEmpty()) preferenceFileName = prefName;
        prefs = getSharedPreferences(preferenceFileName, MODE_PRIVATE);

        String type = prefs.getString(LOGGER_MODE, SURFVIND);
        if (type.equalsIgnoreCase(SURFVIND))
            serviceMode = ServiceMode.Survfind;
        else if (type.equalsIgnoreCase(KRYPGRUND))
            serviceMode = ServiceMode.Krypgrund;
        timeBetweenReading = prefs.getLong(MEASUREMENT_DELAY_MS, TimeUnit.SECONDS.toMillis(2));
        if (timeBetweenReading == 0) {
            timeBetweenReading = TimeUnit.SECONDS.toMillis(2);
        }

        timeBetweenSendingDataToServer = prefs.getLong(SEND_TO_SERVER_DELAY_MS, TimeUnit.MINUTES.toMillis(5));
        if (timeBetweenSendingDataToServer == 0) {
            timeBetweenSendingDataToServer = TimeUnit.MINUTES.toMillis(5);
        }

        timeBetweenAddToHistory = TimeUnit.SECONDS.toMillis(30);

        Helper.appendLog("Settings refreshed in Service:");
        Helper.appendLog("ServiceMode =" + serviceMode.toString());
        Helper.appendLog("TimeBetweenSendingDataToServer = " + timeBetweenSendingDataToServer);
        Helper.appendLog("TimeBetweenReading = " + timeBetweenReading);

        if (dataSenderTimer != null) {
            dataSenderTimer.cancel();
            dataSenderTimer = new Timer("DataSenderTimer");
            dataSenderTimer.scheduleAtFixedRate(new SendDataTask(), timeBetweenSendingDataToServer, timeBetweenSendingDataToServer);
        }
        if (addToHistoryTimer != null) {
            addToHistoryTimer.cancel();
            addToHistoryTimer = new Timer("AddToHistoryTimer");
            addToHistoryTimer.scheduleAtFixedRate(new AddDataTask(), timeBetweenAddToHistory, timeBetweenAddToHistory);
        }

    }

    public void setForceFan(boolean on) {
        if (helper != null) {
            helper.ControlFan(on);
        }
    }

    StatusOfService status = new StatusOfService();

    public StatusOfService getStatus() {


        if (serviceMode == ServiceMode.Survfind) {
            synchronized (KrypgrundsService.this) {
                int pos = rawSurfvindsMeasurements.size() - 1;
                if (pos >= 0) {
                    SurfvindStats oneReading = rawSurfvindsMeasurements.getMostRecentlyAddedObject();
                    status.windDirection = (int) oneReading.windDirectionAvg;
                    status.windSpeed = oneReading.windSpeedAvg;
                    status.analogInput = oneReading.windDirectionAvg * 3.3f / 360f;
                    status.voltage = oneReading.batteryVoltage;
                    status.moistureInne = oneReading.firstExternalHumidity;
                    status.temperatureInne = oneReading.firstExternalTemperature;
                    status.rain = oneReading.rainFall;
                    status.airpreassure = (int) oneReading.airPressure;
                }
            }
            if (surfvindHistory != null)
                status.historySize = surfvindHistory.size();
            if (rawSurfvindsMeasurements != null)
                status.readingSize = rawSurfvindsMeasurements.size();
        } else {
            synchronized (KrypgrundsService.this) {
                int pos = rawKrypgrundMeasurements.size() - 1;
                if (pos >= 0) {
                    KrypgrundStats oneReading = rawKrypgrundMeasurements.getMostRecentlyAddedObject();
                    status.voltage = oneReading.batteryVoltage;
                    status.moistureInne = oneReading.moistureInne;
                    status.temperatureInne = oneReading.temperatureInne;
                    status.moistureUte = oneReading.moistureUte;
                    status.temperatureUte = oneReading.temperatureUte;
                    //status.rain = oneReading.;
                    //status.airpreassure = (int) oneReading.airPressure;
                }
            }
            if (krypgrundHistory != null)
                status.historySize = krypgrundHistory.size();
            if (rawKrypgrundMeasurements != null)
                status.readingSize = rawKrypgrundMeasurements.size();
        }
        status.statusMessage = debugText;
        status.timeForLastSendData = timeForLastSendData;
        status.timeBetweenSendingDataToServer = timeBetweenSendingDataToServer;
        status.timeForLastAddToHistory = timeForLastAddToHistory;
        status.timeBetweenAddToHistory = timeBetweenAddToHistory;
        status.timeBetweenReading = timeBetweenReading;
        status.timeForLastFanControl = timeForLastFanControl;
        status.deviceId = id;
        status.isIOIOConnected = isIOIOConnected;

        return status;
    }
}
