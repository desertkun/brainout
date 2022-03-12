package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.components.ConditionalSpriteComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ConditionalSpriteComponent")
@ReflectAlias("data.components.ConditionalSpriteComponentData")
public class ConditionalSpriteComponentData extends Component<ConditionalSpriteComponent>
{
    private final Array<ConditionalSpriteComponent.Condition> conditions;
    private final String defaultSprite;
    private com.badlogic.gdx.graphics.g2d.Sprite sprite;
    private Vector2 size;

    public ConditionalSpriteComponentData(ActiveData activeData, ConditionalSpriteComponent spriteComponent)
    {
        super(activeData, spriteComponent);

        this.conditions = spriteComponent.getConditions();
        this.defaultSprite = spriteComponent.getDefaultSprite();
        this.size = new Vector2();
    }

    @Override
    public void init()
    {
        super.init();

        updateSprite();

        BrainOutClient.EventMgr.subscribe(Event.ID.simple, this);
    }

    @Override
    public void release()
    {
        super.release();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.simple, this);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        ActiveData activeData = ((ActiveData) getComponentObject());

        if (sprite != null)
        {
            batch.draw(sprite, activeData.getX(), activeData.getY(), size.x, size.y);
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case simple:
            {
                switch (((SimpleEvent) event).getAction())
                {
                    case userProfileUpdated:
                    {
                        updateSprite();

                        break;
                    }
                }

                break;
            }
        }

        return false;
    }

    public Vector2 getSize()
    {
        return size;
    }

    public void updateSprite()
    {
        sprite = null;

        for (ConditionalSpriteComponent.Condition condition : conditions)
        {
            OwnableContent ownableContent = BrainOut.ContentMgr.get(condition.ownable, OwnableContent.class);
            if (ownableContent == null)
            {
                continue;
            }

            if (BrainOutClient.ClientController.getUserProfile().hasItem(ownableContent))
            {
                setSprint(condition.sprite);

                return;
            }
        }

        setSprint(defaultSprite);
    }

    private void setSprint(String newSprite)
    {
        float spriteScale = 1;

        try
        {
            TextureRegion region = BrainOutClient.getRegion(newSprite);

            if (region == null)
            {
                this.sprite = null;
                return;
            }

            this.sprite = new com.badlogic.gdx.graphics.g2d.Sprite(region);
        }
        catch (Exception e)
        {
            e.printStackTrace();

            this.sprite = null;
        }

        if (sprite != null)
        {
            this.size.set(sprite.getRegionWidth() * spriteScale / Constants.Graphics.RES_SIZE,
                sprite.getRegionHeight() * spriteScale / Constants.Graphics.RES_SIZE);
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
        return false;
    }
}
