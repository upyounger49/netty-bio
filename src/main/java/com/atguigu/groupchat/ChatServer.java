package com.atguigu.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;

import static java.lang.System.out;

public class ChatServer {

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private final static int PORT = 6667;

    public ChatServer(){
        try {
            selector = Selector.open();

            serverSocketChannel = ServerSocketChannel.open();

            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));

            serverSocketChannel.configureBlocking(false);
            //注册到selector
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
        }
    }

    public void listen() throws IOException {

        while (true){
            //等待2s
            int select = selector.select(2000);
            if(select > 0){

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeys) {
                    if(selectionKey.isAcceptable()){//客户端是来连接；监听到请求连接服务端 的通道
                        SocketChannel accept = serverSocketChannel.accept();

                        accept.configureBlocking(false);
                        accept.register(selector,SelectionKey.OP_READ);

                        out.println(accept.getRemoteAddress()+"上线了");
                    }
                    //通道发生可读状态
                    if(selectionKey.isReadable()){
                        readData(selectionKey);
                    }

                    selectionKeys.remove(selectionKey);
                }
            }

        }
    }

    /**
     * 读取消息
     * 通过key来反向得到通道 ，并得到通道内的消息
     * @param selectionKey
     */
    private void readData(SelectionKey selectionKey) {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //从通道里读到buffer
        int read = 0;
        try {
            read = socketChannel.read(buffer);
        } catch (IOException e) {
            try {
                out.println(socketChannel.getRemoteAddress() + "已离线！");
                selectionKey.channel();
                socketChannel.close();
            } catch (IOException e1) {
                out.println("关闭异常@"+e1);
            }
        }
        //客户端消息已被服务端接收
        String msg = "";
        if(read > 0){
            msg = new String(buffer.array());
            out.println("消息："+msg);
        }

        //服务端向其他客户端转发消息
        transferMsg(msg,selectionKey);
    }

    /**
     * 转发消息给其他客户端
     * @param msg 消息
     * @param self 排除自己
     */
    private void transferMsg(String msg,SelectionKey self){

        out.println("服务器转发消息中.....");
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        selectionKeys.remove(self);//给别人发 排除自己
        for (SelectionKey key : selectionKeys) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
            try {
                //buffer消息写入到其他通道中
                socketChannel.write(buffer);
            } catch (IOException e) {
            }
        }


    }
    public static void main(String[] args) throws IOException {
        ChatServer chatServer = new ChatServer();
        chatServer.listen();
    }
}
