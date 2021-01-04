package com.atguigu.bio;

import com.sun.org.apache.xpath.internal.operations.String;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *循环读取
 */
public class BioServer {
    public static void main(String[] args) throws Exception{
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        ServerSocket serverSocket = new ServerSocket(6666);

        while (true){
            //等待客户端监听
            final Socket accept = serverSocket.accept();

            if(accept !=null){
                executorService.execute(new Runnable() {
                    public void run() {
                        try {
                            handler(accept);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    public static void handler(Socket socket) throws IOException {
        try {
            byte[] bytes = new byte[1024];
            InputStream inputStream = socket.getInputStream();
            while (true){
                int read = inputStream.read(bytes);
                if(read!=-1){
//                    String string = new String(bytes);
//                    out.println(string);
                }else {
                    break;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            socket.close();
        }
    }
}
