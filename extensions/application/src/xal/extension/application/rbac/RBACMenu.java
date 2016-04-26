package xal.extension.application.rbac;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import xal.extension.application.Application;
import xal.tools.IconLib;

/**
 * A RBAC menu for usage in Menubar. This menu displays the active username and
 * contains a button for user change.
 * 
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
public class RBACMenu extends JMenu {

    private static final long serialVersionUID = 3084497805765227445L;

    /**
     * Constructs the RBAC.
     */
    public RBACMenu() {
        super(getUsername());
        createMenu();
    }

    /**
     * Creates all the components of the object.
     */
    private void createMenu() {
        this.setIcon(IconLib.getIcon("general", "User16.gif"));
        this.setEnabled(true);

        JMenuItem changeUserItem = new JMenuItem("Change User");
        changeUserItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Application.getApp().changeRBACUser();
                updateUsername();
            }
        });
        this.add(changeUserItem);
    }

    /**
     * Updates the displayed username to the current logged in RBAC user.
     */
    private void updateUsername() {
        this.setText(getUsername());
    }

    /**
     * Retrieves the RBAC username from the application.
     * 
     * @return username
     */
    private static String getUsername() {
        return Application.getApp().getRbacSubject().getUserInfo().getUsername();
    }
}
