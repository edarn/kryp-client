package com.surfvindClient.cache.unused;

import android.graphics.drawable.Drawable;

public class CacheObject {

	protected CacheObject next;
	private Drawable windDirNeedle;
	private Drawable windSpeedNeedle;
	private Drawable windSpeedGraph;
	private Drawable windDirGraph;

	private int interval, location;

	public static CacheObject create(Drawable windDirNeedle,
			Drawable windSpeedNeedle, Drawable windSpeedGraph,
			Drawable windDirGraph, int interval, int location) {

		if (windDirNeedle != null && windSpeedNeedle != null
				&& windSpeedGraph != null && windDirGraph != null) {
			return new CacheObject(windDirNeedle, windSpeedNeedle,
					windSpeedGraph, windDirGraph, interval, location);
		}

		return null;
	}

	private CacheObject(Drawable windDirNeedle, Drawable windSpeedNeedle,
			Drawable windSpeedGraph, Drawable windDirGraph, int interval,
			int location) {
		this.windDirNeedle = windDirNeedle;
		this.windSpeedNeedle = windSpeedNeedle;
		this.windSpeedGraph = windSpeedGraph;
		this.windDirGraph = windDirGraph;

		this.interval = interval;
		this.location = location;
	}

	public Drawable getWindSpeedNeedle() {
		return windSpeedNeedle;
	}

	public Drawable getWindDirNeedle() {
		return windDirNeedle;
	}

	public Drawable getWindSpeedGraph() {
		return windSpeedGraph;
	}

	public Drawable getWindDirGraph() {
		return windDirGraph;
	}

	public int getLocation() {
		return location;
	}

	public int getInterval() {
		return interval;
	}

	public CacheObject next() {
		return next;
	}

	public void clean() {
		next = null;
		windDirNeedle = null;
		windSpeedNeedle = null;
		windSpeedGraph = null;
		windDirGraph = null;
	}
}
