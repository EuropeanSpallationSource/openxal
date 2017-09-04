/*
 * Copyright (C) 2017 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package xal.app.configurator;

import javafx.scene.control.CheckBox;

import xal.extension.application.rbac.RBACPlugin;

public class RBAC {

    private final CheckBox rbacCheckBox;

    private boolean useRBACLogin;

    public RBAC(CheckBox rbacCheckBox) {
        this.rbacCheckBox = rbacCheckBox;
    }

    public void enable() {
        RBACPlugin.enableRBACLogin();
    }

    public void disable() {
        RBACPlugin.disableRBACLogin();
    }

    public boolean isEnabled() {
        return RBACPlugin.useRBACLogin();
    }

    public void updateStatus() {
        useRBACLogin = isEnabled();
        
        if (rbacCheckBox != null) {
            if (useRBACLogin) {
                rbacCheckBox.setText("RBAC is enabled");
            } else {
                rbacCheckBox.setText("RBAC is disabled");
            }
        }
    }
}
