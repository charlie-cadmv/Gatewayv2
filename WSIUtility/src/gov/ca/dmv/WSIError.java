package gov.ca.dmv;

public class WSIError {
	private int msg=0;
	private String text=null;
	public WSIError(String s,int n) {
		text=s;
		msg=n;
	}
	public String getText() {
		return text;
	}
	public int getMsgNum() {
		return msg;
	}
	public int getType() {
		// TODO Auto-generated method stub
		switch(msg) {
			case 2702:
				return WSIException.USER_TYPE;
			default:
				return WSIException.SYSTEM_TYPE;
		}
	}
}
