package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.active.Player;

public abstract class KillEnemyOfKind extends Task
{
    private String player;

    @Override
    protected void readTask(JsonValue jsonData)
    {
        player = jsonData.getString("player");
    }

    public String getPlayer()
    {
        return player;
    }
}
