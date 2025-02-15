package me.arycer.dam.shared.utils;

import com.google.gson.Gson;
import me.arycer.dam.shared.protocol.Message;

public class MessageUtils {
    private static final Gson gson = new Gson();

    public static String serialize(Message message) {
        return gson.toJson(message);
    }

    public static Message deserialize(String json) {
        return gson.fromJson(json, Message.class);
    }
}