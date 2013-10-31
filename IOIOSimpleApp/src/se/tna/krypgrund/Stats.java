package se.tna.krypgrund;

import org.json.JSONObject;

import android.text.format.Time;

public abstract class Stats{
	public Time time;
	public Stats(){
		time = new Time();
		time.setToNow();
	}
	public abstract JSONObject getJSON();
	
}