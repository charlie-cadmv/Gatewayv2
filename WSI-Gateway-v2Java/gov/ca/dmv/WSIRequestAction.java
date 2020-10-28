package gov.ca.dmv;

import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbRoute;

public class WSIRequestAction extends WSIGatewayAction {
	private String label=null;
	private String path=null;
	private String requestType=null;
	
	public WSIRequestAction(JSONObject jsonObject) {
		try {
			label=jsonObject.getString("label");
		} catch(JSONException e) {
			label=null;
		}
		try {
			path=jsonObject.getString("path");
		} catch(JSONException e) {
			path=null;
		}
		try {
			requestType=jsonObject.getString("requestType");
		} catch(JSONException e) {
			requestType=null;
		}
	}
	public String getRequestType() {
		return requestType;
	}
	public String getLabel() {
		return label;
	}
	public String getPath() {
		return path;
	}

	@Override
	public void performAction(MbJavaComputeNode node, MbMessageAssembly assy) throws WSIException {
		try {
			MbElement le=assy.getLocalEnvironment().getRootElement();
			if(requestType==null) requestType=MsgUtility.getElementAsString(le, "callType");
			switch(requestType) {
				case "REST": {
					if(label==null) label="WSIGatewayRESTRequest";
					le.evaluateXPath("?Destination/?HTTP/?RequestURL[set-value('"+path+"')]");
					break;
				}
				case "WS": {
					if(label==null) label="WSIGatewayWSRequest";
					
					le.evaluateXPath("?Destination/?SOAP/?Request/?Transport/?HTTP/?WebServiceURL[set-value('"+path+"')]");
					break;
				}
			}
			MsgUtility.setupCall(assy, "request");
			MbRoute r=node.getRoute(label);
			r.propagate(assy);
		} catch(MbException e) {
			throw new WSIException(e.getMessage());
		}
	}
}
