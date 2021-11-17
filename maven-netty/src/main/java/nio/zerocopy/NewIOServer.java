package nio.zerocopy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NewIOServer {

    public static void main(String[] args) {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(7001));
            // 创建buffer
            ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                int readCount = 0;
                while (-1 != readCount) {
                    readCount = socketChannel.read(byteBuffer);
                    byteBuffer.rewind(); // 倒带 position = 0 mark 作废
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
