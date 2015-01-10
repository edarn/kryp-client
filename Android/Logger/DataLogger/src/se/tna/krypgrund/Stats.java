package se.tna.krypgrund;

import org.json.JSONObject;

import android.text.format.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public abstract class Stats{
	public String time;
	public float batteryVoltage = 0;
	public int temperature = 0;
	public Stats(){
	    DateFormat dateFormatLocal = new SimpleDateFormat("yyyyMMddTkkmmss");
        TimeZone t = TimeZone.getDefault();
        dateFormatLocal.setTimeZone(t);
        time = dateFormatLocal.format(new Date(System.currentTimeMillis()));
  	}
	public abstract JSONObject getJSON();
	
}