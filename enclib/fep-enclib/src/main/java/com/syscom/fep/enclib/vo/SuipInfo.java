package com.syscom.fep.enclib.vo;

import com.syscom.fep.enclib.SocketHelper;

public class SuipInfo {
	private String suipIP;
	private int suipPort;
	private SocketHelper suipSocket;
	private boolean suipAlive;

	public String getSuipIP() {
		return suipIP;
	}

	public void setSuipIP(String suipIP) {
		this.suipIP = suipIP;
	}

	public int getSuipPort() {
		return suipPort;
	}

	public void setSuipPort(int suipPort) {
		this.suipPort = suipPort;
	}

	public SocketHelper getSuipSocket() {
		return suipSocket;
	}

	public void setSuipSocket(SocketHelper suipSocket) {
		this.suipSocket = suipSocket;
	}

	public boolean isSuipAlive() {
		return suipAlive;
	}

	public void setSuipAlive(boolean suipAlive) {
		this.suipAlive = suipAlive;
	}
}
