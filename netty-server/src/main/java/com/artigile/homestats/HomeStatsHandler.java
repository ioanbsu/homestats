/*
 * Copyright 2013 The Netty Project
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

import com.artigile.homestats.sensor.HTU21F;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class HomeStatsHandler extends ChannelHandlerAdapter {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeStatsHandler.class);

    private DecimalFormat decimalFormat = new DecimalFormat("###.###");
    private final HTU21F htu21F;
    private final DbService dbService;

    public HomeStatsHandler(final HTU21F htu21F, final DbService dbService) {
        this.dbService = dbService;
        this.htu21F = htu21F;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            byte responseBody[];
            final String uri = req.uri();
            if (uri.startsWith("/getCurrentHumidity")) {
                responseBody = getHumidity();
            } else if (uri.startsWith("/getCurrentTemp")) {
                responseBody = getTemperature();
            } else if (uri.startsWith("/getData")) {
                responseBody = readDataFromDb();
            } else {
                responseBody = "404".getBytes();
            }
            if (HttpHeaderUtil.is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
            boolean keepAlive = HttpHeaderUtil.isKeepAlive(req);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(responseBody));
            response.headers().set(CONTENT_TYPE, "text/plain");
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

            if (!keepAlive) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                ctx.write(response);
            }
        }
    }

    private byte[] readDataFromDb() {
        return dbService.getSerializedStats().getBytes();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private byte[] getTemperature() {
        if (htu21F != null) {
            try {
                return decimalFormat.format(htu21F.readTemperature()).getBytes();
            } catch (Exception e) {
                return ("Failed to read temperature." + e.getMessage()).getBytes();
            }
        } else {
            return "Fake temp: 24".getBytes();
        }
    }

    private byte[] getHumidity() {
        if (htu21F != null) {
            try {
                return decimalFormat.format(htu21F.readHumidity()).getBytes();
            } catch (Exception e) {
                return ("Failed to read humidity." + e.getMessage()).getBytes();
            }
        } else {
            return "Fake humidity: 55".getBytes();
        }
    }
}