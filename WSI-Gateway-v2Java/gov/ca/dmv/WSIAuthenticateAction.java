package gov.ca.dmv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbRoute;

public class WSIAuthenticateAction extends WSIGatewayAction {
	private String type=null;

	public WSIAuthenticateAction(JSONArray ja) throws WSIException {
		for(int i=0;i<ja.length();i++) {
			JSONObject jsonObject=ja.getJSONObject(i);
			try {
				type=jsonObject.getString("label");
			} catch(JSONException e) {
				type="missing";
			}
			switch(type) {
				case "LDAPBasic": {
					break;
				}
				case "JWTToken": {
					break;
				}
				default: {
					WSIConfig.log.error("Unknown token type "+type);
				}
			}
		}
	}

	@Override
	public void performAction(MbJavaComputeNode node, MbMessageAssembly assy) throws WSIException {
		try {
			MbRoute route=node.getRoute(type);
			route.propagate(assy);
		} catch(MbException e) {
			WSIError s=ExceptionHandler.processException(assy);
			throw new WSIException("Unable to perform authentication for user "+s,s.getType());
		}
	}

}
