package gov.ca.dmv;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.List;

import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessageAssembly;

public class ExceptionHandler {
	public static String encodeException(Exception e) {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos=new ObjectOutputStream(baos);
			oos.writeObject(e);
			byte[] b=baos.toByteArray();
			return Base64.getEncoder().encodeToString(b);
		} catch (IOException e1) {
			System.out.println("Cannot serialize object");
		}
		return null;
	}
	public static Exception decodeException(String s) {
		byte[] ib=Base64.getDecoder().decode(s);
		ByteArrayInputStream bais=new ByteArrayInputStream(ib);
		ObjectInputStream ois;
		Exception val=null;
		try {
			ois = new ObjectInputStream(bais);
			val=(Exception)ois.readObject();
		} catch (IOException e) {
			System.out.println("String does not contain an Exception object");
		} catch (ClassNotFoundException e) {
			System.out.println("Cannot find Exception type "+e.getMessage());
		}
		return val;
	}
	public static WSIError processException(MbMessageAssembly assy) {
		try {
			MbElement eexcp=assy.getExceptionList().getRootElement();
			MbElement el=eexcp.getFirstChild();
			if(el!=null) return processException(el);
			else return new WSIError("",0);
		} catch(MbException e) {
		    e.printStackTrace();
		    return new WSIError("",0);
		}
	}

	public static WSIError processException(MbElement svc) {
		StringBuffer sb=new StringBuffer();
		int msg=0;
		try {
			MbElement el=svc.getFirstChild();
		    while(el!=null) {
		            if(el.getName().equalsIgnoreCase("Text")) sb.append(el.getValueAsString()+" ");
		            if(el.getName().equalsIgnoreCase("Insert")) {
		                MbElement insel=el.getLastChild();
		                String inselStr=insel.getValueAsString().trim();
		                if(inselStr.length()>0) sb.append(inselStr+" ");
		            }
		            if(el.getName().equalsIgnoreCase("Number")) {
		            	msg=(int) el.getValue();
		            }
		            if(el.getName().contains("Exception")) {
		                return processException(el);
		            }
		            el=el.getNextSibling();
		        }
		    } catch(MbException e) {
		        e.printStackTrace();
		    }
		    return new WSIError(sb.toString(),msg);
		}
		public static void setException(MbMessageAssembly assy,Exception excp) throws Exception {
			try {
				MbElement root=assy.getLocalEnvironment().getRootElement();
				@SuppressWarnings("unchecked")
				List<MbElement> a=(List<MbElement>)root.evaluateXPath("?ExceptionList");
				MbElement elem=a.get(0);
				elem.setValue(encodeException(excp));
			} catch(MbException e) {
				throw new WSIException("Cannot copy Exception to Environment");
			}	
		}

		public static Exception getException(MbMessageAssembly assy) throws WSIException {
			try {
				MbElement rest=assy.getLocalEnvironment().getRootElement();
				MbElement eexcp=rest.getFirstElementByPath("ExceptionList");
				return decodeException(eexcp.getValueAsString());
			} catch(MbException e) {
				throw new WSIException("Cannot return ExceptionList from Environment "+e.getMessage());
			}
		}
}
