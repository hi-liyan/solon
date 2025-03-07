package org.noear.solon.cloud.extend.pulsar.impl;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageListener;
import org.noear.snack.ONode;
import org.noear.solon.Utils;
import org.noear.solon.cloud.CloudEventHandler;
import org.noear.solon.cloud.CloudProps;
import org.noear.solon.cloud.extend.pulsar.PulsarProps;
import org.noear.solon.cloud.model.Event;
import org.noear.solon.cloud.service.CloudEventObserverManger;
import org.noear.solon.cloud.utils.ExpirationUtils;
import org.noear.solon.core.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.5
 */
public class PulsarMessageListenerImpl implements MessageListener<byte[]> {
    static Logger log = LoggerFactory.getLogger(PulsarMessageListenerImpl.class);

    CloudEventObserverManger observerManger;
    String eventChannelName;

    public PulsarMessageListenerImpl(CloudProps cloudProps, CloudEventObserverManger observerManger) {
        this.observerManger = observerManger;
        this.eventChannelName = cloudProps.getEventChannel();
    }

    @Override
    public void received(Consumer<byte[]> consumer, Message<byte[]> msg) {
        try {
            String event_json = new String(msg.getValue());
            Event event = ONode.deserialize(event_json, Event.class);
            event.channel(eventChannelName);

            boolean isOk = onReceive(event);

            if (isOk == false) {
                event.times(event.times() + 1);

                consumer.reconsumeLater(msg, ExpirationUtils.getExpiration(event.times()), TimeUnit.SECONDS);
            } else {
                consumer.acknowledge(msg);
            }
        } catch (Throwable ex) {
            ex = Utils.throwableUnwrap(ex);

            EventBus.push(ex);

            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void reachedEndOfTopic(Consumer<byte[]> consumer) {
        MessageListener.super.reachedEndOfTopic(consumer);
    }

    /**
     * 处理接收事件
     */
    public boolean onReceive(Event event) throws Throwable {
        boolean isOk = true;
        CloudEventHandler handler = null;

        //new topic
        String topicNew;
        if (Utils.isEmpty(event.group())) {
            topicNew = event.topic();
        } else {
            topicNew = event.group() + PulsarProps.GROUP_SPLIT_MART + event.topic();
        }

        handler = observerManger.get(topicNew);
        if (handler != null) {
            isOk = handler.handle(event);
        } else {
            //只需要记录一下
            log.warn("There is no observer for this event topic[{}]", topicNew);
        }

        return isOk;
    }
}
