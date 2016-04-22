package xal.rbac;

/**
 * <code>RBACLoginInfo</code> provides RBAC login details such as username.
 *
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
public final class RBACUserInfo {
	private final String username;
	private final String firstName;
	private final String lastName;
	
	/**
	 * Constructor of RBACLoginInfo object.
	 * @param username Username of the RBAC user, must not be empty or null.
	 * @param firstName First name of the RBAC user.
	 * @param lastName Last name of the RBAC user.
	 * 
	 * @throws IllegalArgumentException if username is empty or null
	 */
	public RBACUserInfo(String username, String firstName, String lastName) {
		if (username == null || username.isEmpty()) {
			throw new IllegalArgumentException("RBAC username must not be null or empty.");
		}
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	public String getUsername() {
		return username;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
}
