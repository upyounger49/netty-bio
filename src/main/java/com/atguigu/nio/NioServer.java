package com.atguigu.nio;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import static java.lang.System.out;

public class NioServer {

    public static void main(String[] args) throws Exception {
        manyBufferOp();
    }

    public void base(){
        /**
         * 创建buffer
         */
        IntBuffer buffer = IntBuffer.allocate(5);
        buffer.put(10);
        buffer.put(11);
        buffer.put(12);
        buffer.put(13);
        buffer.put(14);

        //读写切换
        buffer.flip();
        //从这开始读
        buffer.position(1);
        //从这截止
        buffer.limit(4);

        while (buffer.hasRemaining()){
            out.println(buffer.get());
        }
    }

    /**
     * 输出到本地文件
     * @throws Exception
     */
    public static void FileChannelM() throws Exception{
        /**
         * 输入：文件输入到内存
         * 输出：内存输出到文件
         */
        String string = "today is nice day!";
        /**
         * 通道的建立也是基于流的基础上的
         */
        FileOutputStream fileOutputStream = new FileOutputStream("d://a.txt");
        FileChannel fileChannel = (FileChannel) Channels.newChannel(fileOutputStream);
        //缓存区创建
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //数据方法缓存区
        byteBuffer.put(string.getBytes());

        //数据放到缓存区之后需要切换
        byteBuffer.flip();

        //byteBuffer 写入到 channel
        fileChannel.write(byteBuffer);
        fileOutputStream.close();

    }

    /**
     * 本地文件独到内存
     * @throws Exception
     */
    public static void FileChannelM1() throws Exception{
        /**
         * 输入：文件输入到内存
         * 输出：内存输出到文件
         * 内存输出到文件
         * 文件写入到内存
         */
        /**
         * 通道的建立也是基于流的基础上的
         */
        FileInputStream fileInputStream = new FileInputStream("d://a.txt");
        FileChannel fileChannel = (FileChannel) Channels.newChannel(fileInputStream);
        //缓存区创建
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        //byteBuffer 写入到 channel
        fileChannel.read(byteBuffer);

        //数据放到缓存区之后需要切换
        byteBuffer.flip();
        out.println(new String(byteBuffer.array()));
        fileInputStream.close();

    }

    /**
     * 文件拷贝
     * 此处 拷贝文件的原理 通过copy通道来完成
     * 通道从缓存读 通道写到缓存
     */
    public static void fileChannelCopy() throws Exception {
        FileInputStream fileInputStream = new FileInputStream("d://a.txt");
        FileChannel fileChannel = (FileChannel) Channels.newChannel(fileInputStream);

        /*ByteBuffer allocate = ByteBuffer.allocate(1024);
        fileChannel.read(allocate);*/

//        allocate.flip();
        FileOutputStream fileOutputStream = new FileOutputStream("d://a1.txt");
//        WritableByteChannel readableByteChannel = (WritableByteChannel) Channels.newChannel(fileOutputStream);
        FileChannel fileChannel1 = (FileChannel) Channels.newChannel(fileOutputStream);

//        fileChannel.transferTo(0,allocate.limit(),readableByteChannel);
        fileChannel1.transferFrom(fileChannel,0,fileChannel.size());

        fileOutputStream.close();
        fileInputStream.close();

    }

    /**
     * MappedByteBuffer 可以将文件直接放在堆外内存修改 os不需要拷贝一次
     * @throws Exception
     */
    public static void mappedByteBuffer() throws Exception{

        FileInputStream fileInputStream = new FileInputStream("d://a.txt");
        FileChannel fileChannel = (FileChannel) Channels.newChannel(fileInputStream);

        /**
         * Buffer的mode
         * 0.可以直接修改的起始位置
         * 5.内存中 截止修改到第五个 ，不是索引的下标，就是单纯第5个,最多修改到索引下标4
         **/
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 5);

        mappedByteBuffer.put(0,(byte)'H');
        mappedByteBuffer.put(4,(byte)'9');

        fileInputStream.close();
    }

    /**
     * 多个buffer进行读写数据！多管齐下
     * scattering：将数据一次写入buffer数组中，一个满了写下一个 （分散）
     * gathering：把数据从buffer读出时候，依次读取 （聚合）
     */
    public static void manyBufferOp() throws IOException {

        //服务端
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //端口号
        InetSocketAddress inetSocketAddress = new InetSocketAddress(4936);

        serverSocketChannel.bind(inetSocketAddress);

        //开始监听,返回客户端
        SocketChannel socketChannel = serverSocketChannel.accept();

        ByteBuffer[] byteBuffers = new ByteBuffer[2];
        byteBuffers[0] = ByteBuffer.allocate(5);
        byteBuffers[1] = ByteBuffer.allocate(5);

        int sizeb = 10;
        while (true){

            long inty =0;

            while(inty < sizeb){
                long read = socketChannel.read(byteBuffers);
                inty+=read;
                Arrays.asList(byteBuffers).stream().map(b->"postion:"+b.position()+",limit:"+b.limit()).forEach(out::println);
                out.println(read);
            }

            Arrays.asList(byteBuffers).forEach(buffer->buffer.flip());

            while(inty < sizeb){
                long write = socketChannel.write(byteBuffers);
                inty+=write;
                Arrays.asList(byteBuffers).stream().map(b->"postion:"+b.position()+",limit:"+b.limit()).forEach(out::println);
                out.print(inty);
            }
        }

    }
}
