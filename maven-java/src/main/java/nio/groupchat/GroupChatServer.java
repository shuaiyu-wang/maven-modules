package nio.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class GroupChatServer {

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private static final int PORT = 6667;

    public GroupChatServer() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            // 绑定端口
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            // 设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            // 注册到selector
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        try {
            while (true) {
                int count = selector.select();
                if (count > 0) { // 有事件发生
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        // 监听 accept
                        if (selectionKey.isAcceptable()) {
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            System.out.println(socketChannel.getRemoteAddress()+" 上线");
                        }
                        // 如果是监听事件
                        if (selectionKey.isReadable()) {
                            // 处理读
                            read(selectionKey);
                        }
                        // 防止重复操作
                        iterator.remove();
                    }
                }

            }
        } catch (Exception e) {

        } finally {

        }
    }

    // 读取客户端消息
    public void read(SelectionKey selectionKey) {
        // 定义一个SocketChannel
        SocketChannel socketChannel = null;
        try {
            // 取到channel
            socketChannel = (SocketChannel) selectionKey.channel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int count = socketChannel.read(byteBuffer);
            if (count > 0) {
                String str = new String(byteBuffer.array());
                // 输出
                System.out.println("from client " + str);
                // 向其他客户端转发消息
                sendMsgToOtherClient(str, socketChannel);
            }

        } catch (IOException e) {
            try {
                System.out.println(socketChannel.getRemoteAddress() + " 离线了。。。");
                // 取消注册
                selectionKey.cancel();
                // 关闭通道
                socketChannel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    // 转发消息到其他客户端
    public void sendMsgToOtherClient(String msg, SocketChannel self) throws IOException{
        // 服务器转发消息
        // 遍历所有注册到selector上的socketChannel，并排除self
        for (SelectionKey selectionKey : selector.keys()) {
            Channel targetChannel = selectionKey.channel();
            if (targetChannel instanceof SocketChannel && targetChannel != self) {
                // 转型
                SocketChannel socketChannel = (SocketChannel) targetChannel;
                // 将msg存放到byteBuffer
                ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes());
                socketChannel.write(byteBuffer);
            }
        }
    }

    public static void main(String[] args) {
        new GroupChatServer().listen();
    }

}
