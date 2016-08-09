package se.tna.crawlspacemonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.util.CircularArray;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import se.tna.commonloggerservice.Helper;
import se.tna.commonloggerservice.KrypgrundStats;
import se.tna.commonloggerservice.KrypgrundsService;
import se.tna.commonloggerservice.StatusOfService;


public class CrawlspaceGUI extends Activity {

    private TextView textStationName;

    private TextView textFuktInne;
    private TextView textFuktUte;
    private TextView textFuktExtra;

    private TextView textTempInne;
    private TextView textTempUte;
    private TextView textTempExtra;

    private TextView textVoltage;

    private TextView debugText;
    private TextView initializedText;
    private TextView fanStatus;
    private TextView phoneId;

    //Remove??
    private TextView textFanOn;
    private TextView batteryText;
    // END

    private ToggleButton debugButton;
    private ToggleButton toggleFanButton;

    private KrypgrundsService loggerService = null;
    private ServiceConnection mConnection = null;

    private LinearLayout noConnectionContainer;
    private boolean serviceBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_crawlspace_gui);

        Helper.appendLog("App started");
//        Helper.setupGoogleAnalytics(this);

        //Remove title bar


        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String id = telephonyManager.getDeviceId();

        //region CHART REGION


        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);


        //endregion


        textStationName = (TextView) findViewById(R.id.stationName);

        debugText = (TextView) findViewById(R.id.debugText);

        textFanOn = (TextView) findViewById(R.id.textFanOn);
        initializedText = (TextView) findViewById(R.id.connectedText);
        phoneId = (TextView) findViewById(R.id.phoneId);
        debugButton = (ToggleButton) findViewById(R.id.showDebugButton);
        batteryText = (TextView) findViewById(R.id.batteryText);


          textFuktInne = (TextView) findViewById(R.id.textHumidIn);
          textFuktUte= (TextView) findViewById(R.id.textHumidOut);
          textFuktExtra= (TextView) findViewById(R.id.textHumidExtra);

         textTempInne= (TextView) findViewById(R.id.textTempIn);
         textTempUte = (TextView) findViewById(R.id.textTempOut);
         textTempExtra=  (TextView) findViewById(R.id.textTempExtra);
        ImageView settingsButton = (ImageView) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CrawlspaceGUI.this, SetupActivity.class));
            }
        });


        final LinearLayout debugContainer = (LinearLayout) findViewById(R.id.debugContainer);


        noConnectionContainer = (LinearLayout) findViewById(R.id.noConnectionContainer);

        if (debugContainer != null) { //We should check if in debug mode
            debugContainer.setVisibility(View.VISIBLE);

            if (debugButton != null) {
                debugButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

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
        }

        mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                loggerService = ((KrypgrundsService.MyBinder) service).getService();
                Toast.makeText(CrawlspaceGUI.this, "Connected to service", Toast.LENGTH_SHORT).show();
                serviceBound = true;
                loggerService.updateSettings(SetupActivity.SETTINGS_FILE);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                loggerService = null;
                serviceBound = false;
                Toast.makeText(CrawlspaceGUI.this, "Disconnected from service", Toast.LENGTH_SHORT).show();
            }
        };

        Intent service = new Intent(this, KrypgrundsService.class);
        this.startService(service);
        bindService(new Intent(this, KrypgrundsService.class), mConnection, Context.BIND_AUTO_CREATE);
        Timer timer = new Timer();
        TimerTask t = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (null != loggerService) {
                    updateUI();
                }
            }
        };

        timer.scheduleAtFixedRate(t, 1000, 5000);


        final ArrayList<KrypgrundStats> list = new ArrayList<>();

//        //region DEBUG DATA
//        KrypgrundStats s = new KrypgrundStats();
//        s.absolutFuktInne = 1.2f;
//        s.absolutFuktUte = 2.3f;
//        s.absolutFuktExtra = 9.9f;
//        s.moistureInne = 3.4f;
//        s.moistureUte = 4.5f;
//        s.moistureExtra = 9.9f;
//        s.batteryVoltage = 15.6f;
//        s.fanOn = true;
//
//        s.temperatureInne = 7.8f;
//        s.temperatureUte = 8.9f;
//        s.temperatureExtra = 9.9f;
//
//        list.add(s);
//        s = new KrypgrundStats();
//        s.absolutFuktInne = 1.2f;
//        s.absolutFuktUte = 2.3f;
//        s.absolutFuktExtra = 9.9f;
//        s.moistureInne = 3.4f;
//        s.moistureUte = 4.5f;
//        s.moistureExtra = 9.9f;
//        s.batteryVoltage = 15.6f;
//        s.fanOn = true;
//
//        s.temperatureInne = 7.8f;
//        s.temperatureUte = 8.9f;
//        s.temperatureExtra = 9.9f;
//        list.add(s);
//
//        new AsyncTask<Void, Void, Void>() {
//
//            @Override
//            protected Void doInBackground(Void... params) {
//                Log.d("TNA", Helper.SendDataToServer(list, KrypgrundsService.ServiceMode.Krypgrund));
//                return null;
//            }
//        }.execute();
//
        //endregion


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_crawlspace_gui, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SetupActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loggerService != null) {
            loggerService.updateSettings(SetupActivity.SETTINGS_FILE);
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


    @Override
    protected void onStart() {

        super.onStart();

        //Launch setup activty
        SharedPreferences preferences = getSharedPreferences("TNA_Crawlspace_Settings", Activity.MODE_PRIVATE);

        String name = preferences.getString(SetupActivity.STATION_NAME, "");
        if (name == null || name.isEmpty())
        {

            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("It appears you are a new user, launching setup")
                    .setTitle("New user?");

            // Add the buttons
            builder.setPositiveButton("OK!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(new Intent(CrawlspaceGUI.this, SetupActivity.class));
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

    ChartDataPoints dataPoints = new ChartDataPoints(200);

    private void updateUI() {
        if (null != loggerService) {
            final StatusOfService status = loggerService.getStatus();
            //dataPoints.insertData(status);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textFuktExtra.setText(String.format("%.1f", status.moistureExtra));
                    textFuktInne.setText(String.format("%.1f", status.moistureInne));
                    textFuktUte.setText(String.format("%.1f", status.moistureUte));

                    textTempExtra.setText(String.format("%.1f", status.temperatureExtra));
                    textTempInne.setText(String.format("%.1f", status.temperatureInne));
                    textTempUte.setText(String.format("%.1f", status.temperatureUte));

                    batteryText.setText(String.format("%.1f", status.voltage));
                   // textFanOn.setText("Fan On =" + status.fanOn);

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

                    debugText.setText(sb.toString());
                    debugText.setText(status.statusMessage);
                    //debugText.setText(status.statusMessage);

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


