package org.vinogradov.mydto.requests;

import org.vinogradov.mydto.commonClasses.BasicQuery;
import org.vinogradov.mydto.commonClasses.User;

public class BackListRequest implements BasicQuery {

    User user;

    public BackListRequest(User user) {
        this.user = user;
    }

    @Override
    public String getType() {
        return "Back list";
    }

    @Override
    public User getUser() {
        return user;
    }
}