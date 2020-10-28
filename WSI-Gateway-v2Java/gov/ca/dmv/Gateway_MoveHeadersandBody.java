package gov.ca.dmv;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;

public class Gateway_MoveHeadersandBody extends MbJavaComputeNode {
	public void onInitialize() throws MbException
	{
		new WSIGatewayProcess();
	}

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
				//elem.detach();
				MbElement save=MsgUtility.getElement(le, "?inputRequest");
				MsgUtility.removeAllChildren(save);
				save.addAsLastChild(elem.copy());
				MbElement request=MsgUtility.getElement(le, "?request");
				MsgUtility.removeAllChildren(request);
				request.addAsLastChild(elem.copy());
			}
			MbElement header=MsgUtility.getElement(root,"HTTPInputHeader");
			if(header!=null) {
				MbElement head=MsgUtility.getElement(le, "?HTTPInputHeader");
				header.detach();
				head.delete();
				le.addAsLastChild(header);
			}
			String baseUrl=null;
			int index=-1;
			String url=null;
			String cmd=MsgUtility.getElementAsString(le, "HTTPInputHeader/X-Original-HTTP-Command");
			if(cmd!=null) {
				String[] cmd_parts=cmd.split(" ");
				url=cmd_parts[1];
			//
				index=url.indexOf(WSIConfig.baseUrlREST);
				if(index>=0) {
					baseUrl=WSIConfig.baseUrlREST;
					le.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,"callType","REST");
				} else {
					index=url.indexOf(WSIConfig.baseUrlWS);
					if(index>=0) {
						baseUrl=WSIConfig.baseUrlWS;
						le.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,"callType","WS");
					}
					else throw new Exception("Invalid URL used to access gateway: "+url);
				}
			} else throw new Exception("Missing X-Original-HTTP-Command header on request");
			le.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,"path",url.substring(index+baseUrl.length()));
			le.createElementAsLastChild(MbElement.TYPE_NAME_VALUE,"baseURL",baseUrl);
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
