package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.common.msg.client.FriendListMsg;
import com.desertkun.brainout.common.msg.client.HelloMsg;
import com.desertkun.brainout.common.msg.server.ServerInfo;
import com.desertkun.brainout.common.msg.server.VersionMismatchMsg;
import com.desertkun.brainout.online.Preset;
import com.desertkun.brainout.utils.Compressor;
import org.anthillplatform.runtime.services.LoginService;
import org.json.JSONException;
import org.json.JSONObject;

public class CSConnected extends ControllerState
{
    private final String key;
    private final int reconnect;

    public CSConnected(String key, int reconnect)
    {
        this.key = key;
        this.reconnect = reconnect;
    }

    @Override
    public ID getID()
    {
        return ID.connected;
    }

    @SuppressWarnings("unused")
    public boolean received(VersionMismatchMsg mismatchMsg)
    {
        switchTo(new CSError(L.get("MENU_VERSION_MISMATCH", mismatchMsg.serverVersion)));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ServerInfo serverInfo)
    {
        Gdx.app.postRunnable(() ->
        {
            ClientController controller = getController();
            controller.setOwnerKey(serverInfo.ownerKey);
            controller.setCurrentPartyId(serverInfo.partyId);

            BrainOutClient.PackageMgr.clearDefines();

            for (ServerInfo.KeyValue define : serverInfo.defines)
            {
                BrainOutClient.PackageMgr.setDefine(define.name, define.value);
            }

            // init basic things
            controller.setId(serverInfo.id);
            controller.setServerTime(serverInfo.time);

            for (ServerInfo.KeyValue lvl : serverInfo.levels)
            {
                controller.setLevelsName(lvl.name, lvl.value);
            }

            for (ServerInfo.Price price : serverInfo.prices)
            {
                controller.setPrice(price.name, price.value);
            }

            controller.setMaxPlayers(serverInfo.maxPlayers);

            // set the play state
            Gdx.app.postRunnable(() -> controller.setPlayState(serverInfo.playState, serverInfo.playData));

            sendFriendList();

            if (serverInfo.preset != null)
            {
                try
                {
                    Preset preset = new Preset();
                    JsonReader reader = new JsonReader();
                    JsonValue presetValue = reader.parse(serverInfo.preset);
                    preset.read(new Json(), presetValue);

                    controller.setPreset(preset);
                }
                catch (Exception ignored)
                {
                    controller.setPreset(null);
                }
            }
            else
            {
                controller.setPreset(null);
            }

            if (serverInfo.userProfile != null)
            {
                JSONObject o;

                try
                {
                    byte[] data = serverInfo.userProfile;
                    String decompressed = Compressor.DecompressToString(data);
                    o = new JSONObject(decompressed);
                }
                catch (Exception ignored)
                {
                    o = null;
                }

                if (o != null)
                {
                    getController().getUserProfile().read(o);
                    BrainOutClient.SocialController.userProfileUpdated(getController().getUserProfile());
                }
            }
        });

        return true;
    }

    private void sendFriendList()
    {
        Array<GameUser.Friend> friends = new Array<>();

        if (BrainOutClient.Env.getGameUser().getFriends(friends))
        {
            if (friends.size <= 0)
                return;

            String[] credentials = new String[friends.size];

            int i = 0;

            for (GameUser.Friend friend : friends)
            {
                credentials[i] = friend.getCredential();
                i++;
            }

            BrainOutClient.ClientController.sendTCP(new FriendListMsg(credentials));
        }
    }

    @Override
    public void init()
    {
        // send hello

        if (BrainOut.OnlineEnabled())
        {
            LoginService loginService = LoginService.Get();

            if (loginService == null)
            {
                switchTo(new CSError("No login service!"));
                return;
            }

            String key = this.key;
            String token = null;

            if (key == null)
            {
                token = loginService.getCurrentAccessToken().get();
            }

            getController().sendTCP(new HelloMsg(Version.VERSION, key, token, reconnect));
        }
        else
        {
            getController().sendTCP(new HelloMsg(Version.VERSION, null, null, reconnect));
        }
    }

    @Override
    public void release()
    {

    }
}
