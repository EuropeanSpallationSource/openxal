/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 * 
 * This file is part of RBAC.
 * RBAC is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free 
 * Software Foundation, either version 2 of the License, or any newer version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package xal.extension.application.rbacgui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import se.esss.ics.rbac.access.Credentials;

/**
 * 
 * <code>AuthenticationPane</code> is an option pane that shows the username and password field, where user can input
 * his credentials that should be used for authentication.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 * 
 */
public class AuthenticationPane extends JOptionPane {
    private static final long serialVersionUID = -832188649399294844L;

    private static class LoginPanel extends JPanel {
        private static final long serialVersionUID = 694419876299762198L;

        private JTextField tfUsername;
        private JPasswordField pfPassword;
        private JComboBox<String> rolePickerCombo;

        LoginPanel() {
            createGUI();
        }

        Credentials getCredentials() {
            return new Credentials(tfUsername.getText(), pfPassword.getPassword());
        }

        private void createGUI() {

            tfUsername = new JTextField();
            pfPassword = new JPasswordField();

            setPreferredSize(new Dimension(320, 64));
            setLayout(new GridBagLayout());

            add(new JLabel("Username:"), new GridBagConstraints(0, 0, 1, 1, 0, 1, GridBagConstraints.WEST,
                    GridBagConstraints.HORIZONTAL, new Insets(2, 4, 2, 4), 0, 0));
            add(tfUsername, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST,
                    GridBagConstraints.HORIZONTAL, new Insets(2, 4, 2, 4), 0, 0));

            add(new JLabel("Password:"), new GridBagConstraints(0, 1, 1, 1, 0, 1, GridBagConstraints.WEST,
                    GridBagConstraints.HORIZONTAL, new Insets(2, 4, 2, 4), 0, 0));
            add(pfPassword, new GridBagConstraints(1, 1, 1, 1, 1, 1, GridBagConstraints.WEST,
                    GridBagConstraints.HORIZONTAL, new Insets(2, 4, 2, 4), 0, 0));
        }

    }

    /**
     * Constructs a new authentication pane.
     */
    public AuthenticationPane() {
        super(new LoginPanel(), JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    }

    /**
     * Creates a dialog that contains this pane.
     * 
     * @param parent
     *            the parent of this dialog
     * @return the dialog
     * @throws HeadlessException
     *             if <code>GraphicsEnvironment.isHeadless</code> returns <code>true</code>
     */
    public JDialog createDialog(Component parent) throws HeadlessException {
        JDialog dialog = super.createDialog(parent, "Sign In");
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                ((LoginPanel) getMessage()).tfUsername.requestFocus();
            }
        });
        ((LoginPanel) getMessage()).tfUsername.requestFocus();
        return dialog;
    }

    /**
     * Returns the credentials if the OK button was pressed or null otherwise.
     * 
     * @return the credentials
     */
    public Credentials getCredentials() {
        Object o = getValue();
        if (o == UNINITIALIZED_VALUE) {
            return null;
        }
        if (o instanceof Integer) {
            return ((Integer) o).intValue() == JOptionPane.OK_OPTION ? ((LoginPanel) getMessage()).getCredentials()
                    : null;
        }
        return null;
    }
}
