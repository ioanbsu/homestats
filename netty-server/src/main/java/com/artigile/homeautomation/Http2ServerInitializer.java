package com.artigile.homeautomation;


import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.ssl.SslContext;

import java.util.Collections;

/**
 * Sets up the Netty pipeline for the example server. Depending on the endpoint config, sets up the
 * pipeline for NPN or cleartext HTTP upgrade to HTTP/2.
 */
public class Http2ServerInitializer extends ChannelInitializer<SocketChannel> {
    private final SslContext sslCtx;

    public Http2ServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        if (sslCtx != null) {
            configureSsl(ch);
        } else {
            configureClearText(ch);
        }
    }

    /**
     * Configure the pipeline for TLS NPN negotiation to HTTP/2.
     */
    private void configureSsl(SocketChannel ch) {
        ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()), new Http2OrHttpHandler());
    }

    /**
     * Configure the pipeline for a cleartext upgrade from HTTP to HTTP/2.
     */
    private static void configureClearText(SocketChannel ch) {
        HttpServerCodec sourceCodec = new HttpServerCodec();
        HttpServerUpgradeHandler.UpgradeCodec upgradeCodec =
                new Http2ServerUpgradeCodec(new HelloWorldHttp2Handler());
        HttpServerUpgradeHandler upgradeHandler =
                new HttpServerUpgradeHandler(sourceCodec, Collections.singletonList(upgradeCodec), 65536);

        ch.pipeline().addLast(sourceCodec);
        ch.pipeline().addLast(upgradeHandler);
        ch.pipeline().addLast(new UserEventLogger());
    }

    /**
     * Class that logs any User Events triggered on this channel.
     */
    private static class UserEventLogger extends ChannelHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            System.out.println("User Event Triggered: " + evt);
            ctx.fireUserEventTriggered(evt);
        }
    }
}