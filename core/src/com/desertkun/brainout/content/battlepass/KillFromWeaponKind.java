package com.desertkun.brainout.content.battlepass;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.RandomWeightComponent;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.data.battlepass.BattlePassData;
import com.desertkun.brainout.data.battlepass.KillFromWeaponKindData;
import com.desertkun.brainout.online.BattlePassEvent;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import org.json.JSONObject;

@Reflect("content.battlepass.KillFromWeaponKind")
public class KillFromWeaponKind extends BattlePassTask
{
    private String kind;

    @Override
    public KillFromWeaponKindData getData(BattlePassData data, BattlePass.TasksDefinition tasksDefinition,
        byte[] taskHash, UserProfile userProfile, JSONObject eventProfile, String taskKey)
    {
        return new KillFromWeaponKindData(data, this, tasksDefinition, taskHash, userProfile, eventProfile, taskKey);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        kind = jsonData.getString("kind");
    }

    @Override
    public boolean validate(UserProfile userProfile)
    {
        Array<Weapon> weaponPool = BrainOut.ContentMgr.queryContent(Weapon.class,
            c ->
        {
            InstrumentSlotItem slot = c.getSlotItem();

            if (slot == null)
                return false;

            if (RandomWeightComponent.Get(slot) == 0)
                return false;

            if (slot.getDefaultSkin() == null)
                return false;

            if (slot.isLocked(userProfile))
                return false;

            if (c.getPrimaryProperties().isUnlimited())
                return false;

            return slot.getCategory().equals(getKind());
        });

        return weaponPool.size > 0;
    }

    public String getKind()
    {
        return kind;
    }
}
