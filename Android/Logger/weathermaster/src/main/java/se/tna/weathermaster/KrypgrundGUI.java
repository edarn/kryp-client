package se.tna.weathermaster;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import se.tna.commonloggerservice.Helper;
import se.tna.commonloggerservice.KrypgrundsService;
import se.tna.commonloggerservice.StatusOfService;
import se.tna.commonloggerservice.SurfvindStats;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


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
    private TextView textStationName;
    private LinearLayout noConnectionContainer;
    private TextView regnText;
    private TextView lufttryckText;

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
            kryp.updateSettings(SetupActivity.SETTINGS_FILE);
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
        Helper.setupGoogleAnalytics(this);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                kryp = ((KrypgrundsService.MyBinder) service).getService();
                Toast.makeText(KrypgrundGUI.this, "Connected", Toast.LENGTH_SHORT).show();
                serviceBound = true;
                kryp.updateSettings(SetupActivity.SETTINGS_FILE);
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
        */

        textStationName = (TextView) findViewById(R.id.stationName);

        textWindSpeed = (TextView) findViewById(R.id.textWindSpeed);
        textWindDirection = (TextView) findViewById(R.id.textWindDirection);

        debugText = (TextView) findViewById(R.id.debugText);

        textFanOn = (TextView) findViewById(R.id.textFanOn);
        initializedText = (TextView) findViewById(R.id.connectedText);
        phoneId = (TextView) findViewById(R.id.phoneId);
        debugButton = (ToggleButton) findViewById(R.id.showDebugButton);

        temperatureText = (TextView) findViewById(R.id.temperatureText);
        humidText = (TextView) findViewById(R.id.humidText);
        batteryText = (TextView) findViewById(R.id.batteryText);

        lufttryckText = (TextView) findViewById(R.id.lufttryckText);
        regnText = (TextView) findViewById(R.id.regnText);

        ImageView settingsButton = (ImageView) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(KrypgrundGUI.this, SetupActivity.class));
            }
        });


        final LinearLayout debugContainer = (LinearLayout) findViewById(R.id.debugContainer);
        if(debugContainer != null) {
            debugContainer.setVisibility(View.GONE);
        }

        noConnectionContainer = (LinearLayout) findViewById(R.id.noConnectionContainer);


        if(debugButton != null) {
            debugButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        debugContainer.setVisibility(View.VISIBLE);
                    } else {
                        debugContainer.setVisibility(View.GONE);
                    }
                }
            });
        }


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
                }
            }
        };


        timer.scheduleAtFixedRate(t, 1000, 5000);

        compassImageView = (ImageView) findViewById(R.id.arrow);
/*
        final ArrayList<SurfvindStats> list = new ArrayList<SurfvindStats>();

        SurfvindStats s = new SurfvindStats();
        s.windSpeedMin = 1.2f;
        s.windSpeedAvg = 2.3f;
        s.windSpeedMax = 3.4f;
        s.windDirectionMin = 4.5f;
        s.windDirectionAvg = 5.6f;
        s.windDirectionMax = 6.7f;

        s.onBoardHumidity = 7.8f;
        s.onBoardTemperature = 8.9f;

        s.firstExternalHumidity = 9.10f;
        s.firstExternalTemperature = 10.11f;

        s.rainFall = 11.12f;
        s.airPressure = 123;
        s.rainFall=12.3f;
        
        list.add(s);
        s = new SurfvindStats();
        s.windSpeedMin = 11.2f;
        s.windSpeedAvg = 12.3f;
        s.windSpeedMax = 13.4f;
        s.windDirectionMin = 14.5f;
        s.windDirectionAvg = 15.6f;
        s.windDirectionMax = 16.7f;

        s.onBoardHumidity = 17.8f;
        s.onBoardTemperature = 18.9f;

        s.firstExternalHumidity = 19.10f;
        s.firstExternalTemperature = 20.11f;

        s.rainFall = 21.12f;
        s.airPressure = 1123;

        list.add(s);

        new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Log.d("TNA",Helper.SendDataToServer(list, KrypgrundsService.ServiceMode.Survfind));
                return null;
            }
        }.execute();

        */

    }

    @Override
    protected void onStart() {

        super.onStart();

        //Launch setup activty
        SharedPreferences preferences = getSharedPreferences(SetupActivity.SETTINGS_FILE, Activity.MODE_PRIVATE);

        // String type = preferences.getString(SetupActivity.SENSOR_TYPE_RADIO, KrypgrundsService.KRYPGRUND);
        String name = preferences.getString(SetupActivity.STATION_NAME, "");
        if (name != null && name.isEmpty())
        {

            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

// 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("It appears you are a new user, launching setup")
                    .setTitle("New user?");

            // Add the buttons
            builder.setPositiveButton("OK!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(new Intent(KrypgrundGUI.this, SetupActivity.class));
                }
            });
            builder.setNegativeButton("Nope!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
// 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();
            dialog.show();

        }
        textStationName.setText(name);
    }


    private void updateUI() {
        if (null != kryp) {
            final StatusOfService status = kryp.getStatus();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //   textWindDirection.setText(status.windDirection);
                    textWindSpeed.setText(String.format("%.1f", status.windSpeed));
                    temperatureText.setText(String.format("%.1f", status.temperatureInne));
                    humidText.setText(String.format("%.1f", status.moistureInne));
                    compassImageView.setRotation(status.windDirection);
                    batteryText.setText(String.format("%.1f", status.voltage));
                    regnText.setText(String.format("%.1f", status.rain));
                    lufttryckText.setText(String.valueOf(status.airpreassure));

                    /*
                    textTempUte.setText("Temp Ute: " + String.format("%.2f", status.temperatureUte));
                    textTempInne.setText("Temp Inne: " + String.format("%.2f", status.temperatureInne));
                    textFuktUte.setText("Fukt Ute: " + String.format("%.2f", status.moistureUte));
                    textFuktInne.setText("Fukt Inne: " + String.format("%.2f", status.moistureInne));
                    textFanOn.setText("Fan On =" + status.fanOn);
                    textVoltage.setText(String.format("%.2f", status.voltage));
*/
                    // Is ioio chip initialized etc
                    if (status.isIOIOConnected)
                        initializedText.setText("Controlunit connected OK");
                    else
                        initializedText.setText("No controlunit detected");

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

                    sb.append("\n StatusMessage:\n" +status.statusMessage);
                    debugText.setText(sb.toString());

                    if (noConnectionContainer != null) {
                        if (status.isIOIOConnected) {
                            noConnectionContainer.setVisibility(View.GONE);
                        } else {
                            noConnectionContainer.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
    }

}
