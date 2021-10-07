package com.naixue.server.protocol;


import com.naixue.server.entity.User;
import com.naixue.util.ByteConverter;
import java.io.*;

public class RpcProtocol {
    static public int CONST_CMD_MAGIC = 0x20110711;
    private int version;
    private int cmd;
    public int magicNum;
    private int bodyLen;
    private byte[] body;
    final public static int HEAD_LEN = 16;

    public byte[] getBody() {
        return body;
    }

    public RpcProtocol setBody(byte[] body) {
        this.body = body;
        return this;
    }

    public int getVersion() {
        return version;
    }

    public RpcProtocol setVersion(int version) {
        this.version = version;
        return this;
    }

    public int getCmd() {
        return cmd;
    }

    public RpcProtocol setCmd(int cmd) {
        this.cmd = cmd;
        return this;
    }

    public int getMagicNum() {
        return magicNum;
    }

    public RpcProtocol setMagicNum(int magicNum) {
        this.magicNum = magicNum;
        return this;
    }

    public int getBodyLen() {
        return bodyLen;
    }

    public RpcProtocol setBodyLen(int bodyLen) {
        this.bodyLen = bodyLen;
        return this;
    }


    public byte[] generateByteArray()
    {
        byte[] data = new byte[HEAD_LEN + bodyLen];
        int index = 0;
        System.arraycopy(ByteConverter.intToBytes(version), 0, data, index, Integer.BYTES);
        index += Integer.BYTES;
        System.arraycopy(ByteConverter.intToBytes(cmd), 0, data, index, Integer.BYTES);
        index += Integer.BYTES;
        System.arraycopy(ByteConverter.intToBytes(magicNum), 0, data, index, Integer.BYTES);
        index += Integer.BYTES;
        System.arraycopy(ByteConverter.intToBytes(bodyLen), 0, data, index, Integer.BYTES);
        index += Integer.BYTES;
        System.arraycopy(body, 0, data, index, body.length);
        return data;
    }

    public RpcProtocol byteArrayToRpcHeader(byte[] data) throws IOException, ClassNotFoundException {
        if (data == null || data.length == 0) {
            return null;
        }

        int index = 0;

        this.setVersion(ByteConverter.bytesToInt(data, index));
        index += Integer.BYTES;

        this.setCmd(ByteConverter.bytesToInt(data, index));
        index += Integer.BYTES;

        this.setMagicNum(ByteConverter.bytesToInt(data, index));
        index += Integer.BYTES;

        this.setBodyLen(ByteConverter.bytesToInt(data, index));
        index += Integer.BYTES;

        this.body = new byte[this.bodyLen];
        System.arraycopy(data, index, this.body, 0, this.bodyLen);

        return this;
    }


    public User byteArrayToUserInfo(byte[] data)
    {
        User user = new User();
        int index = 0;

        user.setUid(ByteConverter.bytesToLong(data, index));
        index += Long.BYTES;

        user.setAge(ByteConverter.bytesToShort(data, index));
        index += Short.BYTES;

        user.setSex(ByteConverter.bytesToShort(data, index));
        index += Short.BYTES;
        return user;
    }

    public byte[] userInfoTobyteArray(User info)
    {
        byte[] data = new byte[Long.BYTES + Short.BYTES + Short.BYTES];
        int index = 0;
        System.arraycopy(ByteConverter.longToBytes(info.getUid()), 0, data, index, Long.BYTES);
        index += Long.BYTES;
        System.arraycopy(ByteConverter.shortToBytes(info.getAge()), 0, data, index, Short.BYTES);
        index += Short.BYTES;
        System.arraycopy(ByteConverter.shortToBytes(info.getSex()), 0, data, index, Short.BYTES);
        return data;
    }


    public static Object bytes2Object(byte[] objBytes) throws Exception {
        if (objBytes == null || objBytes.length == 0) {
            return null;
        }
        ByteArrayInputStream bi = new ByteArrayInputStream(objBytes);
        ObjectInputStream oi = new ObjectInputStream(bi);
        Object obj = oi.readObject();
        bi.close();
        oi.close();
        return obj;
    }

    public static byte[] object2Bytes(Serializable obj) throws Exception {
        if (obj == null) {
            return null;
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(obj);
        bo.close();
        oo.close();
        return bo.toByteArray();
    }


    public byte[] createUserRespTobyteArray(int result)
    {
        byte[] data = new byte[Integer.BYTES];
        int index = 0;
        System.arraycopy(ByteConverter.intToBytes(result), 0, data, index, Integer.BYTES);
        return data;
    }
}
