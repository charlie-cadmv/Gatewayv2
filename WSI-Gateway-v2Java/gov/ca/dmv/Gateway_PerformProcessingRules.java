package gov.ca.dmv;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;

public class Gateway_PerformProcessingRules extends MbJavaComputeNode {
	WSIGatewayProcess process=new WSIGatewayProcess();
	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		//MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		try {
			// create new message as a copy of the input
			MbMessage outMessage = new MbMessage(inMessage);
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			// ----------------------------------------------------------
			// Add user code below
			MbElement le=inAssembly.getLocalEnvironment().getRootElement();
			String url=MsgUtility.getElementAsString(le, "path");
			WSIGatewayActions actions=process.getActions(url);
			if(actions!=null) {
				//
				WSITokenAction tokens=actions.getTokens();
				if(tokens!=null) tokens.performAction(this,outAssembly);
				//
				WSIRequestMediationAction mediation=actions.getRequestMediation();
				if(mediation!=null) mediation.performAction(this,outAssembly);
				//
				WSIRequestAction request=actions.getRequest();
				if(request!=null) request.performAction(this,outAssembly);
				//
				WSIResponseMediationAction respMediation=actions.getResponseMediation();
				if(respMediation!=null) respMediation.performAction(this,outAssembly);
				MbElement resp=MsgUtility.getElement(le, "response");
				resp=MsgUtility.getMessage(resp);
				if(resp!=null) {
					MbElement root=outMessage.getRootElement();
					MsgUtility.removeMessage(root);
					root.addAsLastChild(resp.copy());
				}
			} else throw new Exception("Cannot find valid/active actions for "+url);
			// End of user code
			// ----------------------------------------------------------
		} catch (MbException e) {
			// Re-throw to allow Broker handling of MbException
			throw e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			// Re-throw to allow Broker handling of RuntimeException
			throw e;
		} catch (Exception e) {
			// Consider replacing Exception with type(s) thrown by user code
			// Example handling ensures all exceptions are re-thrown to be handled in the flow
			throw new MbUserException(this, "evaluate()", "", "9999", e.toString(),	null);
		}
		// The following should only be changed
		// if not propagating message to the 'out' terminal
		out.propagate(outAssembly);

	}

}
