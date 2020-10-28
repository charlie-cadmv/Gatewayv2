package gov.ca.dmv.jwt.auth;

import gov.ca.dmv.LocalEnvironment;

import java.util.Map;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;

public class WSIJWTUserAuthenticationSubflow_JWTAuthorize extends
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
			DecodedJWT djwt=null;
			if(djwt==null) {
				String keyFile=le.getString("JWTKeyFile");
				String algorithm=le.getString("JWTAlgorithm");
				String token=le.getString("JWT_TOKEN");
				JWTUtility jwt=new JWTUtility(keyFile,algorithm);
				Verification v=jwt.getBaseVerification();
				djwt=v.build().verify(token);
			}
			Map<String,Claim> claims=djwt.getClaims();
			//Map<String,String> aclaims=new HashMap<String,String>();
			String c=le.getString("JWT_CLAIMS");
			String[] cp=c.split(":");
			for(int i=0;i<cp.length;i++) {
				String[] cpp=cp[i].split("=");
				@SuppressWarnings("unused")
				Claim claim=claims.get(cpp[0]);
				/*
				if(claim.asString().equals(cpp[]) {
					
				}
				claim.asBoolean();
				claim.asDate();
				claim.asList(arg0);
				claim.asDouble();
				claim.asInt();
				claim.asMap();
				*/
						
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
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		}
		// The following should only be changed
		// if not propagating message to the 'out' terminal
		out.propagate(outAssembly);

	}

}
