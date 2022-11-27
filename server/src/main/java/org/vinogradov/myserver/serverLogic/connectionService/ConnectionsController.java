package org.vinogradov.myserver.serverLogic.connectionService;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.codec.digest.DigestUtils;
import org.vinogradov.common.commonClasses.Field;
import org.vinogradov.common.commonClasses.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionsController {

    private ConnectionLimit connectionLimit;
    private User user;

    public void newConnectionLimit(ChannelHandlerContext context) {
            connectionLimit = new ConnectionLimit(context);
    }

    public void stopTimerConnectionLimit() {
        connectionLimit.stopTimer();
    }

    public void addDataUser(User user) {
        String name = user.getNameUser();
        String encryptedPassword =  DigestUtils.md5Hex(user.getPassword());
        this.user = new User(name, encryptedPassword);

    }

    public boolean security(User user) {
        String name = user.getNameUser();
        String encryptedPassword = DigestUtils.md5Hex(user.getPassword());
        return  (name.equals(this.user.getNameUser()) && encryptedPassword.equals(this.user.getPassword()));
    }

    public Field patternMatching(String userName, String password) {
        Pattern patternNameUser = Pattern.compile("^[a-zA-Z0-9_.]{1,30}$");
        Pattern patternPassword = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$");
        Matcher matcherNameUser = patternNameUser.matcher(userName);
        Matcher matcherPassword = patternPassword.matcher(password);
        if (!matcherNameUser.matches()) return Field.USER_NAME;
        if (!matcherPassword.matches()) return Field.PASSWORD;
        else return null;
    }


}
