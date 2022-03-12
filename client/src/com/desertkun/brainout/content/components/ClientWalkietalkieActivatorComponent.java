package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.common.msg.client.ChangeFrequencyMsg;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.WalkietalkieConsumableItem;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.menu.impl.WalkietalkieMenu;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientWalkietalkieActivatorComponent")
public class ClientWalkietalkieActivatorComponent extends ClientItemActivatorComponent
{
    private SoundEffect touchdown;
    private SoundEffect changeFrequency;
    private Array<String> digits;
    private String empty;

    public ClientWalkietalkieActivatorComponent()
    {
        digits = new Array<>();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {
    }

    public void activate(ConsumableRecord record)
    {
        ConsumableItem item = record.getItem();
        if (!(item instanceof WalkietalkieConsumableItem))
            return;

        WalkietalkieConsumableItem walkietalkie = (WalkietalkieConsumableItem) item;

        BrainOutClient.getInstance().topState().pushMenu(new WalkietalkieMenu(
                walkietalkie.getFrequency(), digits, empty, touchdown, changeFrequency,
                frequency -> BrainOutClient.ClientController.sendTCP(
                        new ChangeFrequencyMsg(frequency))
        ));
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        empty = jsonData.getString("empty", "");

        if (jsonData.has("digits"))
        {
            for (JsonValue digit : jsonData.get("digits"))
            {
                this.digits.add(digit.asString());
            }
        }
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        changeFrequency = BrainOutClient.ContentMgr.get("change-walkietalkie-frequency", SoundEffect.class);
        touchdown = BrainOutClient.ContentMgr.get("walkietalkie-click-touchdown", SoundEffect.class);
    }
}
