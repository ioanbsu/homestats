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
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

public class HomeStatsServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    private final DbService dbService;
    private final HTU21F htu21F;

    public HomeStatsServerInitializer(SslContext sslCtx, final DbService dbService, final HTU21F htu21F) {
        this.sslCtx = sslCtx;
        this.dbService = dbService;
        this.htu21F = htu21F;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
        p.addLast(new HttpServerCodec());
        p.addLast(new HomeStatsHandler(htu21F, dbService));
    }
}