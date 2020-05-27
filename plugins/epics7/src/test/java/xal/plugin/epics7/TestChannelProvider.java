/*
 * Copyright (C) 2020 European Spallation Source ERIC
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
package xal.plugin.epics7;

import xal.plugin.epics7.TestChannel;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.client.ChannelFind;
import org.epics.pvaccess.client.ChannelFindRequester;
import org.epics.pvaccess.client.ChannelListRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvdata.factory.StatusFactory;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class TestChannelProvider implements ChannelProvider {

    @Override
    public void destroy() {
        //
    }

    @Override
    public String getProviderName() {
        return "Test";
    }

    @Override
    public ChannelFind channelFind(String channelName, ChannelFindRequester channelFindRequester) {
        return null;
    }

    @Override
    public ChannelFind channelList(ChannelListRequester channelListRequester) {
        return null;
    }

    @Override
    public Channel createChannel(String channelName, ChannelRequester channelRequester, short priority) {
        return createChannel(channelName, channelRequester, priority, "");
    }

    @Override
    public Channel createChannel(String channelName, ChannelRequester channelRequester, short priority, String address) {
        Channel channel = new TestChannel(channelName, this);

        // Wait a bit before sending connexion signals.
        Thread thread = new Thread() {
            public void run() {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    //
                }
                channelRequester.channelCreated(StatusFactory.getStatusCreate().getStatusOK(), channel);
                channelRequester.channelStateChange(channel, ConnectionState.CONNECTED);
            }
        };
        thread.start();

        return channel;
    }

}
