package com.syscom.fep.gateway;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public final class NettyTcpServer {
	private volatile EventLoopGroup bossGroup;
	private volatile EventLoopGroup workerGroup;
	private volatile ServerBootstrap bootstrap;
	private volatile boolean closed = false;
	private final int localPort;

	public NettyTcpServer(int localPort) {
		this.localPort = localPort;
	}

	public void close() {
		closed = true;
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
//		System.out.println("Stopped Tcp Server: " + localPort);
	}

	public void init() {
		closed = false;
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				// todo: add more handler
			}
		});
		doBind();
	}

	protected void doBind() {
		if (closed) {
			return;
		}
		bootstrap.bind(localPort).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture f) throws Exception {
				if (f.isSuccess()) {
//					System.out.println("Started Tcp Server: " + localPort);
				} else {
//					System.out.println("Started Tcp Server Failed: " + localPort);
					//f.channel().eventLoop().schedule(() -> doBind(), 1, TimeUnit.SECONDS);
				}
			}
		});
	}

//	public static void main(String[] args) {
//		NettyTcpServer server = new NettyTcpServer(18090);
//		server.init();
//		server.doBind();
//	}
}