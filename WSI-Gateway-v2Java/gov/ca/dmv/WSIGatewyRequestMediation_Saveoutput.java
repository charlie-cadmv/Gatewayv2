package gov.ca.dmv;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;

import gov.ca.dmv.MsgUtility;

import java.util.List;


public class WSIGatewyRequestMediation_Saveoutput extends MbJavaComputeNode {

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
			MbElement elem=MsgUtility.getMessage(root);
			if(elem!=null) {
				elem.detach();
				@SuppressWarnings("unchecked")
				MbElement save=((List<MbElement>)le.evaluateXPath("?mediatedRequest")).get(0);
				MsgUtility.removeAllChildren(save);
				save.addAsLastChild(elem.copy());
				@SuppressWarnings("unchecked")
				MbElement request=((List<MbElement>)le.evaluateXPath("?request")).get(0);
				MsgUtility.removeAllChildren(request);
				request.addAsLastChild(elem);
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
