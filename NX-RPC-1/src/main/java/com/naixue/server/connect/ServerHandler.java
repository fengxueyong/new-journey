

package com.naixue.server.connect;

import java.net.InetSocketAddress;


import com.naixue.server.entity.User;
import com.naixue.server.protocol.RpcProtocol;
import com.naixue.server.server.UserService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServerHandler extends ChannelInboundHandlerAdapter {
    private Logger logger = LoggerFactory.getLogger(ServerHandler.class);


    private static int CMD_CREATE_USER = 1;
    private static int CMD_FIND_USER = 2;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel ch = ctx.channel();
        InetSocketAddress socketAddress = (InetSocketAddress) ch.remoteAddress();
        String clientIp = socketAddress.getAddress().getHostAddress();

        logger.info("client connect to rpc server, client's ip is: " + clientIp);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel ch = ctx.channel();
        InetSocketAddress socketAddress = (InetSocketAddress) ch.remoteAddress();
        String clientIp = socketAddress.getAddress().getHostAddress();

        logger.info("client close the connection to rpc server, client's ip is: " + clientIp);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] recvData = (byte[]) msg;
        if (recvData.length == 0) {
            logger.warn("receive request from client, but the data length is 0");
            return;
        }

        logger.info("receive request from client, the data length is: " + recvData.length);

        //反序列化请求数据
        RpcProtocol rpcReq = new RpcProtocol();
        rpcReq.byteArrayToRpcHeader(recvData);

        if(rpcReq.getMagicNum() != RpcProtocol.CONST_CMD_MAGIC){
            logger.warn("request msgic code error");
            return;
        }

        //解析请求，并调用处理方法
        int ret = -1;
        if(rpcReq.getCmd() == CMD_CREATE_USER){
            User user = rpcReq.byteArrayToUserInfo(rpcReq.getBody());
            UserService userService = new UserService();
            ret = userService.addUser(user);


            //构造返回数据
            RpcProtocol rpcResp = new RpcProtocol();
            rpcResp.setCmd(rpcReq.getCmd());
            rpcResp.setVersion(rpcReq.getVersion());
            rpcResp.setMagicNum(rpcReq.getMagicNum());
            rpcResp.setBodyLen(Integer.BYTES);
            byte[] body = rpcResp.createUserRespTobyteArray(ret);
            rpcResp.setBody(body);
            ByteBuf respData = Unpooled.copiedBuffer(rpcResp.generateByteArray());
            ctx.channel().writeAndFlush(respData);
        }
    }
}