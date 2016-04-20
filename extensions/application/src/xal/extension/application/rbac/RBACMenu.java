package xal.extension.application.rbac;

import javax.swing.JMenu;

import xal.rbac.RBACUserInfo;
import xal.tools.IconLib;

/**
 * A RBAC menu for usage in Menubar.
 * This menu currently only displays the active username.
 * 
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
public class RBACMenu extends JMenu {

	private static final long serialVersionUID = 3084497805765227445L;

	/**
	 * Constructs the basic menu from the RBAC information.
	 * @param userInfo Information on RBAC user.
	 */
	public RBACMenu(RBACUserInfo userInfo) {
		super(userInfo.getUsername());
		this.setIcon(IconLib.getIcon("general", "User16.gif"));
		this.setDisabledIcon(IconLib.getIcon("general", "User16.gif")); // Remove graying of icon
		this.setEnabled(false);
	}
}
