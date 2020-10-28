package gov.ca.dmv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbRoute;

public class WSILDAPUserAuthenticateAction extends WSIGatewayAction implements WSIAuthenticationAction {
	private MbRoute route=null;
	private String label="WSILDAPUserAuthenticate";
	private boolean authenticated=false;
	private boolean authorized=false;
	private String authorizedBy=null;
	private List<String> authorize=new ArrayList<String>();
	public WSILDAPUserAuthenticateAction(JSONObject jo) {
		try {
			label=jo.getString("label");
		} catch(JSONException e) {
			label="WSILDAPUserAuthenticate";
		}
		try {
			JSONArray ja=jo.getJSONArray("authorize");
			for(int i=0;i<ja.length();i++) {
				authorize.add(ja.getString(i));
			}
		} catch(JSONException e) {
			WSIConfig.log.error("No authorization actions");
			authorize=null;
		}
	}

	@Override
	public void performAction(MbJavaComputeNode node, MbMessageAssembly assy) throws WSIException {
		// try to authenticate
		try {
			LocalEnvironment le=new LocalEnvironment(assy.getLocalEnvironment().getRootElement());
			le.setValue("?JUMP_LABEL", label);
			System.out.println("");
			if(route!=null) route.propagate(assy);
			else throw new WSIException("No route for UserLDAPAuthenticator");
			authenticated=true;
			if(authorize!=null) {
				authorized=false;
				for(Iterator<String> i=authorize.iterator();i.hasNext();) {
					String a=i.next();
					le.setValue("?JUMP_LABEL", a);
					route.propagate(assy);
					authorized=true;
					authorizedBy=a;
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
			System.out.println();
			throw new WSIException("Cannot perform authentication/authorization "+e.getMessage());
		}
	}

	public void setRoute(MbRoute r) {
		route=r;
		
	}
	public boolean isAuthenticated() {
		return authenticated;
	}
	public boolean isAuthorized() {
		return authorized;
	}
	public String getAuthorizedBy() {
		return authorizedBy;
	}
}
