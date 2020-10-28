package gov.ca.dmv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;
import org.json.JSONObject;

public class WSIConfig {
	private static long lastConfigUpdate=0;
	public static String actionFile=null;
	public static long actionReloadInterval=10*60*1000;
	public static String baseUrlWS=null;
	public static String baseUrlREST=null;
	public static Logger log=null;
	static {
		loadConfig();
	}
	public static void loadConfig() {
		long current=System.currentTimeMillis();
		if((lastConfigUpdate+600*1000)<current) {
			StringBuffer sb=new StringBuffer();
			BufferedReader br=null;
			try {
				br = new BufferedReader(new FileReader("/var/mqsi/WSIGateway/config.json"));
				while(true) {
					String s=br.readLine();
					if(s==null) break;
					sb.append(s);
				}
			} catch (IOException e) {
				
			}
			finally {
				try {
					if(br!=null) br.close();
				} catch (IOException e) {
					System.err.println("Cannot close actionFile "+e.getMessage());
				}
			}
			lastConfigUpdate=current;
			JSONObject jo= new JSONObject(sb);
			try {
				actionFile=jo.getString("ActionsFile");
			} catch(JSONException e2) {
				actionFile="/var/mqsi/WSIGateway/ActionsFile.json";
			}
			try {
				baseUrlWS=jo.getString("BaseUrlWS");
			} catch(JSONException e2) {
				baseUrlWS="/DMV/Gateway";
			}
			try {
				baseUrlREST=jo.getString("BaseUrlREST");
			} catch(JSONException e2) {
				baseUrlREST="/DMV/REST";
			}
			String log4j=null;
			try {
				log4j=jo.getString("LoggingConfigurationFile");
			} catch(JSONException e2) {
				log4j="/var/mqsi/WSIGateway/log4j.properties";
			}
		    PropertyConfigurator.configure(log4j);
			log=Logger.getRootLogger();
			log.info("Reloading configuration for WSIGateway");
			try {
				actionReloadInterval=jo.getInt("ActionReloadInterval");
			} catch(JSONException e2) {
				actionReloadInterval=120;
			}
		}
	}
}
