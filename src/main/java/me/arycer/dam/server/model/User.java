package me.arycer.dam.server.model;

import io.netty.channel.ChannelHandlerContext;

public class User {
    private String username;
    private ChannelHandlerContext ctx;

    public User(String username, ChannelHandlerContext ctx) {
        this.username = username;
        this.ctx = ctx;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }
}