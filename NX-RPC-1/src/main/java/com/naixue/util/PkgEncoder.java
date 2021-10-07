/**
 * @ClassName:     ${PkgEncoder}
 * @Description:   ${tcp编码器}
 * @author         ${wangzongsheng}
 * @version        V1.0
 * @Date           ${2019-01-14}
 */

package com.naixue.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PkgEncoder extends MessageToByteEncoder<Object>
{
    public PkgEncoder()
    {
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception
    {
        try {
            //在这之前可以实现编码工作。
            out.writeBytes((byte[])msg);
        }finally {

        }
    }
}