package com.atguigu.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

public class ChatClient {

    private final static String host = "127.0.0.1";
    private final static int port = 6667;

    private static SocketChannel socketChannel;

    private static Selector selector;

    private static String userName;

    public ChatClient() throws  Exception{
        SocketChannel socketChannel = ChatClient.socketChannel = SocketChannel.open();
        selector = Selector.open();
        socketChannel.connect(new InetSocketAddress(host,port));
        socketChannel.configureBlocking(false);

        socketChannel.register(selector, SelectionKey.OP_READ);
        userName = socketChannel.getLocalAddress().toString();
        out.println(userName+"客户端已经就绪...");

    }

    private void sendMsg(String msg) throws IOException {
        msg = userName+":"+msg;
        socketChannel.write(ByteBuffer.wrap(msg.getBytes()));
    }

    /**
     * 从服务端读取消息
     * @throws IOException
     */
    private void readMsg() throws IOException {
        int select = selector.select();
        if(select>0){

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (SelectionKey key : selectionKeys) {
                if(key.isReadable()){
                    SocketChannel socketChannel1 = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    socketChannel1.read(byteBuffer);

                    out.println("服务端返回消息："+new String(byteBuffer.array()));
                }else{
                    out.println("没有可读的通道...");
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ChatClient chatClient = new ChatClient();

        //读数据
        new Thread(()->{
            while (true){
                try {
                    chatClient.readMsg();
                    TimeUnit.SECONDS.sleep(3);
                } catch (IOException e) {

                } catch (InterruptedException e) {

                }
            }
        }).start();

        //向服务端发数据
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()){
            String msg = scanner.nextLine();
            chatClient.sendMsg(msg);
        }
    }
}
