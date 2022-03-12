package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.content.components.ServerRoundLockComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.*;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.inspection.InspectableProperty;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.minlog.Log;

import java.util.StringJoiner;
import java.util.stream.IntStream;

@Reflect("ServerRoundLockComponent")
@ReflectAlias("data.components.ServerRoundLockComponentData")
public class ServerRoundLockComponentData extends Component<ServerRoundLockComponent> implements WithTag,
    Json.Serializable
{
    private final RoundLockSafeData safe;

    @InspectableProperty(name = "door-sprite", kind = PropertyKind.string, value = PropertyValue.vString)
    public String doorSprite;

    public ServerRoundLockComponentData(RoundLockSafeData safe,
                                        ServerRoundLockComponent contentComponent)
    {
        super(safe, contentComponent);

        this.safe = safe;
        this.doorSprite = "";
    }

    @Override
    public void init()
    {
        super.init();

        changeCode();
    }

    private void changeCode()
    {
        safe.setLocked(true);

        int[] code = new int[3];

        for (int i = 0; i < 3; i++)
        {
            code[i] = MathUtils.random(0, 99);
        }

        StringJoiner sj = new StringJoiner(",");
        IntStream.of(code).forEach(x -> sj.add(String.valueOf(x)));

        if (Log.INFO) Log.info("Round lock code for " + getMap().getDimension() + ": " + safe.getCode());
        safe.setCode(sj.toString());
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case activeActivateData:
            {
                ActivateActiveEvent ev = ((ActivateActiveEvent) event);
                unlock(ev.client, ev.playerData, ev.payload);
                return true;
            }
        }

        return false;
    }

    private void unlock(Client client, PlayerData playerData, String payload)
    {
        if (!(client instanceof PlayerClient))
            return;

        if (!safe.isLocked())
        {
            return;
        }

        if (payload.equals(safe.getCode()))
        {
            unlocked(((PlayerClient) client));
        }
    }

    public void lock()
    {
        changeCode();
        safe.updated();

        SpriteData doorSprite = getDoorSprite();

        if (doorSprite != null)
        {
            doorSprite.spriteName = getContentComponent().getSafeClosedTexture();
            doorSprite.updated();
        }
    }

    private void unlocked(PlayerClient client)
    {
        safe.setLocked(false);
        safe.updated();

        client.addStat("round-locks-opened", 1);

        String effect = getContentComponent().getActivateEffect();

        if (!effect.isEmpty())
        {
            BrainOutServer.Controller.getClients().sendUDP(
                new LaunchEffectMsg(safe.getDimension(), safe.getX(), safe.getY(), effect));
        }

        SpriteData doorSprite = getDoorSprite();

        if (doorSprite != null)
        {
            doorSprite.spriteName = getContentComponent().getSafeActivatedTexture();
            doorSprite.updated();
        }
    }

    private ActiveData findActiveData(String tag)
    {
        for (Map map : Map.All())
        {
            ActiveData found = map.getActiveNameIndex().get(tag);

            if (found != null)
                return found;
        }

        return null;
    }

    public SpriteData getDoorSprite()
    {
        ActiveData activeData = findActiveData(doorSprite);

        if (!(activeData instanceof SpriteData))
            return null;

        return ((SpriteData) activeData);
    }

    @Override
    public int getTags()
    {
        return 0;
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("door-sprite", doorSprite);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        doorSprite = jsonData.getString("door-sprite", "");
    }
}
