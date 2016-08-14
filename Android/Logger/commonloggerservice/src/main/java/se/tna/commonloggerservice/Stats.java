package se.tna.commonloggerservice;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public abstract class Stats{
	public String time;
	public float batteryVoltage = 0;
	public float rainFall = 0;
	public float airPressure;

	public Stats(){
	    DateFormat dateFormatLocal = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        TimeZone t = TimeZone.getDefault();
        dateFormatLocal.setTimeZone(t);
        time = dateFormatLocal.format(new Date(System.currentTimeMillis()));
  	}
	public abstract JSONObject getJSON();
	
}