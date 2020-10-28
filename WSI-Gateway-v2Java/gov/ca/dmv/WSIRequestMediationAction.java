package gov.ca.dmv;

import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbRoute;

public class WSIRequestMediationAction extends WSIGatewayAction {
	private String label=null;
	private String path=null;
	private String base=null;

	public WSIRequestMediationAction(JSONObject jsonObject) {
		try {
			label=jsonObject.getString("label").trim();
		} catch(JSONException e) {
			label="WSIGatewayRESTRequestMediation";
		}
		try {
			path=jsonObject.getString("path").trim();
			if(! path.startsWith("/")) path="/"+path;
		} catch(JSONException e) {
			path=null;
		}
		try {
			base=jsonObject.getString("base").trim();
			if(base.endsWith("/")) base=base.substring(0,base.length());
		} catch(JSONException e) {
			base=null;
		}
	}
	public String getLabel() {
		return label;
	}
	public String getPath() {
		return path;
	}
	public String getBase() {
		return base;
	}

	public void performAction(MbJavaComputeNode node, MbMessageAssembly assy) throws WSIException {
		try {
			//MbElement root=assy.getMessage().getRootElement();
			MbElement le=assy.getLocalEnvironment().getRootElement();
			MsgUtility.setupCall(assy, "request");
			String requrl=null;
			if(path!=null) requrl=path;
			if(requrl==null) requrl=MsgUtility.getElementAsString(le, "path");
			//path must begin with a /
			if(base==null) throw new WSIException("Base URL is required for request mediations");
			if(requrl==null) throw new WSIException("Cannot resolve path for request mediations");
			//base must not end with a /
			
			le.evaluateXPath("?Destination/?HTTP/?RequestURL[set-value('"+base+requrl+"')]");
			MbRoute route=node.getRoute(label);
			route.propagate(assy);
				//check for HTTPResonse error (i.e. non 200 returns
		} catch(MbException e) {
			throw new WSIException(e.getMessage());
		}
	}
}
