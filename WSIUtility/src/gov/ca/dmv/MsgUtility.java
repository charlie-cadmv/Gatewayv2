package gov.ca.dmv;

import java.util.List;

import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessageAssembly;

public class MsgUtility {
	public final static String[] msgdomain={"JSON","XMLNSC","SOAP"};
	public static void removeAllChildren(MbElement s) throws MbException {
		MbElement next=s.getFirstChild();
		while(next!=null) {
			MbElement n=next.getNextSibling();
			next.delete();
			next=n;	
		}	
	}
	public static void removeMessage(MbElement s) throws MbException {
		MbElement next=s.getFirstChild();
		while(next!=null) {
			MbElement n=next.getNextSibling();
			for(int i=0;i<msgdomain.length;i++) {
				if(next.getName().equals(msgdomain[i])) {
					next.delete();
					break;
				}
			}
			next=n;	
		}	
	}
	public static MbElement getMessage(MbElement a) throws MbException {
		MbElement elem=null;
		for(int i=0;i<msgdomain.length;i++) {
			@SuppressWarnings("unchecked")
			List<MbElement> l=((List<MbElement>)a.evaluateXPath(msgdomain[i]));
			if(l!=null && l.size()>0) {
				elem=l.get(0);
				break;
			}
		}
		return elem;
	}
	public static void setupCall(MbMessageAssembly assy,String a) throws WSIException  {
		try {
			MbElement le=assy.getLocalEnvironment().getRootElement();
			MbElement m=assy.getMessage().getRootElement();
			removeMessage(m);
			MbElement r=getElement(le,a); //get request mediateRequest or response from environment
			if(r!=null) {
				m.addAsLastChild(r.getFirstChild().copy());
				MbElement h=getElement(m,"HTTPInputHeader");
				if(h!=null) h.delete();
				//MbElement henv=getElement(le,"HTTPInputHeader");
				//if(henv!=null) m.addAsLastChild(henv.copy());
			}
		}
		catch(MbException e) {
			e.printStackTrace();
			throw new WSIException("MbException "+e.getMessage());
		}
	}
	public static MbElement getElement(MbElement base,String s) throws MbException {
		MbElement elem=null;
		@SuppressWarnings("unchecked")
		List<MbElement> elementList=(List<MbElement>)base.evaluateXPath(s);
		if(elementList!=null && elementList.size()>0) elem=elementList.get(0);
		return elem;

	}
	public static String getElementAsString(MbElement base,String s) throws MbException {
		return getElement(base,s).getValueAsString();
	}

}
