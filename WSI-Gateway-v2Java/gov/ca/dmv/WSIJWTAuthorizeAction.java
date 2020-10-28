package gov.ca.dmv;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbRoute;


public class WSIJWTAuthorizeAction extends WSIGatewayAction implements WSIAuthenticationAction {
	private Map<String,Object> attrs=new HashMap<String,Object>();
	private String label="WSIJWTAuthorize";
	private String name=null;
	public WSIJWTAuthorizeAction(JSONObject jo) {
		try {
			label=jo.getString("label");
		}
		catch(JSONException e) {
			label="WSIJWTAuthorize";
		}
		try {
			name=jo.getString("name");
		}
		catch(JSONException e) {
			label="WSIJWTAuthorize";
		}
		try {
			JSONObject claims=jo.getJSONObject("claims");
			attrs=claims.toMap();
		}
		catch(JSONException e) {
		}
	}

	public String getLabel() {
		if(label!=null) return label;
		else return "WSIJWTAuthorize";
	}
	public String getName() {
		if(name!=null) return name;
		else {
			StringBuffer sb=new StringBuffer();
			for(Iterator<String> i=attrs.keySet().iterator();i.hasNext();) {
				String n=i.next();
				sb.append(n+"="+attrs.get(n)+":");
			}
			return sb.toString().substring(0,40);
		}
	}

	@Override
	public boolean isAuthenticated() {
		return true;
	}

	@Override
	public boolean isAuthorized() {
		return false;
	}

	@Override
	public void performAction(MbJavaComputeNode node, MbMessageAssembly assy) throws WSIException {
		try {
			MbRoute route=node.getRoute(label);
			LocalEnvironment le=new LocalEnvironment(assy.getLocalEnvironment().getRootElement());
			StringBuffer sb=new StringBuffer();
			for(Iterator<String> i=attrs.keySet().iterator();i.hasNext();) {
				String n=i.next();
				sb.append(n+"="+attrs.get(n)+":");
			}
			le.setValue("JWT_CLAIMS", sb.toString());
			if(route!=null) route.propagate(assy);
		} catch(MbException e) {
			throw new WSIException("No route found for label "+label);
		} catch (IllegalAccessException e) {
			throw new WSIException("Cannot set JWT_CLAIMS");
		}
		
	}
}
