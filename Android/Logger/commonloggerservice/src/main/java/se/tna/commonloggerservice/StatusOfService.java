package se.tna.commonloggerservice;

public class StatusOfService {
	public float moistureInne = 0;
	public float moistureUte = 0;
	public float temperatureUte = 0;
	public float temperatureInne = 0;
	public float absolutFuktInne = 0;
	public float absolutFuktUte = 0;
	public boolean fanOn = false;
	public long timeOfCreation;
	public int historySize =0;
	public int readingSize = 0;
	public String statusMessage = "Not Set";
	public long timeForLastSendData = 0;
	public long timeBetweenSendingDataToServer = 0;
	public long timeForLastAddToHistory = 0;
	public long timeBetweenAddToHistory = 0;
	public long timeBetweenReading = 0;
	public long timeForLastFanControl = 0;
	public String deviceId = "Not Detected";
	public float analogInput = 0;
	public int windDirection = 0;
	public float windSpeed = 0;
	public float voltage;
	public boolean isIOIOConnected;
	public float moistureExtra = 0;
	public float temperatureExtra = 0;
	public int airpreassure;
	public float rain;

	public StatusOfService(){
		timeOfCreation = System.currentTimeMillis();
	}

}
