package gov.ca.dmv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WSIGatewayProcess {
	private static Map<String,List<WSIGatewayActions>>  actions=new HashMap<String,List<WSIGatewayActions>>();
	private static long lastUpdate=0;
	public WSIGatewayProcess() {
	}
	public synchronized void putActions(String s,WSIGatewayActions w) {
		if(!actions.containsKey(s)) actions.put(s, new ArrayList<WSIGatewayActions>());
		actions.get(s).add(w);
	}
	public synchronized WSIGatewayActions getActions(String s) throws WSIException {
		long current=System.currentTimeMillis();
		WSIGatewayActions ret=null;
		//System.out.println(current+" "+lastUpdate+" "+actionReloadInterval);
		if((lastUpdate+WSIConfig.actionReloadInterval)<current) {
			try {	
				//System.out.println("Loading file");
				loadActions(); //could leave actions in bad state!
				lastUpdate=current;
			} catch(WSIException e) {
				System.err.println("Exception caught "+e.getMessage());
			}
		}
		if(actions!=null && actions.size()>0) {
			String target=s;
			while(true) {
				//System.out.println("Trying "+target);
				List<WSIGatewayActions> candidates=actions.get(target);
				if(candidates!=null) {
					for(Iterator<WSIGatewayActions> i=candidates.iterator();i.hasNext();) {
						WSIGatewayActions a=i.next();
						if(a.isActionActive()) {
					//need to authenticate and check authorizations
							ret=a;
							break; //leave for loop
						}
					}
				}
				if(ret!=null) break; // a candiates has been found
			// check wildcards
				int index=target.lastIndexOf("/");
				if(index>=0) {
					if(target.substring(index+1).length()==0) target=target+"*";
					if(!target.substring(index+1).equals("*")) {  //make this a wildcard
						target=target.substring(0,index)+"/*";
						continue;
					}
					if(index>0) {
						int index2=target.lastIndexOf("/",index-1);
						if(index2>=0) {
							target=target.substring(0,index2)+"/*";
							continue;
						} 
						else break;
					} 
					else break;
				} 
				else break;
			}
			if(ret!=null) System.out.println("Trying "+ret.getUrl());
		}
		if(ret!=null) return ret.copy();
		else return null;
	}
	private void loadActions() throws WSIException {
		if(WSIConfig.actionFile!=null)  {
			BufferedReader br=null;
			try {
				br = new BufferedReader(new FileReader(WSIConfig.actionFile));
				StringBuffer sb=new StringBuffer();
				while(true) {
					String s=br.readLine();
					if(s==null) break;
					sb.append(s);
				}
				JSONArray action=null;
				try {
					action=new JSONArray(sb.toString());
				} catch(JSONException e) {
					throw new WSIException("Invalid JSON format in file "+WSIConfig.actionFile);
				}
				actions.clear();
				for(int i=0;i<action.length();i++) {
					try {
						String name=((JSONObject)action.get(i)).getString("url");
						putActions(name, new WSIGatewayActions(action.getJSONObject(i)));
					} catch(JSONException e2) {
						System.err.println("Ignoring actions, no URL found");
					}
				}
			} catch (IOException e) {
				throw new WSIException(e.getMessage());
			}
			finally {
				try {
					if(br!=null) br.close();
				} catch (IOException e) {
					System.err.println("Cannot close actionFile "+e.getMessage());
				}
			}
		} else throw new WSIException("actionFile has not been set in config.json");
	}
	public static void setActionFile(String s) {
		WSIConfig.actionFile=s;
	}
	public static void setReloadTime(int mins) {
		WSIConfig.actionReloadInterval=mins*60*1000;
			
	}


}
