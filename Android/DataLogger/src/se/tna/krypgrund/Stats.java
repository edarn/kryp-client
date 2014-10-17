package se.tna.krypgrund;

import org.json.JSONObject;

import android.text.format.Time;

public abstract class Stats{
	public Time time;
	public float batteryVoltage = 0;
	public int temperature = 0;
	public Stats(){
		time = new Time();
		time.setToNow();
	}
	public abstract JSONObject getJSON();
	
}