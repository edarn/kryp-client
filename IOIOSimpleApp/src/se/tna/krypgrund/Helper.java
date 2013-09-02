package se.tna.krypgrund;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;

public class Helper {
	IOIO ioio = null;
	DigitalOutput A1=null;
	DigitalOutput A2=null;
	PwmOutput ASpeed = null;

	DigitalOutput B1=null;
	DigitalOutput B2=null;
	PwmOutput BSpeed = null;

	DigitalOutput Standby = null;
	TwiMaster i2c = null;
	public Helper(IOIO _ioio){
		
		ioio = _ioio;
		try {
			i2c = ioio.openTwiMaster(1,TwiMaster.Rate.RATE_100KHz,false);
			Standby = ioio.openDigitalOutput(6);
			Standby.write(true); //Activate chip

			A1 = ioio.openDigitalOutput(5);
			A1.write(false);
			A2 = ioio.openDigitalOutput(4);
			A2.write(false);

			ASpeed = ioio.openPwmOutput(3, 1000000);
			ASpeed.setDutyCycle(0); //Enginge off - 1 = Full on


		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //USE FALSE for I2C otherwise to high voltage!!!

	}

	public void Destroy(){
		i2c.close();
		Standby.close();
		A1.close();
		A2.close();
		ASpeed.close();
	}
/*
	static int GetTemperature(AnalogInput port){
		float SensorTemp=0;
		try {
			float SensorVolt = port.getVoltage();
			float SupplyVolt = 5;
			//SensorTemp = (float) ((SupplyVolt-0.16)/(0.0062*SensorVolt));
			
			//Temperature compensation True RH = (Sensor RH)/(1.0546 � 0.00216T), T in �C
			//int TrueRH = SensorRF/(1.0546-0.00216*temperature);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (int) SensorTemp;
	}
	

	static int GetAnalogueMoisture(AnalogInput port){
		float SensorRF=0;
		try {
			float SensorVolt = port.getVoltage();
			float SupplyVolt = 5;
			SensorRF = (float) ((SupplyVolt-0.16)/(0.0062*SensorVolt));


			//		Voltage output (1
			//				st
			//				 order curve fit) VOUT
			//				=(VSUPPLY
			//				)(0.0062(sensor RH) + 0.16), typical at 25 �C 
			//				Temperature compensation True RH = (Sensor RH)/(1.0546 � 0.00216T), T in �C
			//int TrueRH = SensorRF/(1.0546-0.00216*temperature);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (int) SensorRF;
	}
*/
	public enum SensorType{
		SensorInne,
		SensorUte;
	}

	public boolean SendI2CCommand(int adress, int register, int data){

		byte toSend[] = new byte [2];
		byte receive[] = new byte[1];
		toSend[0] = (byte)register;
		toSend[1] = (byte)data;
		try {
			i2c.writeRead(adress/2, false, toSend, 2, receive, 0);
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public int ReadI2CData(int adress, int register)
	{
		byte toSend[] = new byte [1];
		byte toReceive[] = new byte[1];
		toSend[0] = (byte)register;

		try {
			i2c.writeRead(adress/2, false, toSend, 1, toReceive, 1);
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}

		return (int)(toReceive[0]&0xFF);
	}

	public boolean SetupGpioChip(){

		SendI2CCommand(0x40,1,255);
		SendI2CCommand(0x40,2,255);
		SendI2CCommand(0x40,3,0xC0);

		return true;
	}	
	
	public float GetTemperatureNew(SensorType type){
		float temperature = -1; //Result in %
		//float supply = 5;

		int high=0, low = 0, total = 0;
		float voltage = (float) 4.93;

		if (type == SensorType.SensorInne)
			SendI2CCommand(0x40,0,0x3); //Request ADC measurement from AD0 channel
			else if (type == SensorType.SensorUte)
					SendI2CCommand(0x40,0,0x5); //Request ADC measurement from AD2 channel
		
		high = ReadI2CData(0x40,1); //Read Highbyte
		low = ReadI2CData(0x40,2);  //Read Lowbyte
		total = (high << 8) + low;	//Raw measurement 0-1023 value representing 0-5V
		voltage = (float)((float)total*(float)voltage/(float)1023);
		temperature = 100 * voltage - 50;
		return temperature;
	}

	
	public float GetTemperature(SensorType type){
		float temperature = -1; //Result in %
		float supply = 5;

		int high=0, low = 0, total = 0;
		float voltage = (float) 4.93;

		if (type == SensorType.SensorInne)
			SendI2CCommand(0x40,0,0x3); //Request ADC measurement from AD0 channel
	//		SendI2CCommand(0x40,0,0x4); //Request ADC measurement from AD0 channel
			else if (type == SensorType.SensorUte)
					SendI2CCommand(0x40,0,0x5); //Request ADC measurement from AD2 channel
	//				SendI2CCommand(0x40,0,0x6); //Request ADC measurement from AD2 channel
		
		high = ReadI2CData(0x40,1); //Read Highbyte
		//ClearScreen();
		//CursorHome();
		//WriteText("High:" + Integer.toString(high));
		//NewLine();
		low = ReadI2CData(0x40,2);  //Read Lowbyte
		//WriteText("Low :" + Integer.toString(low));
		//NewLine();
		total = (high << 8) + low;	//Raw measurement 0-1023 value representing 0-5V
		//WriteText("Tot :" + Integer.toString(total));
		//NewLine();
		voltage = (float)((float)total*(float)voltage/(float)1024);
		//WriteText("Volt:" + Float.toString(voltage));
		//temperature = (float)(((float)((float)voltage/(float)supply)-(float)0.16)/(float)0.0062);
		temperature = ((float) ((float)(voltage/((float)supply/(float)5))-1.375))/(float)0.0225;
		//Temperature compensation for moisture
		// int temperature = xxx;
		//moisture = (float)((float)rawMoisture/(float)((1.0546-0.00216*temperature)));
		// = rawMoisture;

		return temperature;
	}


	public float GetMoisture(SensorType type, float temperature){
		float moisture = -1; //Result in %
		float rawMoisture = -1;
		float supply = (float) 4.93;

		int high=0, low = 0, total = 0;
		float voltage = 0;

		if (type == SensorType.SensorInne)
			SendI2CCommand(0x40,0,0x4); //Request ADC measurement from AD1 channel
		else if (type == SensorType.SensorUte)
			SendI2CCommand(0x40,0,0x6); //Request ADC measurement from AD3 channel
		
		high = ReadI2CData(0x40,1); //Read Highbyte
		//ClearScreen();
		//CursorHome();
		//WriteText("High:" + Integer.toString(high));
		//NewLine();
		low = ReadI2CData(0x40,2);  //Read Lowbyte
		//WriteText("Low :" + Integer.toString(low));
		//NewLine();
		total = (high << 8) + low;	//Raw measurement 0-1023 value representing 0-5V
		//WriteText("Tot :" + Integer.toString(total));
		//NewLine();
		voltage = (float)((float)total*(float)4.93/(float)1024);
		//WriteText("Volt:" + Float.toString(voltage));
		rawMoisture = (float)(((float)((float)voltage/(float)supply)-(float)0.16)/(float)0.0062);

		//Temperature compensation for moisture
		// int temperature = xxx;
		moisture = (float)((float)rawMoisture/(float)((1.0546-0.00216*temperature)));
		//moisture = rawMoisture;

		return moisture;
	}

	public void ClearScreen(){
		SendI2CCommand(0xC6,0,12);
	}

	public void TurnOnBacklight(){
		SendI2CCommand(0xC6,0,19);
	}

	public void NewLine(){
		SendI2CCommand(0xC6,0,13);
	}
	
	public void CursorHome()
	{
		SendI2CCommand(0xC6,0,1);
	}

	public void WriteText(String text){
		
		for (char b : text.toCharArray()){
			SendI2CCommand(0xC6,0,b);
		}
		
	}
	public static int FanMaxSpeed = 100;
	public static int Fan30Percent = (int)(FanMaxSpeed*0.3);
	public static int FanStop = 0;
	public static boolean FanOn = false;
	boolean ControlFan(int Speed, boolean Clockwise){
		boolean retVal = true;
		try {
			if(Speed == FanStop){
				A1.write(false);
				A2.write(false);
				FanOn = false;
			}
			else if (Clockwise){
				A1.write(true);
				A2.write(false);
				FanOn=true;

			}
			else {
				A1.write(false);
				A2.write(true);
				FanOn=true;
			}
			ASpeed.setDutyCycle((float)(((float)Speed)/FanMaxSpeed));   

		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			retVal = false;
			e.printStackTrace();
		}


		return retVal;
	}

	public boolean IsFanOn() {
		return FanOn;
	}
	int failureDelay = 0;
	boolean SendSuccess = true;
	
	public String SendDataToServer(ArrayList<Stats> history, boolean forceSendData, String id) {
		String retVal="HSize=" + history.size() + " Succ="+ Boolean.toString(SendSuccess) + " fDelay:"+ Integer.toString(failureDelay);
		failureDelay++;
		if (history.size() > 12|| forceSendData) {
	/*		if (SendSuccess || (!SendSuccess && failureDelay > 12))*/ {
				SendSuccess = true;
				failureDelay =0;
				HttpClient client=null;
				JSONObject data;

				try {
					/* Keep sending data until there is a send failure or the history is emptied. */
					while(SendSuccess && history.size() > 0){
						data = new JSONObject();

						client = new DefaultHttpClient();
						HttpPost message = new HttpPost("http://www.surfvind.se/Krypgrund.php");
						message.addHeader("content-type", "application/x-www-form-urlencoded");
						JSONArray dataArray = new JSONArray();
						/* Create a JSON Array that contains the data.*/
						for (int i = 0; i < Math.min(history.size(),500); i++) //Dont send more than 500 measures in one post.
						{
							Stats temp = history.get(i);
							dataArray.put(temp.getJSON());
						}
						data.put("measure", dataArray);
						data.put("id", id);
						message.setEntity(new StringEntity(data.toString()));
						HttpResponse response = client.execute(message);
						if(response.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
							for (int i = 0; i < Math.min(history.size(),500); i++) //Delete the reading that are sent.
							{
								history.remove(0);
							}
							retVal="Success";
						}
						else{
							SendSuccess = false;
							retVal="F: "+response.getStatusLine().getStatusCode();
						}
					}
				} catch(Exception e){
					retVal="Ex:" + e.toString();

				} finally {
					if(null != client)
						client.getConnectionManager().shutdown();
				}
			}
		}
		return retVal;
		
	}

	public boolean GetSendSuccess() {
		return SendSuccess;
	}

	public int GetFailureDelay() {
		return failureDelay;
	}
}