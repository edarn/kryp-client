package se.tna.commonloggerservice;

import org.json.JSONException;
import org.json.JSONObject;

public class KrypgrundStats extends Stats {
	public float moistureInne = 0;
	public float moistureUte = 0;
	public float temperatureUte = 0;
	public float temperatureInne = 0;
	public float absolutFuktInne = 0;
	public float absolutFuktUte = 100;
	public boolean fanOn = false;
	public float absolutFuktExtra = 0;
	public float moistureExtra = 0;
	public float temperatureExtra = 0;

	@Override
	public JSONObject getJSON() {
		JSONObject ret = new JSONObject();
		try {
			ret.put("MoistureInne", moistureInne);
			ret.put("MoistureUte", moistureUte);
			ret.put("TempUte", temperatureUte);
			ret.put("TempInne", temperatureInne);
			ret.put("AbsolutFuktInne", absolutFuktInne);
			ret.put("AbsolutFuktUte", absolutFuktUte);
			ret.put("FanOn", fanOn);
			ret.put("TimeStamp", time);
			ret.put("BatteryVoltage", batteryVoltage);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public static KrypgrundStats getAverage(ConcurrentMaxSizeArray<KrypgrundStats> rawMeasurements) {
		// Always create a new object, as it is added to the
		// history list.
		KrypgrundStats total = new KrypgrundStats();

		if (rawMeasurements != null && rawMeasurements.size() > 0) {
			// Calculate an averagevalue of all the readings.
			for (Object s : rawMeasurements.getArray()) {
                KrypgrundStats stat = (KrypgrundStats) s;
                total.moistureInne += stat.moistureInne;
				total.moistureUte += stat.moistureUte;
				total.temperatureInne += stat.temperatureInne;
				total.temperatureUte += stat.temperatureUte;
				total.absolutFuktInne += stat.absolutFuktInne;
				total.absolutFuktUte += stat.absolutFuktUte;
				total.batteryVoltage += stat.batteryVoltage;
			}

			float size = rawMeasurements.size();
			total.moistureInne /= size;
			total.moistureUte /= size;
			total.temperatureInne /= size;
			total.temperatureUte /= size;
			total.absolutFuktInne /= size;
			total.absolutFuktUte /= size;
			total.batteryVoltage /= size;
		}
		return total;
	}

}
