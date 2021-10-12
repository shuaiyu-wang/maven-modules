package netty.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * 1. 我们自定义一个handler 需要继承netty 规定好的某个HandlerAdapter
 * 2. 这时我们自定义一个Handler，才能称之为Handler
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    // 读取数据实际（这里我们可以读取客户端发送的消息）
    /*
    1. ChannelHandlerContext ctx：上下文对象， 含有 管道pipeline， 通道channel， 地址
    2. Object msg：就是客户端发送的数据，默认Object
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("server ctx = " + ctx);
        // 将msg 转成一个ByteBuf
        // ByteBuf 是netty 提供的，不是NIO 的ByteBuffer
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("客户端发送的消息是：" + byteBuf.toString(CharsetUtil.UTF_8));
        System.out.println("客户端地址：" + ctx.channel().remoteAddress());
    }

    // 数据读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // writeAndFlush 是write + flush
        // 一般讲，我们对这个发送的数据进行编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端~", CharsetUtil.UTF_8));
    }

    // 处理异常，一般需要关闭连接
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
