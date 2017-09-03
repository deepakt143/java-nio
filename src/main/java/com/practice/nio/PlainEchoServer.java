package com.practice.nio;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class PlainEchoServer {

    public static void main(String[] args) throws IOException {
        serve(8090);
    }
    public static void serve(int port) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            final Socket clientSocket = serverSocket.accept();
            new Thread(new Runnable() {

                public void run() {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                        while (true) {
                            printWriter.println(bufferedReader.readLine());
                            printWriter.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }
    }
}
