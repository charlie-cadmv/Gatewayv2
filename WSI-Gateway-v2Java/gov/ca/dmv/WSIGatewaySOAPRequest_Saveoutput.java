package gov.ca.dmv;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;
import com.ibm.broker.plugin.MbXMLNSC;

public class WSIGatewaySOAPRequest_Saveoutput extends MbJavaComputeNode {

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
			MbElement root=outMessage.getRootElement();
			MbElement le=inAssembly.getLocalEnvironment().getRootElement();
			MbElement elem=MsgUtility.getMessage(root); // this might point to a SOAP element
			if(elem!=null) {
				
				if(elem.getName().equals("SOAP")) { // need to change parser to XMLNSC
					elem.setName("Envelope");
					elem.setNamespace("http://schemas.xmlsoap.org/soap/envelope/");
					elem.detach();
					MbElement context=MsgUtility.getElement(elem, "Context");
					if(context!=null) context.delete();
					MbElement body=MsgUtility.getElement(elem, "Body");
					if(body!=null) body.setNamespace("http://schemas.xmlsoap.org/soap/envelope/");
					MbElement save=MsgUtility.getElement(le, "?responseService");
					if(save!=null) {
						MsgUtility.removeAllChildren(save);
						MbElement elem2=save.createElementAsFirstChild(MbXMLNSC.PARSER_NAME);
						elem2.addAsLastChild(elem.copy());
					}
					MbElement response=MsgUtility.getElement(le, "?response");
					if(response!=null) {
						MsgUtility.removeAllChildren(response);
						MbElement r2=response.createElementAsFirstChild(MbXMLNSC.PARSER_NAME);
						r2.addAsLastChild(elem);
					}
				} else { // this should be an XMLNSC element or similar
					elem.detach();
					MbElement save=MsgUtility.getElement(le, "?responseService");
					if(save!=null) {
						MsgUtility.removeAllChildren(save);
						save.addAsLastChild(elem.copy());
					}
					MbElement response=MsgUtility.getElement(le, "?response");
					if(response!=null) {
						MsgUtility.removeAllChildren(response);
						response.addAsLastChild(elem);
					}

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

	}

}
