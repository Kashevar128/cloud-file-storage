package org.vinogradov.myserver.serverLogic.consoleService;

import java.util.HashMap;
import java.util.Map;

public class ListCommand {

    private final Map<String, CommandEnum> commandEnumMap;

    public ListCommand() {
        this.commandEnumMap = new HashMap<>();
        commandEnumMap.put("newUser", CommandEnum.CREATE_NEW_USER);
        commandEnumMap.put("delUser", CommandEnum.DELETE_USER);
        commandEnumMap.put("back", CommandEnum.BACK);
        commandEnumMap.put("move", CommandEnum.MOVE);
        commandEnumMap.put("ban", CommandEnum.BAN_USER);
        commandEnumMap.put("newFolder", CommandEnum.CREATE_NEW_PACKAGE);
        commandEnumMap.put("delFile", CommandEnum.DELETE_PACKAGE);
        commandEnumMap.put("clear", CommandEnum.CLEAR);
        commandEnumMap.put("exit", CommandEnum.EXIT);
        commandEnumMap.put("curPath", CommandEnum.CURRENT_PATH);
        commandEnumMap.put("root", CommandEnum.ROOT);
        commandEnumMap.put("entry", CommandEnum.ENTRY);
        commandEnumMap.put("allUsers", CommandEnum.All_USERS_DB);
        commandEnumMap.put("getUser", CommandEnum.USER_DB);
        commandEnumMap.put("unBan", CommandEnum.UNBAN_USER);
        commandEnumMap.put("setSizeStorage", CommandEnum.SET_SIZE_STORAGE);
        commandEnumMap.put("listOnline", CommandEnum.USERS_ONLINE);
        commandEnumMap.put("help", CommandEnum.HELP);
    }

    public CommandEnum getCommand(String command) {
        return commandEnumMap.get(command);
    }

    public boolean isACommand(String command) {
        return commandEnumMap.containsKey(command);
    }

    public enum CommandEnum {
        CREATE_NEW_USER,
        DELETE_USER,
        BACK,
        MOVE,
        BAN_USER,
        CREATE_NEW_PACKAGE,
        DELETE_PACKAGE,
        CLEAR,
        EXIT,
        CURRENT_PATH,
        ROOT,
        ENTRY,
        All_USERS_DB,
        USER_DB,
        UNBAN_USER,
        SET_SIZE_STORAGE,
        USERS_ONLINE,
        HELP
    }


}
