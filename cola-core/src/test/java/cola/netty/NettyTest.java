package cola.netty;

import cola.transport.netty.client.RpcChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author lcf
 */
public class NettyTest {
    public static void main(String[] args) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new MyChatClientInitializer());


            Channel channel = bootstrap.connect("localhost",18866).sync().channel();
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                channel.writeAndFlush(br.readLine()+"\r\n");
            }


        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }

    }

    public static class MyChatClientInitializer extends ChannelInitializer<SocketChannel> {
        protected void initChannel(SocketChannel ch) throws Exception {

            ChannelPipeline pipeline = ch.pipeline();

            //netty提供的处理器
            pipeline.addLast(new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()));//配置解码器
            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));


        }
    }

}
