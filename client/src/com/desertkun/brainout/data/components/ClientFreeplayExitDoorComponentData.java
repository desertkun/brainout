package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.ClientFreeplayExitDoorComponent;
import com.desertkun.brainout.data.active.FreeplayExitDoorData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.graphics.CenterSprite;
import com.desertkun.brainout.menu.ui.ActiveProgressBar;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientFreeplayExitDoorComponent")
@ReflectAlias("data.components.ClientFreeplayExitDoorComponentData")
public class ClientFreeplayExitDoorComponentData extends Component<ClientFreeplayExitDoorComponent>
{
    private final FreeplayExitDoorData activeData;

    private ActiveProgressBar progressBar;
    private CenterSprite iconSprite;
    private float counter;

    public ClientFreeplayExitDoorComponentData(FreeplayExitDoorData activeData, ClientFreeplayExitDoorComponent flagComponent)
    {
        super(activeData, flagComponent);

        this.activeData = activeData;
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
                return activeData.getX();
            }

            @Override
            public float getY()
            {
                return activeData.getY() + 0.25f * (float) Math.cos((double) counter * 8f);
            }

            @Override
            public float getAngle()
            {
                return 0;
            }

            @Override
            public String getDimension()
            {
                return activeData.getDimension();
            }

            @Override
            public boolean getFlipX()
            {
                return false;
            }
        };

        this.progressBar = new ActiveProgressBar(launch);
        this.iconSprite = new CenterSprite(getContentComponent().getIcon(), launch);

        BrainOut.EventMgr.subscribe(Event.ID.simple, this);
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
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case updated:
            {
                //

                break;
            }
        }

        return false;
    }

    @Override
    public void update(float dt)
    {
        counter += dt;


        /*
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
        */
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
