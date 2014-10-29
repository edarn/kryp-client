package com.surfvindClient.logic;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public final class SensorsReader {

	private Hashtable<String, String> devices;
	private Vector<String> keys;

	private Hashtable<Integer, String> interval;

	private String sensorInfo;

	private Vector<String> findAllNames() {
		String current;
		Vector<String> ret;
		int index;

		index = 0;
		ret = new Vector<String>();
		while (!(current = getName(index)).equals("")) {
			ret.add(current);
			index = sensorInfo.indexOf(current);
		}

		return ret;
	}

	private String getName(int startIndex) {
		String subString;
		int length;
		int nameStart;

		subString = sensorInfo.substring(startIndex);

		if (!subString.contains("Name:")) {
			return "";
		}

		nameStart = subString.indexOf("Name:") + 5;

		length = 0;
		while (subString.charAt(nameStart + length) != ' ') {
			length++;
		}

		return subString.substring(nameStart, nameStart + length);
	}

	private String getInterval(int i) {
		int start;
		String toFind, interval;

		toFind = i + "=";
		start = sensorInfo.indexOf(toFind);
		if (start == -1) {
			return "";
		}
		start += 3; // Go past the first snuffe (")
		interval = "";
		while (sensorInfo.charAt(start) != '"') {
			interval += sensorInfo.charAt(start++);
		}

		return interval;
	}

	private String getImeiFor(String name) {
		int nameStart;
		int imeiStart;
		int length;

		if (!sensorInfo.contains(name)) {
			return "-1";
		}

		nameStart = sensorInfo.indexOf(name);
		imeiStart = sensorInfo.indexOf("Imei:", nameStart) + 5;

		length = 0;
		// while next char is a number
		while (sensorInfo.charAt(imeiStart + length) >= 48
				&& sensorInfo.charAt(imeiStart + length) <= 57) {
			length++;
		}

		return sensorInfo.substring(imeiStart, imeiStart + length);
	}

	public SensorsReader() {
		init();
	}

	public void init() {
		String info;
		int next;
		/* Get the sensor info */
		HttpParams http = new BasicHttpParams();
		HttpClient client = new DefaultHttpClient(http);
		try {
			HttpResponse respone = client.execute(new HttpGet(
					"http://www.surfvind.se/Android/GetSensors.aspx"));
			InputStream is = respone.getEntity().getContent();
			info = "";
			while ((next = is.read()) != -1) {
				info += Character.toString((char) next);
			}
			is.close();
			client.getConnectionManager().shutdown();
			next = info.indexOf("--END--");
			sensorInfo = info.substring(0, next + 7);
		} catch (IOException ioe) {
			System.err.println("Unable to connect!");
			ioe.printStackTrace();
		}

		/* Get the device names */
		devices = new Hashtable<String, String>();
		/* Get the keys for easy access in the hashtable */
		keys = findAllNames();

		/* Populate the hashtable */
		for (String s : keys) {
			devices.put(s, getImeiFor(s));
		}

		/* Get the intervals */
		interval = new Hashtable<Integer, String>();
		String nextInterval;
		int intervals = 0;
		while (!(nextInterval = getInterval(intervals)).equals("")) {
			interval.put(intervals, nextInterval);
			intervals++;
		}
	}

	public String getImei(String name) {
		
		if (devices.isEmpty()){ return ""; }
		else return devices.get(name);
	}

	public String[] getIntervals() {
		String[] ret;

		ret = new String[interval.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = interval.get(i);
		}

		return ret;
	}

	public String[] getNames() {
		String[] names;
		int i;

		names = new String[keys.size()];
		i = 0;
		for (String s : keys) {
			names[i++] = s;
		}
		return names;
	}
}
