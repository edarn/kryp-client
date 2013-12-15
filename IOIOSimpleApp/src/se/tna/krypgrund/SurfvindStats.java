package se.tna.krypgrund;

import org.json.JSONException;
import org.json.JSONObject;

public class SurfvindStats extends Stats {
	public float windSpeedMin = 0;
	public float windSpeedAvg = 0;
	public float windSpeedMax = 0;
	public float windDirectionMin = 0;
	public float windDirectionAvg = 0;
	public float windDirectionMax = 0;
	public int batteryVoltage =0;
	public int temperature = 0;
	

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
			
			ret.put("Battery", (int)batteryVoltage);
			ret.put("Temperature", (int)temperature);
			
			ret.put("TimeStamp", time.format2445());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
}
