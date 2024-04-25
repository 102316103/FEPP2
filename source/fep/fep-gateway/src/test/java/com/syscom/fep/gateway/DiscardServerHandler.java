package com.syscom.fep.gateway;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class DiscardServerHandler  extends ChannelInboundHandlerAdapter { // (1)
    private final String PROGRAM_NAME = this.getClass().getSimpleName();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        System.out.println(PROGRAM_NAME + "=======channelRead=======");
        ByteBuf in = (ByteBuf) msg;
        try {
//            while (in.isReadable()) { // (1)
//                System.out.print((char) in.readByte());
//                System.out.flush();
//            }
//            System.out.println();
            byte[] bytes = new byte[in.readableBytes()];
            in.readBytes(bytes);
//            System.out.println(new String(bytes, StandardCharsets.UTF_8));
//            Thread.sleep(5000L);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        System.out.println(PROGRAM_NAME + "=======exceptionCaught=======");
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println(PROGRAM_NAME + "=======channelRegistered=======");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println(PROGRAM_NAME + "=======channelUnregistered=======");
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(PROGRAM_NAME + "=======channelActive=======");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(PROGRAM_NAME + "=======channelInactive=======");
        super.channelInactive(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println(PROGRAM_NAME + "=======channelReadComplete=======");
        super.channelReadComplete(ctx);
    }
}
