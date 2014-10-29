package com.surfvindClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import com.surfvindClient.logic.Load;
import com.surfvindClient.logic.SensorsReader;

/**
 * Main class for GUI presentation
 * 
 * @author Erik
 * 
 */
public class SurfvindClient extends Activity {

	public static final int LOAD_FINISHED = 0;
	public static final int INIT_FINISHED = 1;

	/* Spinners */
	private Spinner intervalSpinner;
	private Spinner locationSpinner;

	/* Images */
	private ImageView windDirNeedle;
	private ImageView windSpeedNeedle;
	private WebView windSpeedGraph;

	/* Sensor reader */
	private SensorsReader sensorsReader;

	/* Load class, handles loading data according to settings */
	private Load loader;
	private boolean isLoading;

	/* Handler for the loading parts not to hang the app */
	private MyHandler handler;

	/* Progress dialog when loading */
	private ProgressDialog progressDialog;

	/* Menu options */
	private static final int MENU_QUIT = 2;

	private boolean started = false;

	private static final String DOMAIN = "http://www.surfvind.se/";

	/**
	 * Setup the spinners. Locate them from the xml view and populate them. The
	 * content is parsed from the website.
	 */
	private void initSpinners() {
		/* Init the spinners... */
		intervalSpinner = (Spinner) findViewById(R.id.IntervalSpinner);
		locationSpinner = (Spinner) findViewById(R.id.LocationSpinner);

		/* ...finally add some listeners */
		locationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				loadContent();

			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});

		intervalSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				loadContent();

			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});
	}

	private void populateSpinners() {
		/* ...and populate them... */
		String[] intervalItems = sensorsReader.getIntervals();
		ArrayAdapter<String> intervalAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, intervalItems);
		intervalAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		intervalSpinner.setAdapter(intervalAdapter);

		String[] locationItems = sensorsReader.getNames();
		ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, locationItems);
		locationAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		locationSpinner.setAdapter(locationAdapter);

		/* ...set selected items as per requirement... */
		// The interval shall default to 1 day, so we need to find it
		int i = 0;
		while (i < intervalItems.length && !intervalItems[i].equals("1 Day")) {
			i++;
		}
		if (i >= intervalItems.length) {
			// did not find our default value, set index 0
			intervalSpinner.setSelection(0);
		} else {
			intervalSpinner.setSelection(i);
		}

		// Location shall be lund
		i = 0;
		while (i < locationItems.length && !locationItems[i].equals("Lund")) {
			i++;
		}
		if(i >= locationItems.length) {
			locationSpinner.setSelection(0);
		} else {
			locationSpinner.setSelection(i);
		}
	}
	
	/**
	 * Create the drawables we want from the web, i.e. the graphs and the
	 * needles
	 */
	private void initDrawables() {
		windSpeedGraph = (WebView) findViewById(R.id.WindSpeeedGraph);
		windSpeedNeedle = (ImageView) findViewById(R.id.WindSpeedNeedle);
		windDirNeedle = (ImageView) findViewById(R.id.CompassNeedle);

		String url = Load.imagePath
		+ sensorsReader.getImei((String) locationSpinner
				.getSelectedItem()) + "/graph_" + intervalSpinner.getSelectedItemPosition() + ".png";
		
		windSpeedGraph.loadUrl(url);
	}

	/**
	 * Load images
	 */
	private void loadContent() {
		if (isLoading) {
			return;
		}

		/*
		 * Lock so we don't fire away more than one thread We will unlock when
		 * the thread returns
		 */
		isLoading = true;

		// build the setting string
		String setting;
		String imei;

		setting = settingsBuilder();
		imei = sensorsReader
				.getImei((String) locationSpinner.getSelectedItem());
		loader.load(handler, setting, imei);

		if (progressDialog != null) {
			// just in case
			progressDialog.dismiss();
		}

		progressDialog = ProgressDialog.show(this, "Loading...",
				"Loading images");
	}

	/**
	 * @return the string to load according to the spinner info
	 */
	private String settingsBuilder() {
		String settings;
		String sensor;
		String duration;

		sensor = sensorsReader.getImei((String) locationSpinner
				.getSelectedItem());
		duration = String.valueOf(intervalSpinner.getSelectedItemPosition());

		settings = "GenGraphs.aspx?location=";
		settings += sensor;
		settings += "&duration=";
		settings += duration;

		return settings;
	}

	/**
	 * Show the new images
	 */
	private void updateDrawables() {
		windSpeedNeedle.setImageDrawable(loader.getWindSpeedMeter());
		windDirNeedle.setImageDrawable(loader.getWindDirectionMeter());

		String url = Load.imagePath + "Applet/"
		+ sensorsReader.getImei((String) locationSpinner
				.getSelectedItem()) + "/graph_" + intervalSpinner.getSelectedItemPosition() + ".png";
		
		windSpeedGraph.loadUrl(url);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Make it full screen */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		if (!started) {
			waitingDialog(true);
			
			initSpinners();
			handler = new MyHandler();
			
			new LoadThread().start();

			started = true;
		}
	}
	
	private void init() {
		populateSpinners();
		initDrawables();
		isLoading = false;

		waitingDialog(false);
		
		/* Load the images based on the spinner values */
		loadContent();
	}
	
	private void waitingDialog(boolean show) {
		if(show) {
			progressDialog = ProgressDialog.show(this, "Loading...",
			"Please wait");
		} else {
			if(progressDialog != null) {
				progressDialog.dismiss();
			}
		}
	}

	/**
	 * When the options menu is created
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_QUIT, 0, "Quit");
		return true;
	}

	/**
	 * Handle menu items selection
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_QUIT: {
			System.exit(0);
		}
		}

		return true;
	}

	/**
	 * Handler to update the images. Is called when the load thread is done
	 */
	private class MyHandler extends Handler {

		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case LOAD_FINISHED: {
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				updateDrawables();
				isLoading = false;
				break;
			}
			case INIT_FINISHED: {
				init();
				break;
			}
			}
		}
	}
	
	private class LoadThread extends Thread {
		
		public void run() {
			initSpinners();

			/* Set up the sensors reader and get all data */
			sensorsReader = new SensorsReader();
			loader = new Load(DOMAIN);
			
			Message msg = new Message();
			msg.arg1 = INIT_FINISHED;
			handler.sendMessage(msg);
		}
	}

}