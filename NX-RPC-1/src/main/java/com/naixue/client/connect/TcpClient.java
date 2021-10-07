package com.naixue.client.connect;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class TcpClient {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static int MAX_PACKAGE_SIZE = 1024 * 4;
    private static String SERVER_IP = "127.0.0.1";
    private static int SERVER_PORT = 58885;
    private static TcpClient instance = null;


    private boolean isInit = false;
    //private ChannelFuture channelFuture = null;
    SocketChannel client = null;

    private final static int CONNECT_TIMEOUT_MILLIS = 2000;

    //private Bootstrap bootstrap = new Bootstrap();
    public TcpClient() {}

    public static TcpClient GetInstance() {
        if (instance == null) {
                instance = new TcpClient();
        }
        return instance;
    }


    public void init() throws Exception{
        if(!isInit) {
            client = SocketChannel.open(new InetSocketAddress(SERVER_IP, SERVER_PORT));
            client.configureBlocking(true);
        }
        isInit = true;
    }



    public boolean sendData(byte[] data){
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.put(data);
        byteBuffer.flip();
        int ret = 0;
        try {
            ret = client.write(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public byte[] recvData()  {
        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_PACKAGE_SIZE);
        try {
            int rs = client.read(byteBuffer);
            byte[] bytes = new byte[rs];
            byteBuffer.flip();
            byteBuffer.get(bytes);
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}