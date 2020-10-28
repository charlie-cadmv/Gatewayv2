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

public class WSIJWTTokenAction extends WSIGatewayAction implements WSIAuthenticationAction {
	private String label="WSIJWTAuthentication";
	private List<WSIJWTAuthenticateAction> authenticate=new ArrayList<WSIJWTAuthenticateAction>();

	public WSIJWTTokenAction(JSONObject jo) {
		try {
			label=jo.getString("label");
		} catch(JSONException e) {
			WSIConfig.log.error("Missing label in JWT authenticate");
				label="WSIJWTAuthenticate";
			}
			try {
				JSONArray ja=jo.getJSONArray("authenticate");
				for(int i=0;i<ja.length();i++) {
					authenticate.add(new WSIJWTAuthenticateAction(ja.getJSONObject(i)));
				}
			} catch(JSONException e) {
				authenticate=null;
				WSIConfig.log.error("Missing authenticate actions for LDAPUser authenticate");
			}
	}

	@Override
	public void performAction(MbJavaComputeNode node, MbMessageAssembly assy) throws WSIException {
		try {
			MbRoute r=node.getRoute(label);
			if(r!=null) {
				if(authenticate!=null) {
					for(Iterator<WSIJWTAuthenticateAction> i=authenticate.iterator();i.hasNext();) {
						WSIJWTAuthenticateAction a=i.next();
						a.setRoute(r);
						a.performAction(node, assy); //the point to the process node
					}
				}
			} else throw new WSIException("Cannot find a route for label "+label);
		} catch(MbException e) {
			WSIConfig.log.error("Cannot find route for label "+label+": "+e.getMessage());
			throw new WSIException("Cannot find route for label "+label+": "+e.getMessage());
		}
	}

	@Override
	public boolean isAuthenticated() {
		// TODO Auto-generated method
		return false;
	}

	@Override
	public boolean isAuthorized() {
		// TODO Auto-generated method stub
		return false;
	}

}
