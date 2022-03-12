package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.content.components.ClientChipReceiverComponent;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.graphics.CenterSprite;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientChipReceiverComponent")
@ReflectAlias("data.components.ClientChipReceiverComponentData")
public class ClientChipReceiverComponentData extends Component<ClientChipReceiverComponent> implements WithTag
{
    private final ActiveData activeData;
    private CenterSprite iconSprite;
    private float counter;
    private TextureRegion region;

    public ClientChipReceiverComponentData(ActiveData activeData,
        ClientChipReceiverComponent chipReceiverComponent)
    {
        super(activeData, chipReceiverComponent);

        this.activeData = activeData;
        this.counter = 0;
    }

    @Override
    public void init()
    {
        super.init();

        IconComponent icon = activeData.getContent().getComponent(IconComponent.class);

        region = icon.getIcon("in-game");

        this.iconSprite = new CenterSprite(region, new LaunchData()
        {
            @Override
            public float getX()
            {
                return activeData.getX();
            }

            @Override
            public float getY()
            {
                return activeData.getY() + 0.25f * (float)Math.cos((double)counter * 8f);
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
        });

        BrainOut.EventMgr.subscribe(Event.ID.simple, this);
    }

    @Override
    public void release()
    {
        super.release();

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

        if (activeData.getTeam() == game.getTeam())
        {
            iconSprite.setColor(ClientConstants.Menu.KillList.FRIEND_COLOR);
        }
        else
        {
            iconSprite.setColor(ClientConstants.Menu.KillList.ENEMY_COLOR);
        }
    }

    @Override
    public void update(float dt)
    {
        counter += dt;
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

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.CHIP_RECEIVER);
    }
}
