package com.proxy.api.rx.event;

import com.proxy.api.domain.model.Channel;

/**
 * Created by Evan on 5/5/15.
 */
public class ChannelAddedEvent {
    public final Channel channel;
    public ChannelAddedEvent(Channel channel) {
        this.channel = channel;
    }
}