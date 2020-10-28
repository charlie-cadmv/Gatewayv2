package gov.ca.dmv.jwt.auth;

import gov.ca.dmv.LocalEnvironment;

import java.util.Date;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;

public class WSIJWTUserAuthenticationSubflow_JWTAuthenticate extends
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
			LocalEnvironment le=new LocalEnvironment(inAssembly.getLocalEnvironment().getRootElement());
			if(le.getString("JWT_DECODED")!=null) {
				String keyFile=le.getString("JWTKeyFile");
				Date expiresAt=le.getDate("JWTExpiresAt");
				String algorithm=le.getString("JWTAlgorithm");
				String token=le.getString("JWT_TOKEN");
				JWTUtility jwt=new JWTUtility(keyFile,algorithm);
				Verification v=jwt.getBaseVerification().acceptExpiresAt(expiresAt.getTime());
				DecodedJWT t=v.build().verify(token);
				le.setValue("?JWT_DECODED", t);
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
