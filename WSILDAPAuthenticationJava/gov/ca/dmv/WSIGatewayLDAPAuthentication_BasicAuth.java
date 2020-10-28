package gov.ca.dmv;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbRoute;
import com.ibm.broker.plugin.MbUserException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class WSIGatewayLDAPAuthentication_BasicAuth extends MbJavaComputeNode {
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
			MbElement root=inAssembly.getLocalEnvironment().getRootElement();
			LocalEnvironment le=new LocalEnvironment(root);
			String user=le.getString("LDAP_UID");
			String pwd=le.getString("LDAP_PWD");
			if(user==null || pwd==null) {
				String auth=le.getString("HTTPInputHeader/Authorization");
				if(auth!=null) {
					int ix1=auth.indexOf("Basic");
					if(ix1<0) throw new Exception("Not Basic authorization type");
					auth=auth.substring(ix1+"Basic".length()).trim();
					auth=new String(Base64.decode(auth));
					String[] uid=auth.split(":");
					if(uid.length>0) {
						root.evaluateXPath("?LDAP_UID[set-value('"+uid[0]+"')]");
						root.evaluateXPath("?LDAP_PWD[set-value('"+uid[1]+"')]");
					} else throw new WSIException("Invalid Authorization header format");
				} else throw new WSIException("Must provide HTTP Authorization header to use LDAP Authentication");
			}
			//now route to label
			String jump=le.getString("JUMP_LABEL");
			if(jump!=null) {
				MbRoute r=this.getRoute(jump);
				if(r!=null) r.propagate(inAssembly);
				else {
					WSIConfig.log.error("No route found for label "+jump+" in LDAPUser");
					throw new WSIException("No route found for label "+jump+" in LDAPUser");
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
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		}
		// The following should only be changed
		// if not propagating message to the 'out' terminal
		out.propagate(outAssembly);
System.out.println();
	}

}
