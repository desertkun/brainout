package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Flag;
import com.desertkun.brainout.content.components.ServerFlagComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FlagTakenEvent;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerFlagComponent")
@ReflectAlias("data.components.ServerFlagComponentData")
public class ServerFlagComponentData extends Component<ServerFlagComponent>
{
    private final FlagData flagData;
    private float checkTime;

    private static ObjectMap<Team, Integer> teams = new ObjectMap<>();
    private static Array<Team> leadTeam = new Array<>();
    private static Vector2 tmp = new Vector2();

    public ServerFlagComponentData(FlagData flagData, ServerFlagComponent flagComponent)
    {
        super(flagData, flagComponent);
        
        this.flagData = flagData;
        this.checkTime = 0;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        checkTime -= dt;
        
        if (checkTime < 0)
        {
            checkTime = 0.5f;
            
            checkTaking();
        }
    }

    public float getTakingDistance()
    {
        return getContentComponent().getTakeDistance();
    }

    private boolean checkPlayer(PlayerData playerData)
    {
        // ignore spectators
        tmp.set(flagData.getX(), flagData.getY());

        return (tmp.dst(playerData.getX(), playerData.getY()) < getContentComponent().getTakeDistance());
    }

    private void checkTaking()
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode == null)
            return;

        if (!gameMode.isGameActive())
            return;

        if (!((ServerRealization) gameMode.getRealization()).canTakeFlags())
            return;

        teams.clear();
        leadTeam.clear();

        Map map = getMap();

        for (ActiveData active : map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
        {
            if (active instanceof PlayerData)
            {
                PlayerData playerData = ((PlayerData) active);

                if (checkPlayer(playerData))
                {
                    int amount = teams.get(playerData.getTeam()) != null ? teams.get(playerData.getTeam()) : 0;

                    teams.put(playerData.getTeam(), amount + 1);
                }
            }
        }

        switch (teams.size)
        {
            case 0:
            {
                if (flagData.getState() == FlagData.State.taking)
                {
                    // cancel the flag taking
                    stopTakingTeam();
                }

                break;
            }
            case 1:
            {
                ObjectMap.Entry<Team, Integer> pair = teams.iterator().next();

                Team newTeam = pair.key;
                int amount = pair.value;

                dominate(newTeam, amount);
                break;
            }
            default:
            {
                leadTeam.addAll(teams.keys().toArray());

                leadTeam.sort((o1, o2) -> teams.get(o1) < teams.get(o2) ? 1 : -1);

                Team bestTeam = leadTeam.get(0);
                Team notBestTeam = leadTeam.get(1);

                int difference = teams.get(bestTeam) - teams.get(notBestTeam);

                if (difference == 0)
                {
                    // 2 and more
                    if (flagData.getState() == FlagData.State.taking)
                    {
                        // not waste the time but stop counting
                        pauseTakingTeam();
                    }
                }
                else
                {
                    dominate(bestTeam, difference);
                }
                break;
            }
        }
    }

    private void dominate(Team newTeam, int amount)
    {
        if (flagData.getTeam() != null && flagData.getTeam() != newTeam)
        {
            // if the flag is already taken, first we need to untake it

            newTeam = null;
        }

        switch (flagData.getState())
        {
            case normal:
            {
                if (newTeam != flagData.getTeam())
                {
                    // act taking the flag
                    startTakingTeam(newTeam, (float)amount);
                }
                break;
            }
            case paused:
            {
                if (newTeam == flagData.getTakingTeam())
                {
                    // continue paused taking
                    continueTakingTeam((float)amount);
                }
                else
                {
                    // if the different team from original then restart
                    startTakingTeam(newTeam, (float)amount);
                }
                break;
            }
            case taking:
            {
                if (newTeam == flagData.getTakingTeam())
                {
                    flagData.setTakeSpeed(amount);
                    flagData.updated();
                }
                else
                {
                    startTakingTeam(newTeam, amount);
                }
            }
        }
    }

    private void continueTakingTeam(float takeSpeed)
    {
        flagData.setState(FlagData.State.taking);
        flagData.setTakeSpeed(takeSpeed);

        flagData.updated();

        BrainOutServer.Controller.checkSpawn(flagData);
    }

    private void pauseTakingTeam()
    {
        flagData.setState(FlagData.State.paused);
        flagData.updated();

        BrainOutServer.Controller.checkSpawn(flagData);
    }

    private void stopTakingTeam()
    {
        flagData.setState(FlagData.State.normal);
        flagData.setTime(0);
        flagData.setTakingTeam(null);

        flagData.updated();

        BrainOutServer.Controller.checkSpawn(flagData);
    }

    private void startTakingTeam(Team topTeam, float takeSpeed)
    {
        flagData.setState(FlagData.State.taking);
        flagData.setTime(((Flag) flagData.getCreator()).getTakeTime());
        flagData.setTakingTeam(topTeam);
        flagData.setTakeSpeed(takeSpeed);

        flagData.updated();

        BrainOutServer.Controller.checkSpawn(flagData);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case simple:
            {
                SimpleEvent simpleEvent = ((SimpleEvent) event);

                switch (simpleEvent.getAction())
                {
                    case flagTakeChanged:
                    {
                        flagTakeChanged();
                        break;
                    }
                }
            }
        }

        return false;
    }

    private void flagTakeChanged()
    {
        BrainOutServer.Controller.checkSpawn(flagData);

        Team takingTeam = flagData.getTakingTeam();
        float a = BrainOutServer.getInstance().getSettings().getPrice("takePoint");

        // notify everyone
        for (ObjectMap.Entry<Integer, Client> clientEntry : BrainOutServer.Controller.getClients())
        {
            Client client = clientEntry.value;

            if (client.getTeam() == takingTeam)
            {
                boolean shouldAward = client.getPlayerData() != null && checkPlayer(client.getPlayerData());
                if (shouldAward)
                {
                    client.addStat("capture-flags", 1);
                    client.setFlagsCapturedThisGame(client.getFlagsCapturedThisGame() + 1);
                    if (client.getFlagsCapturedThisGame() == 4)
                    {
                        client.addStat("capture-flags-4", 1);
                    }
                    // award the players in zone
                    client.award(NotifyAward.score, a);
                }

                client.notify(NotifyAward.score,
                        shouldAward ? a : 0.0f,
                        NotifyReason.flagTaken, NotifyMethod.message, null);
            }
            else
            {
                if (client.getTeam() == flagData.getTeam())
                {
                    client.notify(NotifyAward.score, 0, NotifyReason.flagLost, NotifyMethod.message, null);
                }
            }
        }

        BrainOut.EventMgr.sendDelayedEvent(BrainOutServer.Controller.getPlayState(),
            FlagTakenEvent.obtain(takingTeam, flagData));
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }
}
