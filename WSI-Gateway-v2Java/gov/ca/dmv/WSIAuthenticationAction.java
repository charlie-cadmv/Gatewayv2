package gov.ca.dmv;

public interface WSIAuthenticationAction {
	// these actions will use Authorization HTTP header
	public boolean isAuthenticated();
	public boolean isAuthorized();

}
