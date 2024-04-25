package com.syscom.fep.frmcommon.ref;

import java.io.Serializable;

public class RefBase<T> implements Serializable {
	private static final long serialVersionUID = 5640876583765580576L;
	
	protected T value;
	
	public RefBase(T value) {
		this.set(value);
	}
	
	public void set(T value) {
		this.value = value;
	}

	public T get() {
		return value;
	}

	@Override
	public String toString() {
		return this.value.toString();
	}
}
