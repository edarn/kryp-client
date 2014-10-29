package com.surfvindClient.logic;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

import com.surfvindClient.SurfvindClient;

public class Load {

	private String baseAddress;

	/* Image paths */
	public static final String imagePath = "http://www.surfvind.se/";

	private static final String windSpeedMeterName = "_img_speed.png";
	private static final String windDirMeterName = "_img_compass.png";

	/* Images */
	private Drawable windSpeedMeter;
	private Drawable windDirectionMeter;

	private HttpParams http;
	private HttpClient client;

	public Load(String address) {
		this.baseAddress = address;
	}

	/**
	 * 
	 */
	public void load(Handler handler, String extension, String imei) {
		if (baseAddress.endsWith("/")) {
			new LoadThread(handler, baseAddress + extension, imei).start();
		} else {
			new LoadThread(handler, baseAddress + "/" + extension, imei)
					.start();
		}
	}

	public Drawable getWindSpeedMeter() {
		return windSpeedMeter;
	}

	public Drawable getWindDirectionMeter() {
		return windDirectionMeter;
	}

	private class LoadThread extends Thread {

		private String link;
		private Handler handler;
		private String imei;

		public LoadThread(Handler handler, String link, String imei) {
			this.link = link;
			this.handler = handler;
			this.imei = imei;
		}

		public void run() {
			boolean connectionProblem = false;
			http = new BasicHttpParams();
			client = new DefaultHttpClient(http);
			try {
				
				System.out.println(link);
				
				client.execute(new HttpGet(link), new BasicHttpContext());
				client.getConnectionManager().shutdown();
			} catch (IOException ioe) {
				connectionProblem = true;
				System.err.println("Unable to connect!!");
				ioe.printStackTrace();
			}

			if (!connectionProblem) {
				/* Load wind speed needle */
				windSpeedMeter = loadImage(imagePath + "Images/" + imei
						+ windSpeedMeterName);

				/* Load wind dir needle */
				windDirectionMeter = loadImage(imagePath + "Images/" + imei
						+ windDirMeterName);

			}

			Message msg = new Message();
			msg.arg1 = SurfvindClient.LOAD_FINISHED;
			handler.sendMessage(msg);
		}

		private Drawable loadImage(String url) {
			try {
				InputStream is = (InputStream) fetch(url);
				return Drawable.createFromStream(is, "src");
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		private Object fetch(String address) throws MalformedURLException,
				IOException {
			URL url = new URL(address);
			Object content = url.getContent();
			return content;
		}

	}
}
