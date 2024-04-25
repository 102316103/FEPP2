package com.syscom.fep.gateway;

import com.syscom.fep.frmcommon.util.IOUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.KeyManagerFactory;
import java.security.KeyStore;

public class DiscardSslServer {
    private int port;

    public static void main(String[] args) throws Exception {
        int port = 18090;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new DiscardSslServer(port).run();
    }

    public DiscardSslServer(int port) {
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
                            KeyStore ks = KeyStore.getInstance("PKCS12");
                            ks.load(IOUtil.openInputStream("fep-atmgwd.p12"), "syscom".toCharArray());
                            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                            kmf.init(ks, "syscom".toCharArray());
                            SslContext sslContext= SslContextBuilder.forServer(kmf).build();

                            KeyStore ks2 = KeyStore.getInstance("PKCS12");
                            ks2.load(IOUtil.openInputStream("atmgw-certificate_2.p12"), "syscom".toCharArray());
                            KeyManagerFactory kmf2 = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                            kmf2.init(ks2, "syscom".toCharArray());
                            SslContext sslContext2= SslContextBuilder.forServer(kmf2).build();

                            ch.pipeline().addLast("ssl2", sslContext2.newHandler(ch.alloc()));
                            ch.pipeline().addLast("ssl", sslContext.newHandler(ch.alloc()));

                            ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

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
