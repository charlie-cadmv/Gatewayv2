package gov.ca.dmv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbRoute;

public class WSIJWTAuthenticateAction extends WSIGatewayAction implements WSIAuthenticationAction {
	private MbRoute route=null;
	private String label="WSIJWTAuthenticate";
	private String keyFile="/var/mqsi/WSIGateway/publickey.pem";
	private String algorithm="RSA256";
	private Date expiresAt=null;

	private boolean authenticated=false;
	private boolean authorized=false;
	private String authorizedBy=null;
	private List<WSIJWTAuthorizeAction> authorize=new ArrayList<WSIJWTAuthorizeAction>();

	public WSIJWTAuthenticateAction(JSONObject jo) {
		try {
			label=jo.getString("label");
		} catch(JSONException e) {
			label="WSIJWTAuthenticate";
		}
		try {
			keyFile=jo.getString("keyFile");
		} catch(JSONException e) {
			keyFile="/var/mqsi/WSIGateway/publickey.pem";
		}
		try {
			keyFile=jo.getString("algorithm");
		} catch(JSONException e) {
			algorithm="RSA256";
		}
		String dt=null;
		SimpleDateFormat sdf=new SimpleDateFormat();
		try {
			dt=jo.getString("activeTo");
			if(dt!=null) expiresAt=sdf.parse(dt);
		} catch(ParseException e) {
			WSIConfig.log.error("expiresAt date "+dt+" ignored. Dates must be in format "+sdf.toLocalizedPattern());
			expiresAt=new Date(System.currentTimeMillis()+(long)10*365*24*60*60*1000); //10 years
		}	
		catch(JSONException e) {
			expiresAt=new Date(System.currentTimeMillis()+(long)10*365*24*60*60*1000); //10 years
		}


		try {
			JSONArray ja=jo.getJSONArray("authorize");
			for(int i=0;i<ja.length();i++) {
				JSONObject jo2=ja.getJSONObject(i);
				authorize.add(new WSIJWTAuthorizeAction(jo2));
			}
		} catch(JSONException e) {
			WSIConfig.log.error("No authorization actions");
			authorize=null;
		}
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public boolean isAuthorized() {
		return authorized;
	}
	public String getAuthorizedBy() {
		return authorizedBy;
	}

	public void setRoute(MbRoute r) {
		route=r;
	}

	public void performAction(MbJavaComputeNode node, MbMessageAssembly assy) throws WSIException {
		// try to authenticate
		try {
			LocalEnvironment le=new LocalEnvironment(assy.getLocalEnvironment().getRootElement());
			le.setValue("?JUMP_LABEL", label);
			le.setValue("JWTKeyFile", keyFile);
			le.setValue("JWTExpiresAt",expiresAt);
			le.setValue("?JWTAlgorithm",algorithm);
			if(route!=null) route.propagate(assy);
			else throw new WSIException("No route for JWTAuthenticator");
			authenticated=true;
			if(authorize!=null) {
				authorized=false;
				for(Iterator<WSIJWTAuthorizeAction> i=authorize.iterator();i.hasNext();) {
					WSIJWTAuthorizeAction a=i.next();
					le.setValue("?JUMP_LABEL", a.getLabel());
					route.propagate(assy);
					authorized=true;
					authorizedBy=a.getName();
					break;
				}
			} else authorized=true;
		} catch(MbException e) {
			String s=null;
			try {
				LocalEnvironment le=new LocalEnvironment(assy.getLocalEnvironment().getRootElement());
				try {
					s=le.getString("ExceptionList");
				} catch (IllegalAccessException e1) {
				}
			}  catch (MbException e1) {
			}
			throw new WSIException("Cannot perform authentication/authorization "+s);
		} catch (IllegalAccessException e) {
			throw new WSIException("Cannot perform authentication/authorization "+e.getMessage());
		}		
	}

}
