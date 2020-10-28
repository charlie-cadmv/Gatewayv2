package gov.ca.dmv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;

public class LocalEnvironment {
	private MbElement le=null;
	public LocalEnvironment(MbElement l) {
		le=l;
		//check le for name?
	}
	private MbElement findElement(String s) throws MbException, IllegalAccessException {
		//use ? and + syntax ins to create new elements
		if(le==null) throw new IllegalAccessException("LocalEnvironment has not been initialized");
		@SuppressWarnings("unchecked")
		List<MbElement> l=(List<MbElement>) le.evaluateXPath(s);
		if(l.size()==0) return null;
		return l.get(0);
	}
	public String getString(String n) throws IllegalAccessException {
		try {
			MbElement elem=findElement(n);
			if(elem==null) return null;
			Object val=elem.getValue();
			if(val!=null && val instanceof String) return (String)val;
			else throw new IllegalAccessException();
		} catch(MbException e) {
			throw new IllegalAccessException("MbException is "+e.getMessage());
		}
	}
	public Integer getInteger(String n) throws IllegalAccessException {
		try {
			MbElement elem=findElement(n);
			if(elem==null) return null;
			Object val=elem.getValue();
			if(val!=null && val instanceof Integer) return (Integer)val;
			else throw new IllegalAccessException("LocalEnvironment "+n+" is not a String");
		} catch(MbException e) {
			throw new IllegalAccessException("MbException is "+e.getMessage());
		}
	}
	public void setValue(String n,Object v) throws IllegalAccessException {
		try {
			MbElement elem=findElement(n);
			if(elem==null) throw new IllegalAccessException("LocalEnvironment "+n+" is not in LocalEnvironment");
			elem.setValue(v);
		} catch(MbException e) {
			throw new IllegalAccessException("MbException is "+e.getMessage());
		}
	}
	public void clearElement(String n) throws IllegalAccessException {
		try {
			MbElement elem=findElement(n);
			if(elem==null) throw new IllegalAccessException("LocalEnvironment "+n+" is not present");
			elem.setValue(null);
		} catch(MbException e) {
			throw new IllegalAccessException("MbException is "+e.getMessage());
		}
	}
	public void deleteElement(String n) throws IllegalAccessException {
		try {
			MbElement elem=findElement(n);
			if(elem==null) throw new IllegalAccessException("LocalEnvironment "+n+" is not present");
			elem.delete();
		} catch(MbException e) {
			throw new IllegalAccessException("MbException is "+e.getMessage());
		}
		
	}
	public Date getDate(String n) throws IllegalAccessException {
		try {
			MbElement elem=findElement(n);
			if(elem==null) return null;
			Object val=elem.getValue();
			if(val!=null && val instanceof String) {
				try {
					SimpleDateFormat sdf=new SimpleDateFormat(); //dow mon dd hh:mm:ss zzz yyyy
					return sdf.parse((String)val);
				} catch(ParseException e) {
					return new Date();
				}
			}
			else throw new IllegalAccessException();
		} catch(MbException e) {
			throw new IllegalAccessException("MbException is "+e.getMessage());
		}
	}
}
