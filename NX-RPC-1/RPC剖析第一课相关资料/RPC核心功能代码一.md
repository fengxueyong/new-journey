权重随机数组初始化

```java
//  num 表示生成的数组中1的个数 在数组中1表示抛弃请求 0表示接受请求
public static byte[] randomGenerator(int limit, int num) {

    byte[] tempArray = new byte[limit];

    if (num <= 0) {
        for (int i = 0; i < limit; i++) {
            tempArray[i] = 0;
        }
        return tempArray;
    }
    if (num >= limit) {
        for (int i = 0; i < limit; i++) {
            tempArray[i] = 1;
        }
        return tempArray;
    }

    //在数组中随机填充num个1
    Random random = new Random();
    for (int i = 0; i < num; i++) {
        int temp = Math.abs(random.nextInt()) % limit;
        while (tempArray[temp] == 1) {
            temp = Math.abs(random.nextInt()) % limit;
        }
        tempArray[temp] = 1;
    }
    return tempArray;
}
```





代理类请求发送和接收调用逻辑

1、注册WindowData

2、异步发送

3、等待数据到达

4、注销WindowData

```java

public Protocol request(Protocol requestProtocol) throws Exception {
        //Server状态判断
        if (ServerState.Reboot == state || ServerState.Dead == state) {
            throw new RebootException();
        }
        increaseCU();
        CSocket socket = null;
        try {
            try {
                socket = socketPool.getSocket();
                byte[] data = requestProtocol.toBytes(socket.isRights(), socket.getDESKey());
                //注册WindowData，放入SessionID-WindowData的Map
                socket.registerRec(requestProtocol.getSessionID());
                //异步发送，将数据放入发送队列
                socket.send(data);
            } catch (TimeoutException e) { 
                timeout();
                throw e;
            } catch (IOException e) {
                if (socket == null || !socket.connecting()) {
                    if (testServerState() != ServerState.Normal) {
                        this.asDeath();
                        logger.info("this server : {}  is dead , will choose another one !", address);
//                        logger.error(String.format("server %s is dead", new Object[]{this.address}), e);
                        throw new RebootException();
                    }
                }
                throw e;
            } catch (Exception e) {
                throw e;
            } finally {
                if (socket != null) {
                    socket.dispose();
                }
            }
            
            //接收数据（等待数据到达通知）
            byte[] buffer = socket.receive(requestProtocol.getSessionID(), currUserCount);
            Protocol receiveProtocol = Protocol.fromBytes(buffer, socket.isRights(), socket.getDESKey());
			
            return receiveProtocol;
        } finally {
            if (socket != null) {
                //注销WindowData
                socket.unregisterRec(requestProtocol.getSessionID());
            }
        }
```



数据发送实现逻辑：

1、注册发送事件SessionID-WindowData的Map

2、异步发送

注册发送事件

```java
public void registerRec(int sessionId) {
    AutoResetEvent event = new AutoResetEvent();
    WindowData wd = new WindowData(event);
    WaitWindows.put(sessionId, wd);
}
```



异步发送

```java
public void send(byte[] data) {
    try {
        if (null != transmitter) {
            TiresiasClientHelper.getInstance().setEndPoint(channel);
            TransmitterTask task = new TransmitterTask(this, data);
            transmitter.invoke(task);
        }
    } catch (NotYetConnectedException ex) {
        _connecting = false;
        throw ex;
    }
}


public void invoke(TransmitterTask task) {
        int size = wqueue.size();
        if (size > 1024 * 64) {
            logger.warn(Version.ID + " send queue is to max size is:" + size);
        }
    	//放入队列，异步发送
        wqueue.offer(task);
    }

```



发送线程处理逻辑

```java
class sendTask implements Runnable {
    @Override
    public void run() {
        int offset = 0;
        //缓存数据，用于聚合发送
        TransmitterTask[] elementData = new TransmitterTask[5];
        int waitTime = 0;
        for (; ; ) {
            try {
                TransmitterTask task = wqueue.poll(waitTime, TimeUnit.MILLISECONDS);
                if (null == task) {
                    if (elementData.length > 0 && offset > 0) {
                        send(elementData, offset);
                        offset = 0;
                        arrayClear(elementData);
                    }
                    waitTime = 10;
                    continue;
                }
                if (offset == 5) {
                    //发送
                    if (null != elementData) {
                        send(elementData, offset);
                    }
                    offset = 0;
                    arrayClear(elementData);
                }
                
                //不实时发送，暂时放入数组
                elementData[offset] = task;
                waitTime = 0;
                ++offset;
            } catch (Exception ex) {
                offset = 0;
                arrayClear(elementData);
                ex.printStackTrace();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
```



工作线程接收数据处理逻辑：

```java
public byte[] receive(int sessionId, int queueLen) {
    //获取WindowData
    WindowData wd = WaitWindows.get(sessionId);
    if (wd == null) {
        throw new RuntimeException("Need invoke 'registerRec' method before invoke 'receive' method!");
    }
    AutoResetEvent event = wd.getEvent();
    //等待数据到达事件
    if (!event.waitOne(socketConfig.getReceiveTimeout())) {
        throw new TimeoutException("ServiceIP:[" + this.getServiceIP() + "],Receive data timeout or error!timeout:" + socketConfig.getReceiveTimeout() + "ms,queue length:"
                + queueLen);
    }
    
    //从WindowData中获取Data数据
    byte[] data = wd.getData();
    int offset = SFPStruct.Version;
    int len = ByteConverter.bytesToIntLittleEndian(data, offset);
    if (len != data.length) {
        throw new ProtocolException("The data length inconsistent!datalen:" + data.length + ",check len:" + len);
    }
    return data;
}
```

AutoResetEvent实现

```java
public class AutoResetEvent {
    CountDownLatch cdl;

    public AutoResetEvent() {
        cdl = new CountDownLatch(1);
    }

    public AutoResetEvent(int waitCount) {
        cdl = new CountDownLatch(waitCount);
    }

    public void set() {
        cdl.countDown();
    }

    public boolean waitOne(long time) {
        try {
            return cdl.await(time, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

```



接收线程处理逻辑

```java
public void decode(ByteBuffer receiveBuffer, byte[] receiveArray) throws Exception {
    try {
        int limit = receiveBuffer.limit();
        int num = 0;
        for (; num < limit; num++) {
            byte b = receiveArray[num];
            receiveData.write(b);
            if (b == ProtocolConst.P_END_TAG[index]) {
                index++;
                if (index == ProtocolConst.P_END_TAG.length) {
                    byte[] pak = receiveData.toByteArray(ProtocolConst.P_START_TAG.length, receiveData.size() - ProtocolConst.P_END_TAG.length - ProtocolConst.P_START_TAG.length);
                    
                    //解析返回包中的SessionId
                    int pSessionId = ByteConverter.bytesToIntLittleEndian(pak, SFPStruct.Version + SFPStruct.TotalLen);
                    
                    //根据SessionId获取对应的已WindowData
                    WindowData wd = WaitWindows.get(pSessionId);
                    if (wd != null) {
                        if (wd.getFlag() == 0) {
                            //将返回数据放入WindowData
                            wd.setData(pak);
                            //调用CountDownLatch的countDown，结束工作线程的等待
                            wd.getEvent().set();
                        } else if (wd.getFlag() == 1) {
                            /** 异步 */
                            if (null != unregisterRec(pSessionId)) {
                                wd.getReceiveHandler().notify(pak, wd.getInvokeCnxn());
                            }
                        } else if (wd.getFlag() == 2) {
                            /** 异步 */
                            logger.info("un support request type !");
                        }
                    }
                    index = 0;
                    receiveData.reset();
                    continue;
                }
            } else if (index != 0) {
                if (b == ProtocolConst.P_END_TAG[0]) {
                    index = 1;
                } else {
                    index = 0;
                }
            }
        }
    } catch (Exception ex) {
        index = 0;
        ex.printStackTrace();
        receiveData.clear();
    }
}
```


节点信息（包含权重内容）存储示意图

![image](./doc/nodeinfo.png)