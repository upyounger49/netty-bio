package com.atguigu.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static java.lang.System.out;

public class SelectorClient {

    public static void main(String[] args) throws Exception {
        client();
    }

    /**
     * 客户端代码
     */
    public static void client() throws Exception{
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 6666);

        if(!socketChannel.connect(inetSocketAddress)){
            while (!socketChannel.finishConnect()){
                out.println("连接时间较长，客户端不会阻塞，可做其他事情....");
            }
        }

        String string = "我要发送的消息是一个模板";
        //灵活将消息包裹成一个适合大小的buffer
        ByteBuffer byteBuffer = ByteBuffer.wrap(string.getBytes());
        socketChannel.write(byteBuffer);

        //等我写入
        System.in.read();
    }
}
