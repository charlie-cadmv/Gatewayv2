package gov.ca.dmv;

import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbMessageAssembly;

public class WSIAuthorizeAction extends WSIGatewayAction {
	@SuppressWarnings("unused")
	private String label=null;

	public WSIAuthorizeAction(JSONObject jsonObject) {
		try {
			label=jsonObject.getString("label");
		} catch(JSONException e) {
			label="WSIGatewayAuthorize";
		}
	}

	@Override
	public void performAction(MbJavaComputeNode node, MbMessageAssembly assy) throws WSIException {

	}

}
