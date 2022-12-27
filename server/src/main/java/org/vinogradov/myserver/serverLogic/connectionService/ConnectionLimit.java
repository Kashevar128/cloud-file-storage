package org.vinogradov.myserver.serverLogic.connectionService;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.vinogradov.common.responses.ConnectionLimitResponse;

import java.util.Timer;
import java.util.TimerTask;

public class ConnectionLimit {

    private ChannelHandlerContext userContext;
    private TimerTask timerTask;
    private Timer timer;
    private long delay;

    // Шаблон "Фабричный метод"
    public static ConnectionLimit of(ChannelHandlerContext context) {
        return new ConnectionLimit(context);
    }

    private ConnectionLimit(ChannelHandlerContext userContext) {
        this.userContext = userContext;
        this.delay = 180000L;
        this.timer = new Timer();
        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                closeConnect();
            }
        };
        timer.schedule(timerTask, delay);
    }

    private void closeConnect() {
        userContext.writeAndFlush(new ConnectionLimitResponse());
        userContext.close();
    }

    public void stopTimer() {
        timer.cancel();
    }


}
