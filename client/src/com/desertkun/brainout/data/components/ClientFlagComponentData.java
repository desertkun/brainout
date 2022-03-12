package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.content.components.ClientFlagComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.graphics.CenterSprite;
import com.desertkun.brainout.menu.ui.ActiveProgressBar;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientFlagComponent")
@ReflectAlias("data.components.ClientFlagComponentData")
public class ClientFlagComponentData extends Component<ClientFlagComponent>
{
    private final FlagData flagData;
    private ActiveProgressBar progressBar;
    private CenterSprite iconSprite;
    private float counter;

    public ClientFlagComponentData(FlagData flagData, ClientFlagComponent flagComponent)
    {
        super(flagData, flagComponent);

        this.flagData = flagData;
        this.counter = 0;
    }

    @Override
    public void init()
    {
        super.init();

        LaunchData launch = new LaunchData()
        {
            @Override
            public float getX()
            {
                return flagData.getX();
            }

            @Override
            public float getY()
            {
                return flagData.getY() + 0.25f * (float) Math.cos((double) counter * 8f);
            }

            @Override
            public float getAngle()
            {
                return 0;
            }

            @Override
            public String getDimension()
            {
                return flagData.getDimension();
            }

            @Override
            public boolean getFlipX()
            {
                return false;
            }
        };

        this.progressBar = new ActiveProgressBar(launch);
        this.iconSprite = new CenterSprite(getContentComponent().getFlagNoneIcon(), launch);

        BrainOut.EventMgr.subscribe(Event.ID.simple, this);

        ((ActiveData) getComponentObject()).setLayer(2);
    }

    @Override
    public void release()
    {
        super.release();

        if (progressBar != null)
        {
            progressBar.dispose();
        }

        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        iconSprite.draw(batch);

        switch (flagData.getState())
        {
            case taking:
            {
                progressBar.render(batch, context);

                break;
            }
        }
    }

    private float getTakingValue()
    {
        return flagData.getTimeValue();
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case simple:
            {
                if (((SimpleEvent) event).getAction() == SimpleEvent.Action.teamUpdated)
                {
                    updateIcon();
                }
                break;
            }

            case updated:
            {
                updateIcon();

                break;
            }
        }

        return false;
    }

    private void updateIcon()
    {
        CSGame game = BrainOutClient.ClientController.getState(CSGame.class);

        if (flagData.getTeam() == null)
        {
            iconSprite.setRegion(getContentComponent().getFlagNoneIcon());
        }
        else
        if (flagData.getTeam() == game.getTeam())
        {
            iconSprite.setRegion(getContentComponent().getFlagOursIcon());
        }
        else
        {
            iconSprite.setRegion(getContentComponent().getFlagEnemyIcon());
        }
    }

    @Override
    public void update(float dt)
    {
        counter += dt;

        if (flagData.getState() == FlagData.State.taking)
        {
            ClientController CC = BrainOutClient.ClientController;

            if (flagData.getTeam() != null)
            {
                progressBar.setBackgroundColor(CC.getColorOf(flagData.getTeam()));
            }
            else
            {
                progressBar.setBackgroundColor(null);
            }

            if (flagData.getTakingTeam() != null)
            {
                progressBar.setForegroundColor(CC.getColorOf(flagData.getTakingTeam()));
            }
            else
            {
                progressBar.setForegroundColor(Color.DARK_GRAY);
            }

            progressBar.setValue(getTakingValue());
        }
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }
}
