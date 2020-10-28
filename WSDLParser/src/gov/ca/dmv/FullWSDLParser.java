package gov.ca.dmv;

import groovy.xml.QName;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.predic8.schema.ComplexType;
import com.predic8.schema.Element;
import com.predic8.schema.Schema;
import com.predic8.schema.Sequence;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Types;
import com.predic8.wsdl.WSDLParser;

public class FullWSDLParser {

	public static void main(String[] args) {
		WSDLParser parser = new WSDLParser();
	
		Definitions defs = parser.parse(args[0]);
		System.out.println("swagger: \"2.0\"");
		System.out.println("basePath: ");
		System.out.println("info:");
		System.out.println("  description: ");
		System.out.println("  title: ");
		System.out.println("  version: ");
		System.out.println("host: ");
		System.out.println("consumes:");
		System.out.println("- \"application/json\"");
		System.out.println("produces:");
		System.out.println("- \"application/json\"");
		System.out.println("definitions:");
		Types types=defs.getTypes().get(0);
		List<Schema> lschemas=types.getAllSchemas();
		for(Iterator<Schema> i=lschemas.iterator();i.hasNext();) {
			Schema s=i.next();
			List<ComplexType> lct=s.getComplexTypes();
			for(Iterator<ComplexType> j=lct.iterator();j.hasNext();) {
				ComplexType ct=j.next();
				System.out.println("  "+ct.getName()+":");
				List<QName> lst=ct.getSuperTypes();
				if(lst.size()>0) {
					for(Iterator<QName> l=lst.iterator();l.hasNext();) {
						QName qn=l.next();
						System.out.println("  *** Needs a fix ***");
						System.out.println("  super is "+qn.getLocalPart());
						System.out.println("  "+ct.getAsString());
					}
				}
				Sequence sq=ct.getSequence();
				if(sq!=null) {
					List<String> required=new ArrayList<String>();
					System.out.println("    properties:");
					List<Element> lelem=sq.getElements();
					for(Iterator<Element> k=lelem.iterator();k.hasNext();) {
						Element el=k.next();
						String name=el.getName();
						QName type=el.getType();
						String typeName=(type!=null?el.getType().getLocalPart():"null");
						QName ref=el.getRef();
						//String refName=(ref!=null?el.getRef().toString():"null");
						String refpart=(ref!=null?el.getRef().getLocalPart():"null");
						String maxString=el.getMaxOccurs();
						int max=0;
						if(maxString!=null) {
							if(maxString.equalsIgnoreCase("unbounded")) max=-1;
							else max=Integer.parseInt(maxString);
						}
						String minString=el.getMinOccurs();
						int min=0;
						if(minString!=null) min=Integer.parseInt(minString);
						System.out.println("      "+name+":");
						if(max>1 || max==-1) {
							System.out.println("        type: array");
							System.out.println("        items:");
							System.out.println("         -  "+typeName);
						}
						else {
							if(type!=null) System.out.println("        type: "+typeName);
						}
						if(ref!=null) System.out.println("         $ref: "+refpart);
						if(min>0) {
							required.add(name);
						}
						System.out.println("    "+max+" "+min);
					}

				}
			}
		}
		System.out.println("paths:");
		List<Operation> ops=defs.getOperations();
		for(Iterator<Operation> i=ops.iterator();i.hasNext();) { 
			Operation op=i.next();
			System.out.println("  /"+op.getName());
			System.out.println("    post:");
			System.out.println("      operationId:"+op.getName());
			System.out.println("        parameters:");
			System.out.println("        - name: \"body\"");
			System.out.println("        in: \"body\"");
			System.out.println("        schema:");
			System.out.println("          $ref: \"#/definitions/"+defs.getMessage(op.getInput().getMessage().getName()).getParts().get(0).getElement().getName()+"\"");
;
		    System.out.println("        responses:");
		    System.out.println("          200:");
		    System.out.println("            description: \"OK response\"");
		    System.out.println("            schema:");
		    System.out.println("              $ref: \"#/definitions/"+defs.getMessage(op.getOutput().getMessage().getName()).getParts().get(0).getElement().getName()+"\"");
		}

	}
}