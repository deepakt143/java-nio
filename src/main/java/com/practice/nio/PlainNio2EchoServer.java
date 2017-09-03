package com.practice.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

public class PlainNio2EchoServer {
    public static void main(String[] args) throws IOException {
        serve(8080);
    }

    public static void serve(int port) throws IOException {
        final AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        serverChannel.bind(inetSocketAddress);
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

            public void completed(AsynchronousSocketChannel channel, Object attachment) {
                serverChannel.accept(null, this);
                ByteBuffer buffer = ByteBuffer.allocate(100);
                channel.read(buffer, buffer,
                    new EchoCompletionHandler(channel));

            }

            public void failed(Throwable exc, Object attachment) {
                try {
                    serverChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }

            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    private static class EchoCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
        AsynchronousSocketChannel channel;
        public EchoCompletionHandler(AsynchronousSocketChannel channel) {
            this.channel = channel;

        }


        public void completed(Integer result, ByteBuffer attachment) {
            attachment.flip();
            channel.write(attachment, attachment, new CompletionHandler<Integer, ByteBuffer>() {

                public void completed(Integer result, ByteBuffer attachment) {
                    if(attachment.hasRemaining()) {
                        channel.write(attachment, attachment, this);
                    } else {
                        attachment.compact();
                        channel.read(attachment, attachment, EchoCompletionHandler.this);
                    }
                }

                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public void failed(Throwable exc, ByteBuffer attachment) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
