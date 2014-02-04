package se.tna.krypgrund;

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
	public float batteryVoltage = 0;;
	@Override
	public JSONObject getJSON() {
		JSONObject ret = new JSONObject();
		try {
				ret.put("MoistureInne", moistureInne);
				ret.put("MoistureUte", moistureUte);
				ret.put("TempUte", temperatureUte);
				ret.put("TempInne", temperatureInne);
				ret.put("AbsolutFuktInne",absolutFuktInne);
				ret.put("AbsolutFuktUte",absolutFuktUte);
				ret.put("FanOn", fanOn);
				ret.put("TimeStamp", time.format2445());
				ret.put("BatteryVoltage", batteryVoltage);
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	

}
