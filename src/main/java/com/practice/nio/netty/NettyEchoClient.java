package com.practice.nio.netty;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class NettyEchoClient {

    public static void main(String[] args) throws InterruptedException {
        NettyEchoClient nettyEchoClient = new NettyEchoClient(8081);
        nettyEchoClient.start();
    }
    int port;
    String host;
    public NettyEchoClient(int port) {
        this.port = port;
        this.host = "localhost";
    }
    public void start() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(host, port)).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new EchoClientHandler());
                }
            });

            ChannelFuture f = bootstrap.connect().sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    @ChannelHandler.Sharable
    public static class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ctx.write(Unpooled.copiedBuffer("Netty rocks!" ,CharsetUtil.UTF_8));
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            System.out.println("Client received: " + ByteBufUtil
                .hexDump(in.readBytes(in.readableBytes())));

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx,
                                    Throwable cause) {
            System.out.println(cause);
            ctx.close();
        }
    }
}
