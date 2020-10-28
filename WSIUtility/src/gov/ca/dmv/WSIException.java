package gov.ca.dmv;

import java.util.UUID;

public class WSIException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int USER_TYPE=0;
	public static final int SYSTEM_TYPE=1;
	private UUID uuid=UUID.randomUUID();
	private int type=SYSTEM_TYPE;
	private String uerror=null;

	public WSIException(String s) {
		this(s,SYSTEM_TYPE);
	}

	public WSIException(String s,int t) {
		this(s,t,"System error");
	}
	public WSIException(String s,int t,String ue) {
		super(s);
		uerror=ue;
		if(t==USER_TYPE||type==SYSTEM_TYPE) type=t;
		else type=SYSTEM_TYPE;
	}
	public String getUserError() {
		if(type==USER_TYPE) return getMessage();
		else return uerror+" w/correlation "+uuid;
	}
}
