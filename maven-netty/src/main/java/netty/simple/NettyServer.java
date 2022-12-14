package netty.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.CharsetUtil;

public class NettyServer {

    public static void main(String[] args) {
        // 说明
        // 1、创建BossGroup 和 WorkerGroup
        // 2、bossGroup只是处理连接请求，真正的和客户端业务处理，会交给workerGroup完成
        // 3、两个都是无线循环
        // 4、bossGroup 和 workerGroup 含有子线程（NioEventLoop）的个数 默认实际cpu核数 * 2
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(8);
        try {
            // 创建服务端启动对象，配置参数
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            // 使用链式编程来进行设置
            serverBootstrap.group(bossGroup, workerGroup) // 设置两个线程组
                    .channel(NioServerSocketChannel.class) // 使用NioServerSocketChannel 作为服务端的通道实现
                    .option(ChannelOption.SO_BACKLOG, 128) // 设置线程队列等待的连接数
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 设置保持活动连接状态
//                    .handler(null) // 该handler对应bossGroup childHandler对应workerGroup
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 创建一个通道测试对象（匿名对象）
                        // 给pipeline 设置处理器
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 可以使用一个集合管理socketChannel，
                            // 在推送消息时，可以将业务加入到各个socketChannel 对应的NioEvenLoop 或者 scheduleTaskQueue
                            System.out.println("客户 socketChannel hashcode=" + ch.hashCode());
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new DelimiterBasedFrameDecoder(10 * 1024 * 1024, false, Unpooled.copiedBuffer("喵喵喵", CharsetUtil.UTF_8)));
                            pipeline.addLast(new NettyServerHandler());
                        }
                    }); // 给我们的workerGroup 的 EventLoop 对应的管道设置处理器

            System.out.println("server is ready...");

            // 绑定一个端口并且同步，生成一个ChannelFuture 对象
            ChannelFuture channelFuture = serverBootstrap.bind(6668).sync();
            // 给channelFuture 注册监听器，监控我们关心的事件
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        System.out.println("监听端口 6668 成功");
                    } else {
                        System.out.println("监听端口 6668 失败");
                    }
                }
            });
            // 对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }


    }
}
