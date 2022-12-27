package org.vinogradov.myserver.serverLogic.connectionService;

import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.codec.digest.DigestUtils;
import org.vinogradov.common.commonClasses.Field;
import org.vinogradov.common.commonClasses.User;
import org.vinogradov.myserver.serverLogic.storageService.CloudUser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionsController {

    private ConnectionLimit connectionLimit;
    private ConverterPath converterPath;
    private CloudUser cloudUser;
    private User user;

    public void newConnectionLimit(ChannelHandlerContext context) {
        // Применение шаблона "Фабричный метод"
        connectionLimit = ConnectionLimit.of(context);
    }

    public void stopTimerConnectionLimit() {
        connectionLimit.stopTimer();
    }

    public void addDataUser(User user) {
        String name = user.getNameUser();
        String encryptedPassword = DigestUtils.md5Hex(user.getPassword());
        this.user = new User(name, encryptedPassword);
    }

    public boolean security(User user) {
        String name = user.getNameUser();
        String encryptedPassword = DigestUtils.md5Hex(user.getPassword());
        return (name.equals(this.user.getNameUser()) && encryptedPassword.equals(this.user.getPassword()));
    }

    public Field patternMatching(String userName, String password) {
        if (userName.equals("root") && password.equals("root")) return null;
        Pattern patternNameUser = Pattern.compile("^[a-zA-Z0-9_.]{1,30}$");
        Pattern patternPassword = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$");
        Matcher matcherNameUser = patternNameUser.matcher(userName);
        Matcher matcherPassword = patternPassword.matcher(password);
        if (!matcherNameUser.matches()) return Field.USER_NAME;
        if (!matcherPassword.matches()) return Field.PASSWORD;
        else return null;
    }

    public ConverterPath getConverterPath() {
        return converterPath;
    }

    public void setConverterPath(String rootDirectoryPath) {
        this.converterPath = new ConverterPath(rootDirectoryPath);
    }

    public void addSizeCloud(long size) {
        cloudUser.addSize(size);
    }

    public boolean predictTheSizeCloud(long size) {
        return cloudUser.predictTheSize(size);
    }

    public CloudUser getCloudUser() {
        return cloudUser;
    }

    public void setCloudUser(CloudUser cloudUser) {
        this.cloudUser = cloudUser;
    }

    public ConnectionLimit getConnectionLimit() {
        return connectionLimit;
    }

    public void setConnectionLimit(ConnectionLimit connectionLimit) {
        this.connectionLimit = connectionLimit;
    }
}
