package com.syscom.fep.frmcommon.delegate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.syscom.fep.frmcommon.log.LogHelper;

public class MessageAsynchronousWaitReceiverManager {
	private static final LogHelper logger = new LogHelper();
	private static final String CLASS_NAME = MessageAsynchronousWaitReceiverManager.class.getSimpleName();
	@SuppressWarnings("rawtypes")
	private static final ConcurrentMap messageReceiverMap = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public static <K, T> void subscribe(Object caller, MessageAsynchronousWaitReceiver<K, T> callback) {
		if (!messageReceiverMap.containsKey(callback.getKey())) {
			messageReceiverMap.put(callback.getKey(), callback);
			logger.info(CLASS_NAME, " subscribe MessageReceiver, key = [", callback.getKey(), "], caller = [", caller.getClass().getSimpleName(), "]");
		}
	}

	@SuppressWarnings("unchecked")
	public static <K, T> void subscribeRepeatedly(Object caller, MessageAsynchronousWaitReceiver<K, T> callback) {
		messageReceiverMap.put(callback.getKey(), callback);
		logger.info(CLASS_NAME, " subscribe repeatedly MessageReceiver, key = [", callback.getKey(), "], caller = [", caller.getClass().getSimpleName(), "]");
	}

	@SuppressWarnings("unchecked")
	public static <K, T> MessageAsynchronousWaitReceiver<K, T> unsubscribe(Object caller, K key) {
		Object callback = messageReceiverMap.remove(key);
		if (callback != null) {
			logger.info(CLASS_NAME, " unsubscribe MessageReceiver, key = [", key, "], caller = [", caller.getClass().getSimpleName(), "]");
		}
		return (MessageAsynchronousWaitReceiver<K, T>) callback;
	}

	public static <K, T> void messageArrived(Object caller, K key, T message) {
		MessageAsynchronousWaitReceiver<K, T> callback = unsubscribe(caller, key);
		if (callback != null) {
			logger.info(CLASS_NAME, " find callback for key = [", key, "], and message arrived, caller = [", caller.getClass().getSimpleName(), "]!!");
			callback.messageArrived(caller, message);
		} else {
			logger.warn(CLASS_NAME, " cannot find callback for key = [", key, "], caller = [", caller.getClass().getSimpleName(), "]");
		}
	}
}
