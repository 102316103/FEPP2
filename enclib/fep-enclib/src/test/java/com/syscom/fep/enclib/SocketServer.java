package com.syscom.fep.enclib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketServer {
	private Integer port;
	private boolean started;
	private ServerSocket serverSocket;

	public static void main(String[] args) {
		new SocketServer().start(8002);
	}

	public void start() {
		start(null);
	}

	public void start(Integer port) {
		System.out.println("port: " + this.port + " " + port);
		try {
			serverSocket = new ServerSocket(port == null ? this.port : port);
			started = true;
			System.out.println("Socket服務已啟動，占用端口： " + serverSocket.getLocalPort());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		while (started) {
			try {
				Socket socket = serverSocket.accept();
				socket.setKeepAlive(true);
				InputStream in = socket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
				String readLine = null;
				while ((readLine = reader.readLine()) != null) {
					System.out.println("recived message = [" + readLine + "]");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
