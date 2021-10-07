
package com.naixue.server.connect;

import com.naixue.util.PkgDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.ChannelInitializer;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class TcpServer {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private int port;
    private final EventLoopGroup bossGroup;     //处理Accept连接事件的线程
    private final EventLoopGroup workerGroup;   //处理handler的工作线程

    public TcpServer(int port) {
        this.port = port;
        this.bossGroup = new NioEventLoopGroup(1);
        int cores = Runtime.getRuntime().availableProcessors();
        this.workerGroup = new NioEventLoopGroup(cores);
    }

    public void start() throws Exception {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024); //连接数
            serverBootstrap.localAddress(this.port);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new PkgDecoder());
                    pipeline.addLast(new ServerHandler());
                }
            });

            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            if (channelFuture.isSuccess()) {
                logger.info("rpc server start success!");
            } else {
                logger.info("rpc server start fail!");
            }
            channelFuture.channel().closeFuture().sync();
        } catch (Exception ex) {
            logger.error("exception occurred exception=" + ex.getMessage());
        } finally {
            bossGroup.shutdownGracefully().sync();          // 释放线程池资源
            workerGroup.shutdownGracefully().sync();
        }
    }
}