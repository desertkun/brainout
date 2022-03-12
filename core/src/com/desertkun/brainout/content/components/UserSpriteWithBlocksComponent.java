package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.SpriteBlockComponentData;
import com.desertkun.brainout.data.components.SpriteWithBlocksComponentData;
import com.desertkun.brainout.data.components.UserSpriteWithBlocksComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.UserSpriteWithBlocksComponent")
public class UserSpriteWithBlocksComponent extends ContentComponent
{
    private String extName;
    private Block underlyingBlock;
    private int blocksLayer;
    private int zIndex;

    public UserSpriteWithBlocksComponent()
    {
    }

    @Override
    public UserSpriteWithBlocksComponentData getComponent(ComponentObject componentObject)
    {
        return new UserSpriteWithBlocksComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    public boolean validateBlocksForAdding(
            Map map, int atX, int atY, int width, int height, ObjectSet<ActiveData> ignore)
    {
        if (atX < 0 || atY < 0 || atX > map.getWidth() - width || atY > map.getHeight() - height)
            return false;

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                int x = atX + i, y = atY + j;

                BlockData b = map.getBlock(x, y, getBlocksLayer());

                if (b != null)
                {
                    SpriteBlockComponentData sbc = b.getComponent(SpriteBlockComponentData.class);

                    if (sbc != null)
                    {
                        ActiveData sp = sbc.getSprite(map);

                        if (sp == null)
                            return false;

                        if (ignore != null && ignore.contains(sp))
                        {
                            continue;
                        }

                        return false;
                    }
                    else
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }


    public boolean validateBlocksForAdding(Map map, int atX, int atY, int width, int height)
    {
        if (atX < 0 || atY < 0 || atX > map.getWidth() - width || atY > map.getHeight() - height)
            return false;

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                int x = atX + i, y = atY + j;

                BlockData b = map.getBlock(x, y, getBlocksLayer());

                if (b != null)
                    return false;
            }
        }

        return true;
    }


    @Override
    public void read(Json json, JsonValue jsonData)
    {
        extName = jsonData.getString("ext-name");
        underlyingBlock = BrainOut.ContentMgr.get(jsonData.getString("underlying-block"), Block.class);

        blocksLayer = jsonData.getInt("blocks-layer");
        zIndex = jsonData.getInt("zIndex", 0);
    }

    public String getExtName()
    {
        return extName;
    }

    public Block getUnderlyingBlock()
    {
        return underlyingBlock;
    }

    public int getBlocksLayer()
    {
        return blocksLayer;
    }

    public int getzIndex()
    {
        return zIndex;
    }
}
