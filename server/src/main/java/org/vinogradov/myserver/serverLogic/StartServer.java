package org.vinogradov.myserver.serverLogic;

public class StartServer {
    public static void main(String[] args) {
        try {
            new NettyServer();
        } catch (InterruptedException e) {
            throw new RuntimeException("Проблемы при запуске сервера");
        }
    }
}
