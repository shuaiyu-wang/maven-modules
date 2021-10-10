package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {

    public static void main(String[] args) {

        try {
            // 创建ServerSocketChannel
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            // 得到一个Selector对象
            Selector selector = Selector.open();
            // 绑定一个端口6666，在服务端监听
            serverSocketChannel.socket().bind(new InetSocketAddress(6666));
            // 设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            // 把serverSocketChannel注册到selector，关心事件为 OP_ACCEPT
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            // 循环等待客户端连接
            while (true) {
                // 等待一秒，如果没有连接事件，返回
                if (selector.select(1000) == 0) {
                    System.out.println("服务器等待了一秒，无连接。");
                    continue;
                }
                // 如果返回>0，就获取相关的selectionKey集合
                // 如果返回>0，表示已经获得到关心的事件
                // 通过selectionKeys反向获取通道
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey selectionKey = keyIterator.next();
                    // 根据通道发生的事件做相应处理
                    if (selectionKey.isAcceptable()) { // 如果是OP_ACCEPT，有新的客户端连接
                        // 该客户端生成一个SocketChannel
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        System.out.println("客户端连接成功 生成了一个socketChannel " + socketChannel.hashCode());
                        // 将socketChannel设置为非阻塞
                        socketChannel.configureBlocking(false);
                        // 将socketChannel注册到selector，关注事件为OP_READ
                        // 同时给SocketChannel关联一个Buffer
                        socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                    }
                    if (selectionKey.isReadable()) { // 发生OP_READ
                        // 通过selectionKey反向获取socketChannel
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        // 获取到该channel关联的buffer
                        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                        socketChannel.read(byteBuffer);
                        System.out.println("from client " + new String(byteBuffer.array()));
                    }
                    // 手动从集合中移除掉当前的selectionKey，防止重复操作
                    keyIterator.remove();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
