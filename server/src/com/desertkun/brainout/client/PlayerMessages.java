package com.desertkun.brainout.client;

import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.services.MessageService;
import org.json.JSONObject;

import java.util.Date;
import java.util.Set;

public class PlayerMessages implements MessageService.MessageSession.Listener
{
    private PlayerClient playerClient;
    private ObjectMap<String, PlayerMessagesHandler> handlers;

    public interface PlayerMessagesHandler
    {
        boolean handle(String recipientClass, String recipientKey,
                       String messageId, Date time, String sender, int gamespace,
                       JSONObject payload, Set<String> flags);
    }

    public PlayerMessages(PlayerClient playerClient)
    {
        this.playerClient = playerClient;
        this.handlers = new ObjectMap<>();
    }

    public void release()
    {
        handlers.clear();
        playerClient = null;
    }

    public void addHandler(String messageType, PlayerMessagesHandler handler)
    {
        handlers.put(messageType, handler);
    }

    @Override
    public void onOpen()
    {
        if (Log.INFO) Log.info("Connected to messages!");

        if (playerClient != null)
        {
            playerClient.onMessagesOpen();
        }
    }

    @Override
    public void onMessage(String messageType, String recipientClass, String recipientKey,
                          String messageId, Date time, String sender, int gamespace,
                          JSONObject payload, Set<String> flags)
    {
        PlayerMessagesHandler handler = handlers.get(messageType);

        if (handler != null)
        {
            if (handler.handle(recipientClass, recipientKey, messageId, time, sender, gamespace, payload, flags))
                return;
        }

        playerClient.sendSocialMessage(messageType, recipientClass, recipientKey,
            messageId, time, sender, payload, flags);
    }

    @Override
    public void onMessageDeleted(String messageId, String sender, int gamespace)
    {
        playerClient.socialMessageDeleted(messageId, sender);
    }

    @Override
    public void onMessageUpdated(String messageId, String sender, int gamespace, JSONObject payload)
    {
        playerClient.socialMessageUpdated(messageId, sender, payload);
    }

    @Override
    public void onError(int code, String message, String data)
    {
        if (Log.ERROR) Log.error("Error: " + code + " " + message + (data != null ? ", " + data: ""));
    }

    @Override
    public void onClose(int code, String reason, boolean remote)
    {
        if (Log.INFO) Log.info("Connection closed: " + code + " reason " + reason);
    }

    @Override
    public void onError(Exception ex)
    {
        ex.printStackTrace();
    }
}
