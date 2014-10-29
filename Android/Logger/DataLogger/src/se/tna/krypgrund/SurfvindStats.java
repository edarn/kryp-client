package se.tna.krypgrund;

import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class SurfvindStats extends Stats implements Comparable<SurfvindStats> {
	public float windSpeedMin = 0;
	public float windSpeedAvg = 0;
	public float windSpeedMax = 0;
	public float windDirectionMin = 0;
	public float windDirectionAvg = 0;
	public float windDirectionMax = 0;

	@Override
	public JSONObject getJSON() {

		JSONObject ret = new JSONObject();
		try {
			ret.put("WindDirectionMin", windDirectionMin);
			ret.put("WindDirectionAvg", windDirectionAvg);
			ret.put("WindDirectionMax", windDirectionMax);

			ret.put("WindSpeedMin", windSpeedMin);
			ret.put("WindSpeedAvg", windSpeedAvg);
			ret.put("WindSpeedMax", windSpeedMax);

			ret.put("Battery", (int) batteryVoltage);
			ret.put("Temperature", (int) temperature);

			ret.put("TimeStamp", time.format2445());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public int compareTo(SurfvindStats another) {
		if (windSpeedAvg > another.windSpeedAvg) {
			return 1;
		} else if (windSpeedAvg < another.windSpeedAvg) {
			return -1;
		}
		return 0;
	}

	public static SurfvindStats getAverage(ConcurrentMaxSizeArray<SurfvindStats> rawMeasurements) {
		// Always create a new object, as it is added to the
		// history list.
		SurfvindStats total = new SurfvindStats();
		if (rawMeasurements != null && rawMeasurements.size() > 0) {
			total.windDirectionMin = 999999;
			total.windSpeedMin = 999999;

           	// Calculate an averagevalue of all the readings.
			for (Object s : rawMeasurements.getArray()) {
                SurfvindStats stat = (SurfvindStats) s;
				total.windDirectionAvg += stat.windDirectionAvg;
				if (stat.windDirectionAvg < total.windDirectionMin) {
					total.windDirectionMin = stat.windDirectionAvg;
				}
				if (stat.windDirectionAvg > total.windDirectionMax) {
					total.windDirectionMax = stat.windDirectionAvg;
				}
				total.windSpeedAvg += stat.windSpeedAvg;
				if (stat.windSpeedAvg < total.windSpeedMin) {
					total.windSpeedMin = stat.windSpeedAvg;
				}
				if (stat.windSpeedAvg > total.windSpeedMax) {
					total.windSpeedMax = stat.windSpeedAvg;
				}
				total.temperature += stat.temperature;
				total.batteryVoltage += stat.batteryVoltage;
			}
			int size = rawMeasurements.size();
			total.windDirectionAvg /= size;
			total.windSpeedAvg /= size;
			total.temperature /= size;
			total.batteryVoltage /= size;
		}
		return total;
	}
}
