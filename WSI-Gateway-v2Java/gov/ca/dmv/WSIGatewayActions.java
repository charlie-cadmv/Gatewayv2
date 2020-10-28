package gov.ca.dmv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;


public class WSIGatewayActions {
	private String url=null;
	private Boolean active=null;
	private Date activeFrom=null;
	private Date activeTo=null;
	private WSITokenAction tokens=null;
	//private WSIAuthorizeAction authorize=null;
	private WSIRequestMediationAction requestMediation=null;
	private WSIRequestAction request=null;
	private WSIResponseMediationAction responseMediation=null;
	//
	//
	public WSIGatewayActions(JSONObject object) throws WSIException {
		try {
			url=object.getString("url");
		} catch(JSONException e) {
			//Should never happen
		}
		try {
			active=object.getBoolean("active");
		} catch(JSONException e) {
			active=false;
		}
		SimpleDateFormat sdf=new SimpleDateFormat();
		String dt=null;
		try {
			dt=object.getString("activeFrom");
			if(dt!=null) activeFrom=sdf.parse(dt);
		} catch(JSONException e) {
			activeFrom=new Date(0L);
		}
		catch(ParseException e) {
			System.err.println("activeFrom date "+dt+" ignored. Dates must be in format "+sdf.toLocalizedPattern());
			activeFrom=new Date(0L);
		}
		try {
			dt=object.getString("activeTo");
			if(dt!=null) activeTo=sdf.parse(dt);
		} catch(ParseException e) {
			System.err.println("activeTo date "+dt+" ignored. Dates must be in format "+sdf.toLocalizedPattern());
			activeTo=new Date(System.currentTimeMillis()+(long)10*365*24*60*60*1000);
		}	
		catch(JSONException e) {
			activeTo=new Date(System.currentTimeMillis()+(long)10*365*24*60*60*1000);
		}
		try {
			tokens=new WSITokenAction(object.getJSONArray("tokens"));
		} catch(JSONException e) {
			tokens=null;
		}
		try {
			requestMediation=new WSIRequestMediationAction(object.getJSONObject("requestMediation"));
		} catch(JSONException e) {
			requestMediation=null;
		}
		try {
			request=new WSIRequestAction(object.getJSONObject("request"));
		} catch(JSONException e) {
			request=null;
		}

		try {
			responseMediation=new WSIResponseMediationAction(object.getJSONObject("responseMediation"));
		} catch(JSONException e) {
			responseMediation=null;
		}
	}
	public WSIGatewayActions() {
	}
	public boolean isActionActive() {
		if(active) {
			if(isActiveDate()) {
				 return true;
			}
		}
		return false;
	}
	private boolean isActiveDate() {
		boolean ret=false;
		Date current=new Date();
		if(current.after(activeFrom) && current.before(activeTo)) ret=true;
		return ret;
	}
	public void setTokens(WSITokenAction a) {
		tokens=a;
	}
	public void setRequestMediation(WSIRequestMediationAction a) {
		requestMediation=a;
	}
	public void setResponseMediation(WSIResponseMediationAction a) {
		responseMediation=a;
	}
	public String getUrl() {
		return url;
	}
	public WSITokenAction getTokens() {
		return tokens;
	}
	public WSIRequestMediationAction getRequestMediation() {
		return requestMediation;
	}
	public WSIResponseMediationAction getResponseMediation() {
		return responseMediation;
	}
	public WSIRequestAction getRequest() {
		return request;
	}
	public WSIGatewayActions copy() {
		WSIGatewayActions copy=new WSIGatewayActions();
		copy.setTokens(tokens);
		copy.setRequestMediation(requestMediation);
		copy.setResponseMediation(responseMediation);
		copy.setActive(active);
		copy.setActiveFrom(activeFrom);
		copy.setActiveTo(activeTo);
		copy.setRequestAction(request);
		return copy;
	}
	private void setRequestAction(WSIRequestAction r) {
		request=r;
	}
	private void setActiveTo(Date a) {
		activeTo=a;
	}
	private void setActiveFrom(Date a) {
		activeFrom=a;
	}
	private void setActive(Boolean a) {
		active=a;
	}
}