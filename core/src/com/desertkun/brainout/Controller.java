package com.desertkun.brainout;

import com.badlogic.gdx.math.Interpolation;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.components.SlowMoComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateGame;

public abstract class Controller
{
    private final BrainOut brainOut;
    private PlayState playState;

    public Controller(BrainOut brainOut)
    {
        this.brainOut = brainOut;
    }

    public void update(float dt)
    {
        if (playState != null)
        {
            playState.update(dt);
        }
    }

    public PlayState getPlayState()
    {
        return playState;
    }

    public GameMode getGameMode()
    {
        PlayState ps = getPlayState();
        if (ps instanceof PlayStateGame)
        {
            PlayStateGame playStateGame = ((PlayStateGame) ps);
            return playStateGame.getMode();
        }

        return null;
    }

    public PlayState setPlayState(PlayState.ID playState)
    {
        setPlayState(brainOut.newPlayState(playState));

        return this.playState;
    }

    public abstract long getCurrentTime();

    public void setPlayState(PlayState playState)
    {
        if (this.playState != null)
        {
            this.playState.release();
        }

        this.playState = playState;
    }

    public boolean isEnemies(int a, int b)
    {
        PlayState ps = getPlayState();

        if (ps instanceof PlayStateGame)
        {
            PlayStateGame playStateGame = ((PlayStateGame) ps);
            return playStateGame.getMode().isEnemies(a, b);
        }
        else
        {
            return a != b;
        }
    }

    public abstract int getContentIndexFor(Content c);
    public abstract <T extends Content> T getContentFromIndex(int index, Class<T> clazz);

    public boolean isEnemies(Team a, Team b)
    {
        PlayState ps = getPlayState();

        if (ps instanceof PlayStateGame)
        {
            PlayStateGame playStateGame = ((PlayStateGame) ps);
            return playStateGame.getMode().isEnemies(a, b);
        }
        else
        {
            return a != b;
        }
    }

    public void applySlowMo(float time)
    {
        for (Map map : Map.All())
        {
            SlowMoComponent slowMo = map.getComponents().getComponent(SlowMoComponent.class);

            if (slowMo != null)
            {
                slowMo.update(map.getSpeed(), slowMo.getSpeedTo(), time, Interpolation.circleIn);
            }
            else
            {
                map.getComponents().addComponent(new SlowMoComponent(
                        map, 0.25f, map.getSpeed(), time, Interpolation.circleIn
                ));
            }
        }
    }

    public boolean isServer()
    {
        return false;
    }

    public boolean isOnlineEnabled()
    {
        return false;
    }
}
