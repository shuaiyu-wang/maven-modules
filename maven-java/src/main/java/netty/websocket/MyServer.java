package netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class MyServer {

    private final int port;

    public MyServer(int port) {
        this.port = port;
    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(8);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 因为是基于http协议，使用http编解码器
                            pipeline.addLast(new HttpServerCodec());
                            // 以块方式写，添加chunkedWrite处理器
                            pipeline.addLast(new ChunkedWriteHandler());
                            /*
                            说明
                            1.http在传输过程中是分段的，HttpObjectAggregator可以将多个段聚合起来
                            2.这就是为什么当浏览器发送大量数据时。就会发出多个http请求
                             */
                            pipeline.addLast(new HttpObjectAggregator(8192));
                            /*
                            说明
                            1.对于websocket，他的数据是以帧（frame）的形式传递
                            2.可以看到WebsocketFrame下面有6个子类
                            3.浏览器发送请求时 ws://localhost:7000/hello 表示请求的uri
                            4.WebSocketServerProtocolHandler核心功能：将http协议升级为ws协议，保持长连接
                            5.是通过状态码 101
                             */
                            pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));
                            // 自定义handler 处理业务逻辑
                            pipeline.addLast(new MyTextWebSocketFrameHandler());
                        }
                    });
            System.out.println("netty 服务器启动");
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            // 监听关闭
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }



    }

    public static void main(String[] args) {
        new MyServer(7000).run();
    }
}
