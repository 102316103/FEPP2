package com.syscom.fep.invoker.netty;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import io.netty.channel.Channel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleNettyNotification {
    private static final LogHelper logger = LogHelperFactory.getTraceLogger();
    private final Map<String, List<SimpleNettyConnStateListener>> connStateListenerMap = new ConcurrentHashMap<>();

    public void addConnStateListener(String nettyTransmissionNotificationKey, SimpleNettyConnStateListener listener) {
        List<SimpleNettyConnStateListener> list = connStateListenerMap.get(nettyTransmissionNotificationKey);
        if (list == null) {
            list = new ArrayList<SimpleNettyConnStateListener>();
        }
        if (list.contains(listener)) return;
        list.add(listener);
        connStateListenerMap.put(nettyTransmissionNotificationKey, list);
        logger.info("[addConnStateListener]nettyTransmissionNotificationKey = [", nettyTransmissionNotificationKey, "], listener = [", listener, "], list.size = [", list.size(), "]");
    }

    public void addConnStateListener(int index, String nettyTransmissionNotificationKey, SimpleNettyConnStateListener listener) {
        List<SimpleNettyConnStateListener> list = connStateListenerMap.get(nettyTransmissionNotificationKey);
        if (list == null) {
            list = new ArrayList<SimpleNettyConnStateListener>();
        }
        if (list.contains(listener)) return;
        list.add(index, listener);
        connStateListenerMap.put(nettyTransmissionNotificationKey, list);
        logger.info("[addConnStateListener]nettyTransmissionNotificationKey = [", nettyTransmissionNotificationKey, "], listener = [", listener, "], list.size = [", list.size(), "]");
    }

    public void removeConnStateListener(String nettyTransmissionNotificationKey, SimpleNettyConnStateListener listener) {
        List<SimpleNettyConnStateListener> list = connStateListenerMap.get(nettyTransmissionNotificationKey);
        if (CollectionUtils.isEmpty(list)) return;
        if (!list.contains(listener)) return;
        list.remove(listener);
        logger.info("[removeConnStateListener]nettyTransmissionNotificationKey = [", nettyTransmissionNotificationKey, "], listener = [", listener, "], list.size = [", list.size(), "]");
    }

    public void notifyConnStateChanged(String nettyTransmissionNotificationKey, Channel channel, SimpleNettyConnState state, Throwable... t) {
        if (connStateListenerMap.isEmpty())
            return;
        List<SimpleNettyConnStateListener> list = connStateListenerMap.get(nettyTransmissionNotificationKey);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        SimpleNettyConnStateListener[] listeners = new SimpleNettyConnStateListener[list.size()];
        list.toArray(listeners);
        if (ArrayUtils.isNotEmpty(listeners)) {
            logger.info("[notifyConnStateChanged]nettyTransmissionNotificationKey = [", nettyTransmissionNotificationKey, "], state = [", state, "], listeners.size = [", listeners.length, "]");
            if (ArrayUtils.isEmpty(t)) {
                for (SimpleNettyConnStateListener SimpleNettyConnStateListener : listeners) {
                    SimpleNettyConnStateListener.connStateChanged(channel, state);
                }
            } else {
                for (SimpleNettyConnStateListener SimpleNettyConnStateListener : listeners) {
                    SimpleNettyConnStateListener.connStateChanged(channel, state, t[0]);
                }
            }
        }
    }
}
