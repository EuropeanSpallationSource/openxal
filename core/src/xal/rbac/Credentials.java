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
package xal.rbac;

import java.util.Arrays;

/**
 * Credentials carry the login information. They are created by the SecurityCallback during the 
 * authentication and are discarded immediately afterwards.
 * 
 * @author <a href="mailto:jakob.battelino@cosylab.com">Jakob Battelino Prelog</a>
 */
public class Credentials {

    private String username;
    private char[] password;
    private String preferredRole;
    private String ip;

    /**
     * Constructs new Credentials. Preferred role and IP are set to null.
     * 
     * @param username the username used for authentication
     * @param password the password used for authentication
     */
    public Credentials(String username, char[] password) {
        this(username, password, null, null);
    }

    /**
     * Constructs new credentials with fields set to the provided values. IP is set to null.
     * 
     * @param username that will be used for authentication.
     * @param password that will be used for authentication.
     * @param preferredRole the user would prefer to have.
     */
    public Credentials(String username, char[] password, String preferredRole) {
        this(username, password, preferredRole, null);
    }

    /**
     * Constructs new credentials with fields set to the provided values. 
     * 
     * @param username that will be used for authentication.
     * @param password that will be used for authentication.
     * @param preferredRole the user would prefer to have.
     * @param ip address that will be used for authentication.
     */
    public Credentials(String username, char[] password, String preferredRole, String ip) {
        this.username = username;
        this.password = password == null ? null : Arrays.copyOf(password, password.length);
        this.preferredRole = preferredRole;
        this.ip = ip;
    }

    /**
     * Returns username that will be used for authentication.
     * 
     * @return username that will be used for authentication.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns password that will be used for authentication.
     * 
     * @return password that will be used for authentication.
     */
    public char[] getPassword() {
        if (password == null) {
            return new char[0];
        }
        return Arrays.copyOf(password, password.length);
    }

    /**
     * Returns the name of the role the user would prefer to have.
     * 
     * @return the name of the role the user would prefer to have.
     */
    public String getPreferredRole() {
        return preferredRole;
    }

    /**
     * Returns IP address that will be used for authentication.
     * 
     * @return IP address that will be used for authentication.
     */
    public String getIP() {
        return ip;
    }
}
