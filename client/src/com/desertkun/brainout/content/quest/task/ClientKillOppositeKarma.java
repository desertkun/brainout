package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.task.KillOppositeKarma")
public class ClientKillOppositeKarma extends KillOppositeKarma implements ClientTask
{
    @Override
    public void renderIcon(WidgetGroup to)
    {
    }

    @Override
    public boolean showInSummaryScreen()
    {
        return false;
    }

    @Override
    public boolean hasLocalizedName()
    {
        return true;
    }

    @Override
    public boolean hasRichLocalization()
    {
        RemoteClient my = BrainOutClient.ClientController.getMyRemoteClient();
        if (my == null)
        {
            return false;
        }

        int karma = my.getInfoInt("karma", 0);

        if (karma >= -1 && karma <= 1)
        {
            return false;
        }

        return true;
    }

    @Override
    public String getLocalizedName()
    {
        RemoteClient my = BrainOutClient.ClientController.getMyRemoteClient();
        if (my == null)
        {
            return L.get("QUEST_TASK_OBTAIN_REPUTATION");
        }

        int karma = my.getInfoInt("karma", 0);

        if (karma >= -1 && karma <= 1)
        {
            return L.get("QUEST_TASK_OBTAIN_REPUTATION");
        }

        if (karma > 0)
        {
            return L.get("QUEST_TASK_KILL_HITMEN");
        }
        else
        {
            return L.get("QUEST_TASK_KILL_ENFORCERS");
        }
    }

    @Override
    public String getShortLocalizedName()
    {
        return L.get("QUEST_TITLE_BOUNTY_HUNTER");
    }

    @Override
    public boolean hasIcon()
    {
        return false;
    }

    @Override
    public boolean hasProgress()
    {
        return true;
    }

    @Override
    public boolean isItemTaskRelated(ConsumableItem item)
    {
        return false;
    }
}
