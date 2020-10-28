package gov.ca.dmv;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbMessageAssembly;


public abstract class WSIGatewayAction {
	public abstract void performAction(MbJavaComputeNode node, MbMessageAssembly assy) throws WSIException;

}
