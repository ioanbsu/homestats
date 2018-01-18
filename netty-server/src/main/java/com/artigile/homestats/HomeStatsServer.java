/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.artigile.homestats;

import static com.artigile.homestats.ArgsParser.APP_MODE_OPTION;
import static com.artigile.homestats.ArgsParser.APP_PORT_OPTION;
import static com.artigile.homestats.ArgsParser.DB_HOST_OPTION;
import static com.artigile.homestats.ArgsParser.DB_PWD_OPTION;
import static com.artigile.homestats.ArgsParser.DB_USER_OPTION;
import static com.artigile.homestats.ArgsParser.PRINT_AND_EXIT;

import com.artigile.homestats.sensor.BMP085AnfDht11;
import com.artigile.homestats.sensor.HTU21F;
import com.artigile.homestats.sensor.SensorFactory;
import com.artigile.homestats.sensor.SensorMode;
import com.artigile.homestats.sensor.SensorsDataProvider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HTTP server that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 */
public final class HomeStatsServer {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeStatsServer.class);

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "28080"));


    public static void main(String[] args) throws Exception {
        ArgsParser argsParser = new ArgsParser(args);
        if (argsParser.isDisplayHelp()) {
            ArgsParser.printHelp();
            return;
        }

        EventLoopGroup bossGroup = null;
        EventLoopGroup workerGroup = null;
        try {
            SensorMode appMode = SensorMode.valueOf(
                argsParser.getString(ArgsParser.APP_MODE_OPTION, "dev").toUpperCase());

            SensorsDataProvider sensorsDataProvider = SensorFactory.buildSensorDataProvider(appMode);
            if(sensorsDataProvider==null){
                LOGGER.error("No sensor device available, quitting.");
                return;
            }

            final boolean printAndExit = argsParser.argumentPassed(ArgsParser.PRINT_AND_EXIT);
            if (printAndExit) {
                sensorsDataProvider.printAll();
                return;
            }

            final String dbHost = argsParser.getString(DB_HOST_OPTION, "localhost");
            final String user = argsParser.getString(DB_USER_OPTION);
            final String pwd = argsParser.getString(DB_PWD_OPTION);
            final int port = Integer.valueOf(argsParser.getString(APP_PORT_OPTION, PORT + ""));

            LOGGER.info("Connecting to {}, user {}, pwd: {}", dbHost, user, pwd);
            final DbDao dbDao = new DbDao(dbHost, user, pwd);
            new DataService(sensorsDataProvider, dbDao, 1000 * 60 * 5).start();


            // Configure SSL.
            final SslContext sslCtx;
            if (SSL) {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
            } else {
                sslCtx = null;
            }

            // Configure the server.
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new HomeStatsServerInitializer(sslCtx, dbDao, sensorsDataProvider));

            Channel ch = b.bind(port).sync().channel();

            System.err.println("Open your web browser and navigate to " +
                    (SSL ? "https" : "http") + "://127.0.0.1:" + port + '/');

            ch.closeFuture().sync();
        } finally {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
        }
    }
}