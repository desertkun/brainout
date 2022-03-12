package com.desertkun.brainout.data.battlepass;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.battlepass.BattlePass;
import com.desertkun.brainout.content.battlepass.KillFromWeaponKind;
import com.desertkun.brainout.content.components.RandomWeightComponent;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.online.UserProfile;
import org.json.JSONObject;

import java.math.BigInteger;

public class KillFromWeaponKindData extends BattlePassTaskData<KillFromWeaponKind>
{
    private Weapon weapon;

    public KillFromWeaponKindData(BattlePassData data, KillFromWeaponKind task, BattlePass.TasksDefinition tasksDefinition,
        byte[] taskHash, UserProfile userProfile, JSONObject eventProfile, String taskKey)
    {
        super(data, task, tasksDefinition, userProfile, eventProfile, taskKey);

        calculateWeapon(taskHash, eventProfile);
    }

    private void calculateWeapon(byte[] taskHash, JSONObject eventProfile)
    {
        if (eventProfile != null && eventProfile.has(getSelectedStringKey()))
        {
            String selectedWeaponKey = eventProfile.getString(getSelectedStringKey());
            weapon = BrainOut.ContentMgr.get(selectedWeaponKey, Weapon.class);
            if (weapon != null)
            {
                return;
            }
        }

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

            if (slot.isLocked(getUserProfile()))
                return false;

            if (c.getPrimaryProperties().isUnlimited())
                return false;

            return slot.getCategory().equals(getTask().getKind());
        });

        if (weaponPool.size == 0)
        {
            throw new RuntimeException("Empty weapon pool.");
        }

        int intHash = Math.abs(new BigInteger(taskHash).intValue());
        weapon = weaponPool.get(intHash % weaponPool.size);
    }

    private String getSelectedStringKey()
    {
        return "sel_" + getTaskKey();
    }

    @Override
    protected void onCommit(JSONObject profileUpdate)
    {
        super.onCommit(profileUpdate);

        if (weapon != null)
        {
            profileUpdate.put(getSelectedStringKey(), weapon.getID());
        }
    }

    @Override
    public String getTaskTitle()
    {
        return L.get("QUEST_TASK_KILL_WITH", new String[]{weapon.getTitle().get()});
    }

    @Override
    public boolean getTaskActionMatches(String action)
    {
        return action.equals(weapon.getKillsStat());
    }
}
