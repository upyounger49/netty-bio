package com.atguigu.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import static java.lang.System.out;

public class SelectorServer {

    public static void main(String[] args) throws Exception{
        selctor();
    }


    /**
     * 服务端的逻辑
     * @throws Exception
     */
    public static void selctor() throws Exception{

        //建立一个服务端，等待客户端来连接
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);//非阻塞

        serverSocketChannel.socket().bind(new InetSocketAddress(6666));

        //建立一个selector
        Selector selector = Selector.open();
        //为服务端注册一个selector，监听的事件为接受
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


        while (true){//这个属于主线程

            if(selector.select(1000) == 0){//这里如果没有介入，1s后释放资源，不会将线程挂起
                out.println("服务器无连接，等待1秒后放弃！");
                continue;
            }

            //否则就获取通道集合遍历
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            //遍历key获取通道
            for (SelectionKey selectionKey : selectionKeys) {
                //看看key对应的通道发生了什么时间
                if(selectionKey.isAcceptable()){//是新客户端连接
                    out.println("客户端连接成功！"+selectionKey);
                    //分配一个客户端连接通道
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    //新分配的客户端通道也要注册到selector上
                    //这个缓存是服务器和通道之间的，不是客户端和通道之间的
                    socketChannel.register(selector,SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }

//                selectionKey.interestOps(SelectionKey.OP_CONNECT);//可以改变某个key的监听事件的类别
                if(selectionKey.isReadable()){//如果是可读事件
                    //通过key来获取通道
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    //获取通道关联的buffer
                    ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                    //将客户端socketChannel的数据读入到buffer里面去
                    socketChannel.read(byteBuffer);
                    out.println("客户端消息："+new String(byteBuffer.array()));
                }

                //做完之后将key删掉，避免多线程下重复操作
                selectionKeys.remove(selectionKey);

            }
        }

    }

}
