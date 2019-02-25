/*
 * Copyright (c) 2018, Open XAL Collaboration
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package xal.extension.fxapplication;

import java.util.Date;
import xal.extension.application.ApplicationStatus;

/**
 * FxApplicationStatusService handles FX application status queries on behalf of
 * the running application instance. Provides status information to clients on
 * the local network.
 *
 * @author tap, Juan F. Esteban Müller <JuanF.EstebanMuller@esss.se>
 */
public class FxApplicationStatusService implements ApplicationStatus {

    private FxApplication _application = null;

    public FxApplicationStatusService(FxApplication application) {
        _application = application;
    }

    /**
     * Get the free memory available to the application instance.
     *
     * @return The free memory available on this virtual machine in kB.
     */
    @Override
    public double getFreeMemory() {
        return ((double) Runtime.getRuntime().freeMemory()) / 1024;
    }

    /**
     * Get the total memory consumed by the application instance.
     *
     * @return The total memory consumed by the application instance in kB.
     */
    @Override
    public double getTotalMemory() {
        return ((double) Runtime.getRuntime().totalMemory()) / 1024;
    }

    /**
     * reveal the application by bringing all windows to the front
     */
    @Override
    public void showAllWindows() {
        _application.showAllWindows();
    }

    /**
     * Request that the virtual machine run the garbage collector.
     */
    @Override
    public void collectGarbage() {
        System.gc();
    }

    /**
     * Quit the application normally.
     *
     * @param code An unused status code.
     */
    @Override
    public void quit(final int code) {
        _application.quit();
    }

    /**
     * Force the application to quit immediately without running any finalizers.
     *
     * @param code The status code used for halting the virtual machine.
     */
    @Override
    public void forceQuit(int code) {
        Runtime.getRuntime().exit(code);
    }

    /**
     * Get the name of the host where the application is running.
     *
     * @return The name of the host where the application is running.
     */
    @Override
    public String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException exception) {
            return "";
        }
    }

    /**
     * Get the application name.
     *
     * @return The application name.
     */
    @Override
    public String getApplicationName() {
        return _application.getApplicationName();
    }

    /**
     * Get the launch time of the application in seconds since the epoch
     * (midnight GMT, January 1, 1970)
     *
     * @return the time at with the application was launched in seconds since
     * the epoch
     */
    @Override
    public Date getLaunchTime() {
        return _application.getLaunchTime();
    }

    /**
     * Get a heartbeat from the service.
     *
     * @return the time measured from the service at which the heartbeat was
     * sent
     */
    @Override
    public Date getHeartbeat() {
        return new Date();
    }
}
