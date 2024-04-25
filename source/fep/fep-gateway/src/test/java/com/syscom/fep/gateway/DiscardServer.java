package com.syscom.fep.gateway;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class DiscardServer {
    private int port;

    public static void main(String[] args) throws Exception {
        int port = 18090;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new DiscardServer(port).run();
    }

    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(512));
                            ch.pipeline().addLast(new DiscardServerHandler());
                            ch.pipeline().addLast(new DiscardServerHandler2());
                            ch.pipeline().addLast(new DiscardServerRuleBasedIpFilter());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // (6)
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(1024*1024))

//                    .option(ChannelOption.SO_SNDBUF, 512)
//                    .option(ChannelOption.SO_RCVBUF, 512)
            ;

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
