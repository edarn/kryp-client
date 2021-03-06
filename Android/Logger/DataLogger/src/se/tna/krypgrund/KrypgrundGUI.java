package se.tna.krypgrund;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.util.Timer;
import java.util.TimerTask;

public class KrypgrundGUI extends Activity {
    private TextView textFuktInne;
    private TextView textFuktUte;
    private TextView textTempInne;
    private TextView textTempUte;
    private TextView debugText;
    private TextView initializedText;
    private TextView fanStatus;
    private TextView phoneId;
    private TextView textVoltage;

    private TextView temperatureText;
    private TextView humidText;




    private ToggleButton debugButton;
    private ToggleButton toggleFanButton;

    private KrypgrundsService kryp = null;
    private ServiceConnection mConnection = null;
    private TextView textFanOn;
    private TextView textWindSpeed;
    private TextView textWindDirection;
    private TextView batteryText;

    private ImageView compassImageView;

    private ImageView temperatureImageView;
    private ImageView moistureImageView;

    private int angle = 0;

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, SetupActivity.class));
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (kryp != null) {
            kryp.updateSettings();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound && mConnection != null) {
            unbindService(mConnection);
            serviceBound = false;
            mConnection = null;
        }
    }

    boolean serviceBound = false;

    // SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Helper.appendLog("App started");
        mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                kryp = ((KrypgrundsService.MyBinder) service).getService();
                Toast.makeText(KrypgrundGUI.this, "Connected", Toast.LENGTH_SHORT).show();
                serviceBound = true;
                kryp.updateSettings();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                kryp = null;
                serviceBound = false;
                Toast.makeText(KrypgrundGUI.this, "DisConnected", Toast.LENGTH_SHORT).show();

            }
        };

        setContentView(R.layout.main);

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String id = telephonyManager.getDeviceId();

		/*
         * // debug id = "358848043355882";
		 * 
		 * // WebView myWebView = (WebView) findViewById(R.id.webView); //
		 * myWebView.loadUrl("http://www.surfvind.se/Applet/" + id + //
		 * "/graph_0.png"); // myWebView.setBackgroundColor(0x00000000); WebView
		 * compassView = (WebView) findViewById(R.id.webViewCompass);
		 * compassView.loadUrl("http://www.surfvind.se/Images/" + id +
		 * "_img_compass.png"); compassView.setBackgroundColor(0x00000000);
		 * WebView compassViewBackground = (WebView)
		 * findViewById(R.id.webViewCompassBackground);
		 * compassViewBackground.loadUrl
		 * ("http://www.surfvind.se/Images/ws_compass.png");
		 * compassViewBackground.setBackgroundColor(0x00000000);
		 * 
		 * WebView speedView = (WebView) findViewById(R.id.webViewSpeed);
		 * speedView.loadUrl("http://www.surfvind.se/Images/" + id +
		 * "_img_speed.png"); speedView.setBackgroundColor(0x00000000); WebView
		 * speedViewBackground = (WebView)
		 * findViewById(R.id.webViewSpeedBackground);
		 * speedViewBackground.loadUrl
		 * ("http://www.surfvind.se/Images/ws_speed.png");
		 * speedViewBackground.setBackgroundColor(0x00000000);
		 */

        class GraphViewData implements GraphViewDataInterface {
            private double x;
            private double y;

            GraphViewData(double a, double b) {
                x = a;
                y = b;
            }

            @Override
            public double getX() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public double getY() {
                // TODO Auto-generated method stub
                return 0;
            }

        }
        // init example series data
        GraphViewSeries exampleSeries = new GraphViewSeries(
                new GraphViewData[]{new GraphViewData(1, 2.0d),
                        new GraphViewData(2, 1.5d), new GraphViewData(3, 2.5d),
                        new GraphViewData(4, 1.0d)});

        GraphView graphView = new LineGraphView(this // context
                , "GraphViewDemo" // heading
        );
        graphView.addSeries(exampleSeries); // data

        LinearLayout layout = (LinearLayout) findViewById(R.id.mainContainer);
        layout.addView(graphView);

        textVoltage = (TextView) findViewById(R.id.voltageText);

        textWindSpeed = (TextView) findViewById(R.id.textWindSpeed);
        textWindDirection = (TextView) findViewById(R.id.textWindDirection);

        fanStatus = (TextView) findViewById(R.id.fanStatus);
        debugText = (TextView) findViewById(R.id.debugText);

        textFuktInne = (TextView) findViewById(R.id.textFuktInne);
        textFuktUte = (TextView) findViewById(R.id.textFuktUte);
        textTempInne = (TextView) findViewById(R.id.textTempInne);
        textTempUte = (TextView) findViewById(R.id.textTempUte);
        textFanOn = (TextView) findViewById(R.id.textFanOn);
        initializedText = (TextView) findViewById(R.id.connectedText);
        phoneId = (TextView) findViewById(R.id.phoneId);
        toggleFanButton = (ToggleButton) findViewById(R.id.toggleFanButton);
        debugButton = (ToggleButton) findViewById(R.id.DebugButton1);

        temperatureText = (TextView) findViewById(R.id.temperatureText);
        humidText = (TextView) findViewById(R.id.humidText);
        batteryText = (TextView) findViewById(R.id.batteryText);

        debugButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggleFanButton.setVisibility(View.VISIBLE);
                } else {
                    toggleFanButton.setVisibility(View.GONE);
                }
            }
        });

        Intent service = new Intent(this, KrypgrundsService.class);
        this.startService(service);
        bindService(new Intent(this, KrypgrundsService.class), mConnection, Context.BIND_AUTO_CREATE);
        Timer timer = new Timer();
        TimerTask t = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (null != kryp) {
                    updateUI();
                    if (debugButton.isChecked()) {
                        kryp.setForceFan(toggleFanButton.isChecked());
                    }
                }
            }
        };


        timer.scheduleAtFixedRate(t, 0, 5000);

        compassImageView = (ImageView) findViewById(R.id.arrow);
        temperatureImageView = (ImageView) findViewById(R.id.temp_arrow);
        moistureImageView = (ImageView) findViewById(R.id.moist_arrow);



		Timer te = new Timer();

		TimerTask tu = new TimerTask() {
			
			@Override
			public void run() {
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
                        temperatureImageView.setRotation(angle);
                        moistureImageView.setRotation(angle);
                        i+=1;
					}
				});
			}
		};
		
		te.scheduleAtFixedRate(tu, 1,50);

	    
	/*    
	    Gauge radial = GaugeBuilder.create()
                .prefWidth(500)
                .prefHeight(500)
                .gaugeType(GaugeBuilder.GaugeType.RADIAL)
                .frameDesign(Gauge.FrameDesign.STEEL)
                .backgroundDesign(Gauge.BackgroundDesign.DARK_GRAY)
                .lcdDesign(LcdDesign.STANDARD_GREEN)
                .lcdDecimals(2)
                .lcdValueFont(Gauge.LcdFont.LCD)
                .pointerType(Gauge.PointerType.TYPE14)
                .valueColor(ColorDef.RED)
                .knobDesign(Gauge.KnobDesign.METAL)
                .knobColor(Gauge.KnobColor.SILVER)
                .sections(new Section[] {
                    new Section(0, 37, Color.LIME),
                    new Section(37, 60, Color.YELLOW),
                    new Section(60, 75, Color.ORANGE})
                .sectionsVisible(true)
                .areas(new Section[] {new Section(75, 100, Color.RED)})
                .areasVisible(true)
                .markers(new Marker[] {
                    new Marker(30, Color.MAGENTA),
                    new Marker(75, Color.AQUAMARINE)})
                .markersVisible(true)
                .threshold(40)
                .thresholdVisible(true)
                .glowVisible(true)
                .glowOn(true)
                .trendVisible(true)
                .trend(Gauge.Trend.UP)
                .userLedVisible(true)
                .bargraph(true)
                .title("Temperature")
                .unit("C")
                .build();
	    */
    }

    int i = 0;

    @Override
    protected void onStart() {

        super.onStart();

        LinearLayout l = (LinearLayout) findViewById(R.id.weatherStationContainer);
        LinearLayout ll = (LinearLayout) findViewById(R.id.crawlspaceContainer);
        SharedPreferences preferences = getSharedPreferences("TNA_Sensor", Activity.MODE_PRIVATE);
        int type = preferences.getInt(SetupActivity.SENSOR_TYPE_RADIO, KrypgrundsService.KRYPGRUND);
        if (type == KrypgrundsService.KRYPGRUND) {
            l.setVisibility(View.GONE);
            ll.setVisibility(View.VISIBLE);
        } else {
            l.setVisibility(View.VISIBLE);
            ll.setVisibility(View.GONE);
        }
    }

    private int getMoistureAngle(float moisture) {
        int angle = 0;
        angle = (int) ((moisture / 100f) * 135 * 2);
        angle -= 135;
        return angle;
    }

    private int getTempAngle(float temperature) {
        int angle = 0;
        angle = (int) ((temperature / 100f) * 120 * 2);
        angle -= 120;
        return angle;
    }

    private void updateUI() {
        if (null != kryp) {
            final StatusOfService status = kryp.getStatus();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (status.fanOn) {
                        fanStatus.setText("Fan is RUNNING");
                    } else {
                        fanStatus.setText("Fan is OFF");

                    }

                 //   textWindDirection.setText(status.windDirection);
                    textWindSpeed.setText(String.format("%.2f", status.windSpeed));
                    temperatureText.setText(String.format("%.0f", status.temperatureInne));
                    humidText.setText(String.format("%.1f", status.moistureInne));
                    compassImageView.setRotation(status.windDirection);
                    batteryText.setText(String.format("%.1f", status.voltage));

                    textTempUte.setText("Temp Ute: " + String.format("%.2f", status.temperatureUte));
                    textTempInne.setText("Temp Inne: " + String.format("%.2f", status.temperatureInne));
                    textFuktUte.setText("Fukt Ute: " + String.format("%.2f", status.moistureUte));
                    textFuktInne.setText("Fukt Inne: " + String.format("%.2f", status.moistureInne));
                    textFanOn.setText("Fan On =" + status.fanOn);
                    textVoltage.setText(String.format("%.2f", status.voltage));

                    // Is ioio chip initialized etc
                    initializedText.setText(status.statusMessage);
                    phoneId.setText("IMEI:" + status.deviceId);
                    StringBuilder sb = new StringBuilder();
                    sb.append("HistorySize: ");
                    sb.append(status.historySize);
                    sb.append("\n");
                    sb.append("ReadingSize: ");
                    sb.append(status.readingSize);
                    sb.append("\n");

                    sb.append("TimeOfCreation: ");

                    CharSequence cs = DateFormat.format("yyyy-MM-dd - kk:mm:ss", status.timeOfCreation);
                    sb.append(cs.toString());

                    sb.append("\nTimeSinceLastSend: ");
                    cs = DateFormat.format("yyyy-MM-dd - kk:mm:ss", status.timeForLastSendData);
                    sb.append(cs.toString());

                    debugText.setText(sb.toString());
                    // debugText.setText(status.)
                }
            });
        }
    }

}
