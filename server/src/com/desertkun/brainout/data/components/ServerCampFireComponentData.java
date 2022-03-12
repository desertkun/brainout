package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.CampFireFuelComponent;
import com.desertkun.brainout.content.components.ServerCampFireComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FreePlayItemBurnedEvent;
import com.desertkun.brainout.events.FreePlayItemUsedEvent;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.GameModeFree;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("CampFireComponent")
@ReflectAlias("data.components.CampFireComponentData")
public class ServerCampFireComponentData extends Component<ServerCampFireComponent> implements Json.Serializable, WithTag
{
    private float duration;
    private float check;
    private float burnSpeed;

    public ServerCampFireComponentData(ComponentObject componentObject, ServerCampFireComponent contentComponent)
    {
        super(componentObject, contentComponent);

        duration = 60;
        check = 2.0f;
        burnSpeed = 1.0f;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        check -= dt;
        duration -= dt * burnSpeed;

        if (duration < 0)
        {
            ItemData itemData = ((ItemData) getComponentObject());
            itemData.getMap().removeActive(itemData, true, true, false);
            return;
        }

        if (check < 0)
        {
            check = 2.0f;
            check();
        }
    }

    public float getDuration()
    {
        return duration;
    }

    private void check()
    {
        ItemData itemData = ((ItemData) getComponentObject());
        float bs = burnSpeed;

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode instanceof GameModeFree)
        {
            ServerFreeRealization r = ((ServerFreeRealization) gameMode.getRealization());
            burnSpeed = r.isRain() ? 3 : 1;
        }
        else
        {
            burnSpeed = 1;
        }


        ActiveData closest = getMap().getClosestActiveForTag(32, itemData.getX(), itemData.getY(),
                ActiveData.class, Constants.ActiveTags.WIND, activeData -> true);

        if (closest != null)
        {
            burnSpeed *= 2;
        }

        if (bs != burnSpeed)
        {
            updated(itemData);
        }

        if (duration > 300)
            return;

        ConsumableContainer cnt = itemData.getRecords();

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : cnt.getData())
        {
            ConsumableRecord record = entry.value;
            Content content = record.getItem().getContent();

            CampFireFuelComponent fuel = content.getComponent(CampFireFuelComponent.class);

            if (fuel != null)
            {
                ConsumableContainer.AcquiredConsumables taken = cnt.getConsumable(Math.min(record.getAmount(), fuel.getNeed()), record);
                fuel(fuel, content, record.getWho(), taken.amount, record.getQuality());
                itemData.updated();
                break;
            }
        }
    }

    private void fuel(CampFireFuelComponent fuel, Content content, int who, int taken, int quality)
    {
        ItemData itemData = ((ItemData) getComponentObject());
        this.duration += fuel.getDuration(quality);

        if (content instanceof ConsumableContent)
        {
            Client client = BrainOutServer.Controller.getClients().get(who);
            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) client);
                ModePayload payload = playerClient.getModePayload();
                if (payload instanceof FreePayload)
                {
                    FreePayload freePayload = ((FreePayload) payload);
                    freePayload.questEvent(FreePlayItemBurnedEvent.obtain(playerClient,
                        ((ConsumableContent) content), taken));
                }
            }
        }

        BrainOutServer.Controller.getClients().sendTCP(new LaunchEffectMsg(
            itemData.getDimension(), itemData.getX(), itemData.getY(), getContentComponent().getAddFuel()
        ));
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("d", duration);
        json.writeValue("bs", burnSpeed);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {

    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.CAMP_FIRE);
    }
}
