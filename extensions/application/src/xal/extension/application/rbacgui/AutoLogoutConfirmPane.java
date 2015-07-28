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
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import se.esss.ics.rbac.access.Token;

/**
 * 
 * <code>AutoLogoutConfirmPane</code> is a pane that displays the auto logout message and counts down from the timeout.
 * After the timeout elapses the dialog closes automatically, signalling that the user chose to confirm the logout.
 * 
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 * 
 */
public class AutoLogoutConfirmPane extends JOptionPane {
    private static final long serialVersionUID = -832188649399294844L;

    private final String username;
    private final Timer timer;
    private int timeout;
    private JDialog dialog;

    /**
     * Constructs a new confirmation pane.
     * 
     * @param token the token for which it is constructed
     * @param initialTimeout the initial timeout, from which the pane will count down
     */
    public AutoLogoutConfirmPane(Token token, int initialTimeout) {
        super("", JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        this.username = token.getUsername();
        this.timeout = initialTimeout;
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeout--;
                updateMessage(timeout);
                if (timeout == 0) {
                    dialog.dispose();
                    timer.stop();
                }
            }
        });
        timer.start();
        updateMessage(timeout);
    }

    /**
     * Updates the message of this pane with a new timeout value.
     * 
     * @param timeout the timeout in seconds
     */
    protected void updateMessage(int timeout) {
        setMessage("Due to inactivity the user " + username + " will be signed out in " + timeout
                + " seconds.\nClick OK to confirm or Cancel to abort the signout.");
    }

    /**
     * Creates a dialog that contains this pane.
     * 
     * @param parent the parent of this dialog
     * @return the dialog
     * @throws HeadlessException if <code>GraphicsEnvironment.isHeadless</code> returns <code>true</code>
     */
    public JDialog createDialog(Component parent) throws HeadlessException {
        dialog = super.createDialog(parent, "Confirm Signout");
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                timer.stop();
            }
        });
        return dialog;
    }

    /**
     * Returns <code>true</code> if the user confirmed the logout or if the dialog closed automatically, or
     * <code>false</code> if the user chose to cancel the logout process.
     * 
     * @return true if logout is confirmed or false otherwise
     */
    public boolean isConfirmed() {
        Object o = getValue();
        if (o == UNINITIALIZED_VALUE) {
            return true;
        }
        if (o instanceof Integer) {
            return ((Integer) o).intValue() == JOptionPane.OK_OPTION;
        }
        return true;
    }
}
