package netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.URI;

/*
说明
1. SimpleChannelInboundHandler 是ChannelInboundHandlerAdapter
2. HttpObject 客户端和服务端互相通讯的数据被封装成 HttpObject
 */
public class TestHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    // channelRead0 读取客户端数据
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

        // 判断 msg 是否是httpRequest 请求
        if (msg instanceof HttpRequest) {

            System.out.println("pipeline hashcode = " + ctx.pipeline().hashCode() + "，TestHttpServerHandler hashcode" + this.hashCode());

            // 获取httpRequest
            HttpRequest httpRequest = (HttpRequest) msg;

            System.out.println("msg 类型=" + msg.getClass());
            System.out.println("客户端地址：" + ctx.channel().remoteAddress());


            // 获取uri，过滤指定资源
            URI uri = new URI(httpRequest.uri());
            if ("/favicon.ico".equals(uri.getPath())) {
                System.out.println("请求了 favicon.ico， 不做响应");
                return;
            }

            // 回复消息给浏览器 [ http协议]
            ByteBuf content = Unpooled.copiedBuffer("hello client, im 服务器", CharsetUtil.UTF_8);

            //构造一个http的响应， 即httpResponse
            FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
            httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=utf-8");
            httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

            // 将构建好的 response 返回
            ctx.writeAndFlush(httpResponse);
        }

    }
}
