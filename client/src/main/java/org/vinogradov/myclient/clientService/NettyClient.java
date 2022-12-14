package org.vinogradov.myclient.clientService;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import javafx.application.Platform;
import org.vinogradov.common.commonClasses.BasicQuery;
import org.vinogradov.common.commonClasses.Constants;
import org.vinogradov.myclient.GUI.AlertWindowsClass;

public class NettyClient {

    private Channel channel = null;

    ClientLogic clientLogic;

    public NettyClient() throws InterruptedException {

        new Thread(() -> {
            EventLoopGroup eventLoopGroup = null;
            try {
                eventLoopGroup = new NioEventLoopGroup();
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(eventLoopGroup);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.remoteAddress("localhost", 45001);
                bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast(
                                new ObjectDecoder(Constants.MB_10, ClassResolvers.cacheDisabled(null)),
                                new ObjectEncoder(),
                                new ClientHandler(clientLogic)
                        );
                    }
                });
                ChannelFuture channelFuture = bootstrap.connect().sync();
                channel = channelFuture.channel();
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                clientLogic.closeRegAuthGui();
                Platform.runLater(AlertWindowsClass::showFallsConnectAlert);
                e.printStackTrace();
            } finally {
                assert eventLoopGroup != null;
                eventLoopGroup.shutdownGracefully();
            }
        }).start();
    }

    public void exitClient() {
        channel.closeFuture();
        channel.close();
    }

    public void setClientLogic(ClientLogic clientLogic) {
        this.clientLogic = clientLogic;
    }
}

