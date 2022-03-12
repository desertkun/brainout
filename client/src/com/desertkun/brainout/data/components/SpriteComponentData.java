package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.SpriteComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.SpriteData;
import com.desertkun.brainout.data.interfaces.Cacheble;
import com.desertkun.brainout.events.ActiveActionEvent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("SpriteComponent")
@ReflectAlias("data.components.SpriteComponentData")
public class SpriteComponentData extends Component<SpriteComponent> implements Cacheble
{
    private final SpriteData spriteData;
    private com.badlogic.gdx.graphics.g2d.Sprite sprite;
    private Vector2 size;

    public SpriteComponentData(SpriteData spriteData, SpriteComponent spriteComponent)
    {
        super(spriteData, spriteComponent);

        this.spriteData = spriteData;

        this.size = new Vector2();
    }

    @Override
    public void init()
    {
        super.init();

        updateSprite();
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        if (sprite != null)
        {
            batch.draw(sprite, spriteData.getX(), spriteData.getY(),
                size.x, size.y);
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case activeAction:
            {
                switch (((ActiveActionEvent) event).action)
                {
                    case updated:
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
        String spriteName = spriteData.getSpriteName();
        float spriteScale = spriteData.getScale();

        try
        {
            TextureRegion region = BrainOutClient.getRegion(spriteName);

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
            sprite.setFlip(spriteData.isFlipX(), spriteData.isFlipY());
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

    @Override
    public void cache(Map map, SpriteCache cache)
    {
        if (sprite != null)
        {
            cache.add(sprite, spriteData.getX(), spriteData.getY(), size.x, size.y);
        }
    }

    @Override
    public boolean hasCache()
    {
        return spriteData.isCache();
    }
}
