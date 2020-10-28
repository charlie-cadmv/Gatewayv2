package gov.ca.dmv.jwt.auth;

import gov.ca.dmv.LocalEnvironment;
import gov.ca.dmv.WSIConfig;
import gov.ca.dmv.WSIException;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbRoute;
import com.ibm.broker.plugin.MbUserException;

public class WSIJWTUserAuthenticationSubflow_TokenAuth extends
		MbJavaComputeNode {

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
			//now route to label
			MbElement root=inAssembly.getLocalEnvironment().getRootElement();
			LocalEnvironment le=new LocalEnvironment(root);
			String token=le.getString("JWT_TOKEN");
			if(token==null) {
				String auth=le.getString("HTTPInputHeader/Authorization");
				if(auth!=null) {
					int ix1=auth.indexOf("Bearer");
					if(ix1<0) throw new Exception("Not JWT token type");
					le.setValue("?JWT_TOKEN",auth.substring("Bearer".length()+1));
				} else throw new WSIException("Must provide HTTP Authorization header to use JWT token Authentication");
			}

			String jump=le.getString("JUMP_LABEL");
			if(jump!=null) {
				MbRoute r=this.getRoute(jump);
				if(r!=null) r.propagate(inAssembly);
				else {
					WSIConfig.log.error("No route found for label "+jump+" in WSIJWTUserAuthenticate");
					throw new WSIException("No route found for label "+jump+" in WSIJWTUserAuthenticate");
				}
			}

			// End of user code
			// ----------------------------------------------------------
		} catch (MbException e) {
			// Re-throw to allow Broker handling of MbException
			throw e;
		} catch (RuntimeException e) {
			// Re-throw to allow Broker handling of RuntimeException
			throw e;
		} catch (Exception e) {
			// Consider replacing Exception with type(s) thrown by user code
			// Example handling ensures all exceptions are re-thrown to be handled in the flow
			throw new MbUserException(this, "evaluate()", "", "9999", e.toString(),
					null);
		}
		// The following should only be changed
		// if not propagating message to the 'out' terminal
		out.propagate(outAssembly);

	}

}
