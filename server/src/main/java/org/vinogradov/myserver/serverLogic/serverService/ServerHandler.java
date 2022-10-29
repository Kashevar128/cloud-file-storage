package org.vinogradov.myserver.serverLogic.serverService;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.vinogradov.mydto.commonClasses.BasicQuery;
import org.vinogradov.mydto.requests.*;
import org.vinogradov.myserver.serverLogic.ConnectionsService.ConnectionLimit;
import org.vinogradov.myserver.serverLogic.ConnectionsService.ConnectionLimitRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private ServerLogic serverLogic;

    private static final Map<Class<? extends BasicQuery>, BiConsumer<ServerHandlerLogic, BasicQuery>> REQUEST_HANDLERS = new HashMap<>();

    public ServerHandler(ServerLogic serverLogic) {
        this.serverLogic = serverLogic;
    }

    static {

        REQUEST_HANDLERS.put(RegClientRequest.class, ((serverHandlerLogic, basicQuery) -> {
            serverHandlerLogic.sendRegServerResponse((RegClientRequest) basicQuery);
        }));

        REQUEST_HANDLERS.put(AuthClientRequest.class, ((serverHandlerLogic, basicQuery) -> {
            serverHandlerLogic.sendAuthServerResponse((AuthClientRequest) basicQuery);
        }));

        REQUEST_HANDLERS.put(GetListRequest.class, ((serverHandlerLogic, basicQuery) -> {
            serverHandlerLogic.sendListResponse((GetListRequest) basicQuery);
        }));

        REQUEST_HANDLERS.put(SendFileRequest.class, ((serverHandlerLogic, basicQuery) -> {
            serverHandlerLogic.getHandingSendFileRequest((SendFileRequest) basicQuery);
        }));
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverLogic.getConnectionsController().newConnectionLimit(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        serverLogic.getConnectionsController().unConnectUser(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BasicQuery request = (BasicQuery) msg;

        serverLogic.getConnectionsController().putChannel(request, ctx);

        System.out.println(request.getType());
        BiConsumer<ServerHandlerLogic, BasicQuery> channelServerHandlerContextConsumer = REQUEST_HANDLERS.get(request.getClass());
        channelServerHandlerContextConsumer.accept(serverLogic, request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}