package org.vinogradov.mydto.responses;

import org.vinogradov.mydto.commonClasses.BasicQuery;
import org.vinogradov.mydto.commonClasses.User;

import java.util.List;

public class AuthServerResponse implements BasicQuery {

    private boolean authComplete;

    private User user;

    private List<String> startList;

    public AuthServerResponse(boolean authComplete, User user, List<String> startList) {
        this.authComplete = authComplete;
        this.user = user;
        this.startList = startList;
    }

    public AuthServerResponse(boolean authComplete) {
        this.authComplete = authComplete;
    }

    @Override
    public String getType() {
        return "Server auth";
    }

    public boolean isAuthComplete() {
        return authComplete;
    }

    @Override
    public User getUser() {
        return user;
    }

    public List<String> getStartList() {
        return startList;
    }
}