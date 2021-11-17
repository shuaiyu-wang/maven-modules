package nio;

import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * Scattering 将数据写入buffer时， 可以采用buffer数组依次写入 【分散】
 * Gathering 从buffer中读取数据时，采用buffer数组依次读 【聚合】
 */
public class ScatteringAndGatheringTest {

    public static void main(String[] args) {
        // 使用 ServerSocketChannel 和 SocketChannel 网络

        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(7000);
            // 绑定端口到socket并启动
            serverSocketChannel.socket().bind(inetSocketAddress);
            // 创建buffer数组
            ByteBuffer[] byteBuffers = new ByteBuffer[2];
            byteBuffers[0] = ByteBuffer.allocate(5);
            byteBuffers[1] = ByteBuffer.allocate(3);

            // 等待客户端链接（telnet）
            SocketChannel socketChannel = serverSocketChannel.accept();
            // 假定从客户端接收8个字节
            int messageLength = 8;
            // 循环读取
            while (true) {
                long byteRead = 0;

                while (byteRead < messageLength) {
                    long read = socketChannel.read(byteBuffers);
                    // 累计读取的字节数
                    byteRead += read;
                    System.out.println("byteRead="+byteRead);
                    // 使用流打印，看看当前buffer的position和limit
                    Arrays.stream(byteBuffers)
                            .map(byteBuffer -> "position="+byteBuffer.position()+",limit="+byteBuffer.limit())
                            .forEach(System.out::println);
                    // 将所有的buffer反转
                    Arrays.stream(byteBuffers).forEach(Buffer::flip);
                    // 将数据读出显示到客户端
                    long byteWrite = 0;
                    while (byteWrite < messageLength) {
                        long write = socketChannel.write(byteBuffers);
                        byteWrite += write;
                    }
                    // 将所有的buffer clear
                    Arrays.stream(byteBuffers).forEach(Buffer::clear);
                    System.out.println("byteWrite="+byteWrite);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
