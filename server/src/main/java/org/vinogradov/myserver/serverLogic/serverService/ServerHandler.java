package org.vinogradov.myserver.serverLogic.serverService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.vinogradov.common.commonClasses.BasicQuery;
import org.vinogradov.common.requests.*;
import org.vinogradov.myserver.serverLogic.dataBaseService.DataBase;
import org.vinogradov.myserver.serverLogic.storageService.Storage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private final ServerLogicImpl serverLogicImpl;

    private final DataBase dataBase;
    private final Storage storage;
    private final NettyServer nettyServer;

    private static final Map<Class<? extends BasicQuery>, BiConsumer<ServerLogic, BasicQuery>> REQUEST_HANDLERS = new HashMap<>();

    public ServerHandler(DataBase dataBase, Storage storage, NettyServer nettyServer) {
        this.dataBase = dataBase;
        this.storage = storage;
        this.nettyServer = nettyServer;
        this.serverLogicImpl = new ServerLogicImpl(dataBase, storage, nettyServer);
    }

    //Реализация паттерна "Медиатор" в обработчики сообщений -
    //имеется мапа в которой хранится ключ(класс пришедшего сообщения) и
    //значение(логика обработчика - реакция на пришедшее сообщение)

    static {

        REQUEST_HANDLERS.put(RegOrAuthClientRequest.class, ((serverLogic, basicQuery) -> {
            serverLogic.getHandingRegOrAuthClientRequest((RegOrAuthClientRequest) basicQuery);
        }));

        REQUEST_HANDLERS.put(GetListRequest.class, ((serverLogic, basicQuery) -> {
            serverLogic.getHandingGetListRequest((GetListRequest) basicQuery);
        }));

        REQUEST_HANDLERS.put(DelFileRequest.class, ((serverLogic, basicQuery) -> {
            serverLogic.getHandingDelFileRequest((DelFileRequest) basicQuery);
        }));

        REQUEST_HANDLERS.put(CreateNewFolderRequest.class, ((serverLogic, basicQuery) -> {
            serverLogic.getHandingCreateNewFolderRequest((CreateNewFolderRequest) basicQuery);
        }));

        REQUEST_HANDLERS.put(MetaDataFileRequest.class, ((serverLogic, basicQuery) -> {
            serverLogic.getHandingMetaDataFileRequest((MetaDataFileRequest) basicQuery);
        }));

        REQUEST_HANDLERS.put(SendPartFileRequest.class, ((serverLogic, basicQuery) -> {
            serverLogic.getHandingSendPartFileRequest((SendPartFileRequest) basicQuery);
        }));

        REQUEST_HANDLERS.put(ClearFileOutputStreamsRequest.class, ((serverLogic, basicQuery) -> {
            serverLogic.getHandingClearFileOutputStreamsRequest((ClearFileOutputStreamsRequest) basicQuery);
        }));

        REQUEST_HANDLERS.put(GetFileRequest.class, ((serverLogic, basicQuery) -> {
            serverLogic.getHandingGetFileRequest((GetFileRequest) basicQuery);
        }));

        REQUEST_HANDLERS.put(PermissionToTransferRequest.class, ((serverLogic, basicQuery) -> {
            serverLogic.getHandingPermissionToTransferRequest((PermissionToTransferRequest) basicQuery);
        }));

        REQUEST_HANDLERS.put(PatternMatchingRequest.class, ((serverLogic, basicQuery) -> {
            serverLogic.getHandingPatternMatchingRequest((PatternMatchingRequest) basicQuery);
        }));

        REQUEST_HANDLERS.put(OverwriteFileRequest.class, ((serverLogic, basicQuery) -> {
            serverLogic.getHandingOverwriteFileRequest((OverwriteFileRequest) basicQuery);
        }));
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverLogicImpl.setContext(ctx);
        serverLogicImpl.addConnectionLimit(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        nettyServer.getUserContextRepository().deleteUserContext(ctx);
        serverLogicImpl.getReceivingFileServerController().closeAllFileOutputStreams();
        serverLogicImpl.getSendFileServerController().clearSrcPathsMap();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {;
        BasicQuery request = (BasicQuery) msg;

        if (!serverLogicImpl.filterSecurity(request)) return;

        System.out.println(request.getClassName());
        BiConsumer<ServerLogic, BasicQuery> channelServerHandlerContextConsumer = REQUEST_HANDLERS.get(request.getClass());
        channelServerHandlerContextConsumer.accept(serverLogicImpl, request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
