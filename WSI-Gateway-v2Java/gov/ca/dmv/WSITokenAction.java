package gov.ca.dmv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbMessageAssembly;

public class WSITokenAction extends WSIGatewayAction {
	private static final String LDAP="LDAPUser";
	private static final String JWT="JWTToken";
	private static final String CUSTOM="CustomToken";
	private List<WSIGatewayAction> token_actions=new ArrayList<WSIGatewayAction>();
	public WSITokenAction(JSONArray ja) {
		for(int i=0;i<ja.length();i++) {
			JSONObject jo=ja.getJSONObject(i);
			try {
				String type=jo.getString("type");
				switch(type) {
					case LDAP: {
						token_actions.add(new WSILDAPUserAction(jo));
						break;
					}
					case JWT: {
						token_actions.add(new WSIJWTTokenAction(jo));
						break;
					}
					case CUSTOM: {
						WSIConfig.log.error("Custom tokens not supported at the moment");
						break;
					}
					default: {
						WSIConfig.log.error("Invalid token type: "+type+" found in ActionFile");
					}
				}
			} catch(JSONException e) {
				WSIConfig.log.error("Missing token type in ActionFile tokens");;
			}
		}
	}
	@Override
	public void performAction(MbJavaComputeNode node, MbMessageAssembly assy)
			throws WSIException {
		boolean authenticated=false;
		boolean authorized=true;
		for(Iterator<WSIGatewayAction> i=token_actions.iterator();i.hasNext();) {
			WSIGatewayAction action=i.next();
			if(action!=null) {
				if(action instanceof WSIAuthenticationAction) {
					if(!authenticated) {
						action.performAction(node, assy);
						authenticated=((WSIAuthenticationAction)action).isAuthenticated();
						authorized=((WSIAuthenticationAction)action).isAuthorized();
					}
				} else action.performAction(node, assy);
			}
		}
		if(!authenticated) throw new WSIException("Request was not authenticated");
		if(!authorized) throw new WSIException("Request was not authorized");
	}

}
