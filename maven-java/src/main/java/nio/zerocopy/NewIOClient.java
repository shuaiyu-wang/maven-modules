package nio.zerocopy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class NewIOClient {

    public static void main(String[] args) {
        try {
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 7001));
            // 1.54G (1,659,292,307 byte)
            String file = "C:\\Users\\13689\\Desktop\\kafka-log-2.txt";
            FileInputStream fileInputStream = new FileInputStream(file);
            FileChannel fileChannel = fileInputStream.getChannel();
            long startTime = System.currentTimeMillis();
            // 在linux下一个transferTo方法可以完成传输
            // 在windows下一次调用transferTo方法只能发送8m，就需要分段传输文件
            // 主要是文件传输时的位置
            long transferCount = 0;
            if (isWindows()) {
                long count = 8 * 1024;
                for (int i = 0; i < fileChannel.size() / count + 1; i++) {
                    transferCount += fileChannel.transferTo(i * count, count, socketChannel);
                    if (i == fileChannel.size() / count) {
                        System.out.println("已拷贝完成，进度：100%");
                    } else {
                        System.out.println("已拷贝："+(i+1) * count + "，进度：" + (i+1) * count * 100 / fileChannel.size() + "%");
                    }
                }
                System.out.println("发送的总的字节数：" + transferCount + "，耗时：" + (System.currentTimeMillis() - startTime));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().startsWith("win");
    }
}
