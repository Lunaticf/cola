package cola.transport.netty.client;

import cola.serialization.Serializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author lcf
 * 连接管理器 维护连接 复用连接
 */
@Slf4j
public class ConnectManager {
    private Serializer serializer;

    public ConnectManager(Serializer serializer) {
        this.serializer = serializer;
    }

    private Map<InetSocketAddress, Channel> address2Channel = new ConcurrentHashMap<>();

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        if (address2Channel.containsKey(inetSocketAddress)) {
            return address2Channel.get(inetSocketAddress);
        }

        Channel channel = null;
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcChannelInitializer(serializer))
                    .option(ChannelOption.SO_KEEPALIVE, true);

            channel = bootstrap.connect(inetSocketAddress.getHostName(), inetSocketAddress.getPort()).sync()
                    .channel();

            System.out.println(channel);
            log.info("连接到 {}", inetSocketAddress.toString());

            // 存放连接
            addChannel(inetSocketAddress, channel);

            // 如果Channel关闭 移除该channel
            channel.closeFuture().addListener((ChannelFutureListener) future -> removeChannel(inetSocketAddress));

        } catch (Exception e) {
            log.warn("Fail to get channel for address: {}", inetSocketAddress);
        }

        return channel;
    }

    private void addChannel(InetSocketAddress inetSocketAddress, Channel channel) {
        address2Channel.put(inetSocketAddress, channel);
    }

    private void removeChannel(InetSocketAddress inetSocketAddress) {
        address2Channel.remove(inetSocketAddress);
    }
}
