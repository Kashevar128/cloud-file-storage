package org.vinogradov.myclient.clientService;

import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import org.vinogradov.common.commonClasses.*;
import org.vinogradov.myclient.GUI.AlertWindowsClass;
import org.vinogradov.myclient.GUI.ClientGUI;
import org.vinogradov.myclient.GUI.ProgressBarSendFile;
import org.vinogradov.myclient.GUI.RegAuthGui;
import org.vinogradov.myclient.controllers.ClientController;
import org.vinogradov.common.requests.*;
import org.vinogradov.common.responses.*;
import org.vinogradov.myclient.receivingFileClientService.ReceivingFileClientController;
import org.vinogradov.myclient.sendFileClientService.SendFileClientController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class ClientLogic implements ClientHandlerLogic {

    private final Runnable runnableRegComplete = AlertWindowsClass::showRegComplete,
            runnableAuthComplete = AlertWindowsClass::showAuthComplete,
            runnableRegFalse = AlertWindowsClass::showRegFalse,
            runnableAuthFalse = AlertWindowsClass::showAuthFalse;

    private ChannelHandlerContext context;

    private RegAuthGui regAuthGui;

    private NettyClient nettyClient;

    private ClientGUI clientGUI;

    private ClientController clientController;

    private ClientLogic clientLogic;

    private User user;

    private ProgressBarSendFile progressBarSendFile;

    private final SendFileClientController sendFileClientController;

    private final ReceivingFileClientController receivingFileClientController;

    public ClientLogic() {
        this.receivingFileClientController = new ReceivingFileClientController();
        this.sendFileClientController = new SendFileClientController();
    }

    @Override
    public void getHandingRegOrAuthResponse(RegOrAuthServerResponse regOrAuthServerResponse) {
        boolean regOrAuthComplete = regOrAuthServerResponse.isRegOrAuthComplete();
        this.user = regOrAuthServerResponse.getUser();
        UpdatePanel updatePanel = regOrAuthServerResponse.getUpdatePanel();
        StatusUser statusUser = regOrAuthServerResponse.getStatusUser();
        Runnable runnableComplete = null;
        Runnable runnableFalse = null;
        switch (statusUser) {
            case AUTH -> {
                runnableComplete = runnableAuthComplete;
                runnableFalse = runnableAuthFalse;
            }
            case REG -> {
                runnableComplete = runnableRegComplete;
                runnableFalse = runnableRegFalse;
            }
        }
        if (regOrAuthComplete) {
            startClientGUI(updatePanel, runnableComplete);
            return;
        }
        startFalseClientGUI(runnableFalse);
    }

    @Override
    public void getHandingGetListResponse(GetListResponse responseList) {
        UpdatePanel updatePanel = responseList.getUpdatePanel();
        clientController.serverPC.updateList(updatePanel);
    }

    @Override
    public void getHandingConnectionLimit(ConnectionLimitResponse connectionLimitResponse) {
        Platform.runLater(() -> {
            AlertWindowsClass.showConnectionLimit();
            regAuthGui.getStage().close();
        });
    }

    @Override
    public void getHandingPermissionToTransferResponse(PermissionToTransferResponse permissionToTransferResponse) {
        boolean allowTransmission = permissionToTransferResponse.isAllowTransmission();
        if (allowTransmission) {
            String nameFileOrDirectorySend = sendFileClientController.getNameFileOrDirectorySend();
            progressBarSendFile.updateFileNameBar(nameFileOrDirectorySend, Constants.SEND);
            progressBarSendFile.setCounterFileSize(sendFileClientController.getCounterFileSize());
            progressBarSendFile.showProgressBar();
            MyFunction<Long, byte[], Boolean> myFunctionSendPartFile = (id, bytes) -> {
                sendFileClientController.addSizePartInCounter(bytes.length);
                sendMessage(new SendPartFileRequest(user, id, bytes));
                progressBarSendFile.updateProgressBar(sendFileClientController.getRatioCounter());
                if (progressBarSendFile.isEnd()) {
                    progressBarSendFile.setEnd(false);
                    sendMessage(new DelFileRequest(user, sendFileClientController.getSelectedDstPath()));
                    sendMessage(new ClearFileOutputStreamsRequest(user));
                    return true;
                }
                return false;
            };
            Map<Long, String> pathsMapFile = sendFileClientController.getSrcPathsMap();
            for (Map.Entry<Long, String> entry : pathsMapFile.entrySet()) {
                if (HelperMethods.splitFile(entry.getKey(), entry.getValue(), myFunctionSendPartFile)) break;
            }
            sendFileClientController.clearSrcPathsMap();
        } else {
            Platform.runLater(AlertWindowsClass::showSizeCloudAlert);
        }
    }

    @Override
    public void getHandingMetaDataResponse(MetaDataResponse metaDataResponse) {
        long sizeFile = metaDataResponse.getSizeFile();
        Map<Long, String> dstPaths = metaDataResponse.getDstPaths();
        receivingFileClientController.addFileOutputStreamMap(dstPaths);
        receivingFileClientController.createCounterFileSize(sizeFile);
        progressBarSendFile.setCounterFileSize(receivingFileClientController.getCounterFileSize());
        progressBarSendFile.showProgressBar();
        sendMessage(new PermissionToTransferRequest(user, true));
    }

    @Override
    public void getHandingSendPartFileResponse(SendPartFileResponse sendPartFileResponse) {
        long sizePart = sendPartFileResponse.getSizePart();
        Long id = sendPartFileResponse.getId();
        byte[] bytes = sendPartFileResponse.getBytes();
        receivingFileClientController.addSizePartInCounter(sizePart);
        double ratioCounter = receivingFileClientController.getRatioCounter();
        progressBarSendFile.updateFileNameBar(receivingFileClientController.getFileName(), Constants.DOWNLOAD);
        progressBarSendFile.updateProgressBar(ratioCounter);
        receivingFileClientController.addBytesInFileOutputStream(id, bytes);
        boolean sizeFileCheck = receivingFileClientController.sizeFileCheck();
        boolean end = progressBarSendFile.isEnd();
        if (end) {
            progressBarSendFile.setEnd(false);
            clientController.clientPC.delFile(Paths.get(receivingFileClientController.getDstPath()));
        }
        if (sizeFileCheck) {
            Path parentFilePath = Paths.get(receivingFileClientController.getDstPath()).getParent();
            receivingFileClientController.closeAllFileOutputStreams();
            clientController.clientPC.updateList(parentFilePath);
        }
    }

    @Override
    public void getHandingPatternMatchingResponse(PatternMatchingResponse patternMatchingResponse) {
        Field field = patternMatchingResponse.getField();
        if (field == null) {
            User user = patternMatchingResponse.getUser();
            StatusUser statusUser = patternMatchingResponse.getStatusUser();
            createRegOrAuthClientRequest(user, statusUser);
        } else {
            switch (field) {
                case PASSWORD -> Platform.runLater(AlertWindowsClass::showIncorrectPasswordAlert);
                case USER_NAME -> Platform.runLater(AlertWindowsClass::showIncorrectUserNameAlert);
            }
        }
    }

    @Override
    public void getHandingClearClientMapResponse(ClearClientMapResponse clearClientMapResponse) {
        sendFileClientController.clearSrcPathsMap();
    }

    @Override
    public void getHandingNotCreateNewPathResponse(NotCreateNewPathResponse notCreateNewPathResponse) {
        Platform.runLater(AlertWindowsClass::showNotCreateNextDirectoryAlert);
    }

    @Override
    public void getHandingTheUserIsAlreadyLoggedIn(TheUserIsAlreadyLoggedIn theUserIsAlreadyLoggedIn) {
        Platform.runLater(AlertWindowsClass::showTheUserIsAlreadyLoggedInAlert);
    }

    @Override
    public void getHandingOverwriteFileResponse(OverwriteFileResponse overwriteFileResponse) {
        Platform.runLater(() -> {
            Runnable runnableCreateSendFileRequest = () -> createSendFileRequest(clientController.getSrcPath(),
                    clientController.getDstPath(), clientController.getSelectedFile());
            boolean existsFile = overwriteFileResponse.isExistsFile();
            if (existsFile) {
                boolean continuation = AlertWindowsClass.showOnTheServerFileExistingConfirmation();
                if (continuation) runnableCreateSendFileRequest.run();
            } else runnableCreateSendFileRequest.run();
        });

    }

    @Override
    public void getHandingBanUserResponse(BanUserResponse banUserResponse) {
        Platform.runLater(AlertWindowsClass::showBanUserAlert);
    }

    public void closeClient() {
        nettyClient.exitClient();
    }

    public void createRegOrAuthClientRequest(User user, StatusUser statusUser) {
        sendMessage(new RegOrAuthClientRequest(user, statusUser));
    }

    public void createSendFileRequest(Path srcPath, Path dstPath, FileInfo selectedFile) {
        FileInfo.FileType fileType = selectedFile.getType();
        String fileOrDirectoryName = selectedFile.getFilename();
        sendFileClientController.setNameFileOrDirectorySend(fileOrDirectoryName);
        Path parentDirectory = dstPath.getParent();
        sendFileClientController.setSelectedDstPath(dstPath.toString());
        Map<Long, String> dstPathsMap = new HashMap<>();
        long sizeFile = 0;

        switch (fileType) {

            case FILE -> {
                sizeFile = selectedFile.getSize();
                Long id = sendFileClientController.addNewSrcPath(srcPath.toString());
                dstPathsMap.put(id, dstPath.toString());
            }

            case DIRECTORY -> {
                sizeFile = HelperMethods.sumSizeFiles(srcPath);
                Map<String, String> srcDstMap = HelperMethods.creatDstPaths(srcPath, dstPath);
                for (Map.Entry<String, String> entry : srcDstMap.entrySet()) {
                    Long id = sendFileClientController.addNewSrcPath(entry.getKey());
                    dstPathsMap.put(id, entry.getValue());
                }
            }
        }
        sendFileClientController.createNewCounterFileSize(sizeFile);
        sendMessage(new MetaDataFileRequest(user, dstPathsMap,
                parentDirectory.toString(), sizeFile));
    }

    public void createGetListRequest(String currentPath) {
        sendMessage(new GetListRequest(user, currentPath));
    }

    public void createDelFileRequest(String delFilePath) {
        sendMessage(new DelFileRequest(user, delFilePath));
    }

    public void createUserFolder(Path path) {
        sendMessage(new CreateNewFolderRequest(user, path.toString()));
    }

    public void createGetFileRequest(Path srcPath, Path dstPath, FileInfo selectedFile) {
        receivingFileClientController.setDstPath(dstPath.toString());
        receivingFileClientController.setFileName(selectedFile.getFilename());
        sendMessage(new GetFileRequest(user, selectedFile.getType(), srcPath.toString(), dstPath.toString()));
    }

    public boolean overwriteTheClientFile(Path dstPath) {
        boolean exists = Files.exists(dstPath);
        if (exists) {
            boolean continuation = AlertWindowsClass.showOnTheClientFileExistingConfirmation();
            if (continuation) return true;
            else return false;
        }
        return true;
    }

    public void createOverwriteTheServerFile(Path dstPath) {
        sendMessage(new OverwriteFileRequest(user, dstPath.toString()));
    }

    public void sendMessage(BasicQuery basicQuery) {
        context.writeAndFlush(basicQuery);
    }

    public void setRegAuthGui(RegAuthGui regAuthGui) {
        this.regAuthGui = regAuthGui;
    }

    public void setNettyClient(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    public void setClientLogic(ClientLogic clientLogic) {
        this.clientLogic = clientLogic;
    }

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
    }

    public User getUser() {
        return user;
    }

    public ClientGUI getClientGUI() {
        return clientGUI;
    }

    public ClientController getClientController() {
        return clientController;
    }

    public ReceivingFileClientController getReceivingFileClientController() {
        return receivingFileClientController;
    }

    public void lossOfConnectionToTheServer() {
        Platform.runLater(() -> {
            if (regAuthGui != null) regAuthGui.getStage().close();
            if (clientGUI != null) clientGUI.getStage().close();
            AlertWindowsClass.showLossOfConnectionAlert();
        });
        closeClient();
    }

    public void closeRegAuthGui() {
        Platform.runLater(() -> {
            if (regAuthGui != null) regAuthGui.getStage().close();
        });
    }

    //Поведенческий шаблон "Команда" - методы startClientGUI() и startFalseClientGUI()
    //принимают на вход Runnable c определенной логикой в зависимости от результатов if и switch
    private void startClientGUI(UpdatePanel updatePanel, Runnable runnableComplete) {
        Platform.runLater(() -> {
            regAuthGui.getStage().close();
            runnableComplete.run();
            this.clientGUI = new ClientGUI(clientLogic);
            this.clientController = clientGUI.getClientController();
            clientController.setClientLogic(clientLogic);
            clientController.serverPC.updateList(updatePanel);
        });
        Platform.runLater(() -> this.progressBarSendFile = new ProgressBarSendFile());
    }

    private void startFalseClientGUI(Runnable runnableFalse) {
        Platform.runLater(runnableFalse);
    }


}
