/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.code4everything.wetool.plugin.support.http;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.support.exception.HttpBadReqException;

import java.util.Objects;
import java.util.StringTokenizer;

/**
 * @author pantao
 * @since 2020/12/5
 */
@Slf4j
@RequiredArgsConstructor
public class HttpServiceHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final int port;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            long start = System.currentTimeMillis();
            HttpRequest req = (HttpRequest) msg;
            QueryStringDecoder decoder = new QueryStringDecoder(req.uri());

            String api = req.method().name().toLowerCase() + decoder.path();
            FullHttpResponse response = getResponse(req, api, decoder);

            boolean keepAlive = HttpUtil.isKeepAlive(req);
            if (Objects.isNull(response.status())) {
                response.setStatus(HttpResponseStatus.OK);
            }
            if (Objects.isNull(response.protocolVersion())) {
                response.setProtocolVersion(req.protocolVersion());
            }
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON).setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

            if (keepAlive) {
                if (!req.protocolVersion().isKeepAliveDefault()) {
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                }
            } else {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            }

            ChannelFuture f = ctx.write(response);

            long runTime = System.currentTimeMillis() - start;
            log.info("request api: {}, status: {}, run time: {}ms", api, response.status().code(), runTime);
            if (!keepAlive) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    private JSONObject parseReqBody(HttpRequest request) {
        if (request instanceof FullHttpRequest) {
            ByteBuf content = ((FullHttpRequest) request).content();
            if (content.isReadable()) {
                String json = content.toString(CharsetUtil.CHARSET_UTF_8);
                try {
                    return JSON.parseObject(json);
                } catch (Exception e) {
                    log.error("parse request body error: {}", ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
                }
            }
        }
        return new JSONObject();
    }

    private JSONObject parseParams(String queryString) {
        JSONObject jsonObject = new JSONObject();
        if (StrUtil.isEmpty(queryString)) {
            return jsonObject;
        }

        StringTokenizer tokenizer = new StringTokenizer(queryString, "&");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            String[] keyValue = token.split("=");
            boolean hasValue = keyValue.length > 1;
            jsonObject.put(keyValue[0], hasValue ? URLDecoder.decode(keyValue[1], CharsetUtil.CHARSET_UTF_8) : null);
        }

        return jsonObject;
    }

    private FullHttpResponse getResponse(HttpRequest req, String api, QueryStringDecoder decoder) {
        HttpVersion httpVersion = req.protocolVersion();
        HttpApiHandler apiHandler = HttpService.HTTP_SERVICE.get(port).get(api);

        FullHttpResponse response;

        if (Objects.isNull(apiHandler)) {
            response = new DefaultFullHttpResponse(httpVersion, HttpResponseStatus.NOT_FOUND,
                    Unpooled.wrappedBuffer(new byte[0]));
        } else {
            JSONObject params = parseParams(decoder.rawQuery());
            JSONObject body = parseReqBody(req);
            response = new WeFullHttpResponse(httpVersion, HttpResponseStatus.OK);

            try {
                Object responseObject = apiHandler.handleApi(req, response, params, body);
                String respStr = "{}";
                if (Objects.nonNull(responseObject)) {
                    respStr = JSON.toJSONString(responseObject, SerializerFeature.QuoteFieldNames,
                            SerializerFeature.WriteMapNullValue, SerializerFeature.WriteEnumUsingToString,
                            SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty,
                            SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullBooleanAsFalse,
                            SerializerFeature.SkipTransientField, SerializerFeature.WriteNonStringKeyAsString);
                }
                ((WeFullHttpResponse) response).setContent(str2buf(respStr));
            } catch (Exception e) {
                HttpResponseStatus status;
                String errMsg;
                if (e instanceof HttpBadReqException) {
                    status = HttpResponseStatus.BAD_REQUEST;
                    errMsg = e.getMessage();
                } else {
                    status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
                    errMsg = ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE);
                    log.error("[{}] api service error, request api: {}, error: {}", port, api, errMsg);
                }

                response = new DefaultFullHttpResponse(httpVersion, status, str2buf(errMsg));
            }
        }

        return response;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(ExceptionUtil.stacktraceToString(cause, Integer.MAX_VALUE));
        ctx.close();
    }

    private ByteBuf str2buf(String str) {
        return Unpooled.copiedBuffer(str, CharsetUtil.CHARSET_UTF_8);
    }
}