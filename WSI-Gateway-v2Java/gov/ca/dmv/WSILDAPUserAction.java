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

public class WSILDAPUserAction extends WSIGatewayAction implements WSIAuthenticationAction {
	private String label="WSILDAPUserAuthentication";
	private List<WSILDAPUserAuthenticateAction> authenticate=new ArrayList<WSILDAPUserAuthenticateAction>();
	public WSILDAPUserAction(JSONObject jo) {
		try {
			label=jo.getString("label");
		} catch(JSONException e) {
			WSIConfig.log.error("Missing label in LDAPUser authenticate");
			label="WSILDAPUserAuthentication";
		}
		try {
			JSONArray ja=jo.getJSONArray("authenticate");
			for(int i=0;i<ja.length();i++) {
				authenticate.add(new WSILDAPUserAuthenticateAction(ja.getJSONObject(i)));
			}
		} catch(JSONException e) {
			authenticate=null;
			WSIConfig.log.error("Missing autheticate actions for LDAPUser authenticate");
		}
	}

	@Override
	public void performAction(MbJavaComputeNode node, MbMessageAssembly assy)  throws WSIException {
		try {
			MbRoute r=node.getRoute(label);
			if(r!=null) {
				if(authenticate!=null) {
					for(Iterator<WSILDAPUserAuthenticateAction> i=authenticate.iterator();i.hasNext();) {
						WSILDAPUserAuthenticateAction a=i.next();
						a.setRoute(r);
						a.performAction(node, assy); //the point to the process node
					}
				}
			} else throw new WSIException("Cannot find a route for label "+label);
		} catch(MbException e) {
			WSIConfig.log.error("Cannot find route for label "+label+": "+e.getMessage());
		}
	}

	@Override
	public boolean isAuthenticated() {
		if(authenticate!=null) {
			for(Iterator<WSILDAPUserAuthenticateAction> i=authenticate.iterator();i.hasNext();) {
				WSILDAPUserAuthenticateAction a=i.next();
				if(a.isAuthenticated()) return true;
			}
		} else return true;
		return false;
	}
	public boolean isAuthorized() {
		if(authenticate!=null) {
			for(Iterator<WSILDAPUserAuthenticateAction> i=authenticate.iterator();i.hasNext();) {
				WSILDAPUserAuthenticateAction a=i.next();
				if(a.isAuthenticated()) {
					if(a.isAuthorized()) return true;
				}
			}
		} else return true;
		return false;
	}

}
