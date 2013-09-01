package ioio.examples.simple;

import ioio.examples.simple.Helper.SensorType;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import android.telephony.TelephonyManager;

public class KrypgrundsService extends IOIOService {
	public Stats data;
	int delay = 0;
	int historyDelay = 0;
	final int TIME_BETWEEN_ON_OFF = 2 * 5; //5 minutes - each itteration is 30 sec.

	private final IBinder mBinder = new MyBinder();
	private boolean debugMode = false;
	private Stats debugStats = null;
	private boolean forceSendData = false;
	ArrayList<Stats> measurements = new ArrayList<Stats>();
	public String debugText = "";
	public String statusText = "";
	boolean isInitialized = false;
	
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
			}

			@Override
			public void setup() throws ConnectionLostException, InterruptedException {
			
				initialize();
			}

			private void initialize() {
				helper = new Helper(ioio_);
				helper.SetupGpioChip();

				TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				id = telephonyManager.getDeviceId();
				isInitialized = true;
			}
			

			@Override
			public void loop(){
				try {
					
					if (!isInitialized)
					{
						initialize();
					}
					
					Stats temp = new Stats();

					if (debugMode) {
						if (debugStats != null) {
							temp = debugStats;
						}
					} else {
						temp.temperatureUte = helper.GetTemperature(SensorType.SensorUte);
						temp.temperatureInne = helper.GetTemperature(SensorType.SensorInne);
						temp.moistureUte = helper.GetMoisture(SensorType.SensorUte, temp.temperatureUte);
						temp.moistureInne = helper.GetMoisture(SensorType.SensorInne, temp.temperatureInne);
					}

					data = temp;

					measurements.add(temp);

					historyDelay++;
					if (historyDelay > 30) {

						Stats total = new Stats();

						for (Stats stat : measurements) {
							total.moistureInne += stat.moistureInne;
							total.moistureUte += stat.moistureUte;
							total.temperatureInne += stat.temperatureInne;
							total.temperatureUte += stat.temperatureUte;
						}
						total.moistureInne /= (float) measurements.size();
						total.moistureUte /= (float) measurements.size();
						total.temperatureInne /= (float) measurements.size();
						total.temperatureUte /= (float) measurements.size();

						//total = temp;
						/*y = 4.632248129 e6.321315927á10-2 x
						y=max fukt i gram/m3 */
						total.absolutFuktUte = (float) (4.632248129 * (Math.expm1(0.06321315927 * total.temperatureUte) + 1));
						total.absolutFuktInne = (float) (4.632248129 * (Math.expm1(0.06321315927 * total.temperatureInne) + 1));

						total.absolutFuktInne = total.moistureInne * total.absolutFuktInne;
						total.absolutFuktUte = total.moistureUte * total.absolutFuktUte;
						statusText = "AInne: " + String.format("%.2f", total.absolutFuktInne) + " AUte: "
								+ String.format("%.2f", total.absolutFuktUte);

						ControlFan(total);
						total.fanOn = helper.IsFanOn();
						history.add(total);
						historyDelay = 0;
						String res = helper.SendDataToServer(history, forceSendData, id);

						measurements = new ArrayList<Stats>();
						debugText = res;
					} else {
						String text = "HSize=" + history.size() + " Succ=" + Boolean.toString(helper.GetSendSuccess()) + " fDelay:"
								+ Integer.toString(helper.GetFailureDelay());
						debugText = text;
					}
					Thread.sleep(1000);

				} catch (Exception e) {
					data.absolutFuktInne=0;
					data.absolutFuktUte=0;
					data.moistureInne=0;
					data.moistureUte=0;
					data.temperatureInne=0;
					data.temperatureUte=0;
					helper.ControlFan(0, clockwise); //will not work if connection Lost exception. 
				}

			}
			public void ControlFan(Stats data) {
				if (delay > 0)
					delay--;
				//Dont start fans if inside temp is close to freezeingpoint.
				if (data.temperatureInne < 0.9) {
					helper.ControlFan(Helper.FanStop, true);
				} else {
					if (data.absolutFuktUte + 5 < data.absolutFuktInne && delay == 0 && data.temperatureInne > 1) { //Start the fans!!
						delay = TIME_BETWEEN_ON_OFF;
						float speed = (float) (Helper.FanMaxSpeed);///*seekBar_.getProgress()/99);
						helper.ControlFan((int) speed, clockwise);
					} else if (data.absolutFuktUte * 1.05 > data.absolutFuktInne && delay == 0) {
						delay = TIME_BETWEEN_ON_OFF;
						helper.ControlFan(Helper.FanStop, true);
					}
				}
			}
		};

	}
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);

	}
	private Helper helper;
	private String id = "";
	boolean clockwise = true;
	@SuppressLint("UseSparseArrays")
	ArrayList<Stats> history = new ArrayList<Stats>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
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
		if(on)
		{
			helper.ControlFan(Helper.FanMaxSpeed, true);
		}
		else
		{
			helper.ControlFan(Helper.FanStop, true);
		}
	}
}
