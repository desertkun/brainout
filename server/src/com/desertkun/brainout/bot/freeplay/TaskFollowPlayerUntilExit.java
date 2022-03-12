package com.desertkun.brainout.bot.freeplay;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskFollowTarget;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.MapDimensionsGraph;
import com.desertkun.brainout.data.WayPointMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.events.DestroyEvent;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("bot.freeplay.TaskFollowPlayerUntilExit")
public class TaskFollowPlayerUntilExit extends Task
{
    private final Runnable exit;
    private PlayerData follow;
    private String partyId;
    private Map goTo;
    private float timer;

    public TaskFollowPlayerUntilExit(TaskStack stack, String goTo, PlayerData follow, Runnable exit)
    {
        super(stack);

        this.goTo = Map.Get(goTo);
        this.follow = follow;

        PlayerClient playerClient = (PlayerClient)BrainOutServer.Controller.getClients().get(follow.getOwnerId());
        this.partyId = playerClient.getPartyId();
        this.exit = exit;
    }

    @Override
    protected void update(float dt)
    {
        timer -= dt;

        if (timer > 0)
            return;

        timer = 0.25f;

        if (!follow.isAlive())
        {
            if (!pickAnotherPlayerToFollow())
            {
                pushTask(new TaskHide(getStack(), null));
            }

            return;
        }

        Map map = getMap();

        if (map == goTo)
        {
            pop();
            BrainOut.EventMgr.sendDelayedEvent(getPlayerData(), DestroyEvent.obtain());
            exit.run();
            return;
        }

        if (MapDimensionsGraph.IsNeighbor(map, goTo))
        {
            ActiveData reFollowTo = goTo.getRandomActiveForTag(Constants.ActiveTags.PORTAL);

            if (reFollowTo != null)
            {
                pushTask(new TaskFollowTarget(getStack(), reFollowTo, null, true));
            }
            return;
        }

        if (getController().isFollowing(follow))
            return;

        if (Vector2.dst2(follow.getX(), follow.getY(), getPlayerData().getX(), getPlayerData().getY()) < 5.0f * 5.0f)
            return;

        getController().follow(follow, this::done, this::stuck, this::gotBlocksInOurWay);
    }

    private boolean pickAnotherPlayerToFollow()
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (!(gameMode.getRealization() instanceof ServerFreeRealization))
        {
            return false;
        }

        ServerFreeRealization freeRealization = ((ServerFreeRealization) gameMode.getRealization());
        ServerFreeRealization.Party party = freeRealization.getParty(partyId);

        if (party == null)
            return false;

        for (ObjectMap.Entry<String, PlayerClient> member : party.getMembers())
        {
            PlayerClient client = member.value;

            if (!client.isAlive())
                continue;

            follow = client.getPlayerData();
            return true;
        }

        return false;
    }

    private void gotBlocksInOurWay(Queue<WayPointMap.BlockCoordinates> blockCoordinates)
    {
        //
    }

    private void stuck()
    {

    }

    private void done()
    {

    }
}
