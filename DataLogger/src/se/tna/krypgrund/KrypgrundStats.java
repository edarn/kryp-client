package se.tna.krypgrund;

import java.util.List;

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
			ret.put("TimeStamp", time.format2445());
			ret.put("BatteryVoltage", batteryVoltage);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public static KrypgrundStats getAverage(List<KrypgrundStats> rawMeasurements) {
		// Always create a new object, as it is added to the
		// history list.
		KrypgrundStats total = new KrypgrundStats();

		// Calculate an averagevalue of all the readings.
		for (KrypgrundStats stat : rawMeasurements) {
			total.moistureInne += stat.moistureInne;
			total.moistureUte += stat.moistureUte;
			total.temperatureInne += stat.temperatureInne;
			total.temperatureUte += stat.temperatureUte;
			total.absolutFuktInne += stat.absolutFuktInne;
			total.absolutFuktUte += stat.absolutFuktUte;
			total.temperature += stat.temperature;
			total.batteryVoltage += stat.batteryVoltage;
		}

		float size = rawMeasurements.size();
		total.moistureInne /= size;
		total.moistureUte /= size;
		total.temperatureInne /= size;
		total.temperatureUte /= size;
		total.absolutFuktInne /= size;
		total.absolutFuktUte /= size;
		total.temperature /= size;
		total.batteryVoltage /= size;
		return total;
	}

}
