package com.desertkun.brainout.data.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.SpriteWithBlocksComponent;
import com.desertkun.brainout.content.components.ClientSpriteBlockComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.Cacheble;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.graphics.CenterSprite;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientSpriteBlockComponent")
@ReflectAlias("data.components.ClientSpriteBlockComponentData")
public class ClientSpriteBlockComponentData extends
        Component<ClientSpriteBlockComponent> implements Cacheble
{
    public ClientSpriteBlockComponentData(
        ComponentObject componentObject,
        ClientSpriteBlockComponent contentComponent)
    {
        super(componentObject, contentComponent);
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
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public void cache(Map map, SpriteCache cache)
    {
        ActiveData activeData = ((ActiveData) getComponentObject());
        SpriteWithBlocksComponentData spi = activeData.getComponent(SpriteWithBlocksComponentData.class);

        if (spi != null)
        {

            SpriteWithBlocksComponent sp = spi.getContentComponent();

            for (SpriteWithBlocksComponent.SpriteImage spriteImage : sp.getImages())
            {
                TextureRegion region = BrainOutClient.getRegion(spriteImage.getImage());

                if (region == null)
                    continue;

                float x = activeData.getX() + spriteImage.getX(),
                        y = activeData.getY() + spriteImage.getY();

                cache.add(region, x, y, spriteImage.getW(), spriteImage.getH());
            }
        }
        else
        {
            UserSpriteWithBlocksComponentData us = activeData.getComponent(UserSpriteWithBlocksComponentData.class);

            if (us == null)
                return;
            
            TextureRegion region = BrainOutClient.getRegion(us.getSprite());

            if (region == null)
            {
                if (us.getSprite().endsWith(".png"))
                {
                    region = BrainOutClient.getRegion(us.getSprite().replace(".png", ""));
                    if (region == null)
                    {
                        return;
                    }
                }
            }

            float x = activeData.getX() + (region.getRegionWidth() % Constants.Graphics.BLOCK_SIZE) / (Constants.Graphics.BLOCK_SIZE * 2f),
                  y = activeData.getY() + (region.getRegionHeight() % Constants.Graphics.BLOCK_SIZE) / (Constants.Graphics.BLOCK_SIZE * 2f);

            cache.add(region, x, y, (float)region.getRegionWidth() / Constants.Graphics.BLOCK_SIZE,
                (float)region.getRegionHeight() / Constants.Graphics.BLOCK_SIZE);
        }
    }

    /*
    @Override
    public void release()
    {
        super.release();

        SpriteWithBlocksComponentData spi = (SpriteWithBlocksComponentData) getComponentObject();

        ClientActiveDataMap.ClientRenderMap renderMap =
            ((ClientActiveDataMap.ClientRenderMap) getMap().getActives().getRenderLayer(spi.getLayer()));

        //renderMap.getCache().setDirty();
    }
    */

    @Override
    public boolean hasCache()
    {
        return true;
    }
}
