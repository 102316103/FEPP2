package com.syscom.fep.frmcommon.delegate;

import java.util.concurrent.atomic.AtomicBoolean;

import com.syscom.fep.frmcommon.log.LogHelper;

public class MessageAsynchronousWaitReceiver<K, T> {
	private final LogHelper logger = new LogHelper();
	private final String CLASS_NAME = this.getClass().getSimpleName();

	private final K key;
	private T message;
	private final AtomicBoolean wait = new AtomicBoolean(true);

	public MessageAsynchronousWaitReceiver(K key) {
		this.key = key;
	}

	public K getKey() {
		return key;
	}

	public void messageArrived(Object caller, T message) {
		this.message = message;
		logger.info("[", CLASS_NAME, "]message arrived, caller = [", caller.getClass().getSimpleName(), "], key = [", this.key, "], message = [", message, "]");
		synchronized (this) {
			logger.info("[", CLASS_NAME, "]start to notify");
			this.notifyAll();
		}
		this.wait.set(false);
	}

	public T getMessage() {
		return this.message;
	}

	public boolean waitMessage(long timeout) {
		if (this.wait.get()) {
			if (timeout <= 0) {
				timeout = 1000L;
			}
			synchronized (this) {
				long currentTimeMillis = System.currentTimeMillis();
				logger.info("[", CLASS_NAME, "]wait(", timeout, ") at [", currentTimeMillis, "], key = [", this.key, "]...");
				try {
					this.wait(timeout);
				} catch (Exception e) {
					logger.warn(e, e.getMessage());
				}
				if (System.currentTimeMillis() - currentTimeMillis >= timeout) {
					logger.info("[", CLASS_NAME, "]wait timeout, key = [", this.key, "]");
					return false;
				} else {
					logger.info("[", CLASS_NAME, "]asynchronous wait succeed, key = [", this.key, "]");
					return true;
				}
			}
		} else {
			return true;
		}
	}
}