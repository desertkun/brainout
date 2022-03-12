package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.SpriteWithBlocksComponentData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.SpriteBlockComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.minlog.Log;

@Reflect("content.components.SpriteWithBlocksComponent")
public class SpriteWithBlocksComponent extends ContentComponent
{
    private int width;
    private int height;
    private Queue<SpriteImage> images;
    private Block underlyingBlock;
    private Array<Block> underlyingBlockMap;
    private int blocksLayer;
    private int zIndex;
    private int limitAmount;

    public SpriteWithBlocksComponent()
    {
        images = new Queue<>();
    }

    @Override
    public SpriteWithBlocksComponentData getComponent(ComponentObject componentObject)
    {
        return new SpriteWithBlocksComponentData(componentObject, this);
    }

    public class SpriteImage implements Json.Serializable
    {
        private float x;
        private float y;
        private float w;
        private float h;
        private String image;

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            x = jsonData.getFloat("x");
            y = jsonData.getFloat("y");
            w = jsonData.getFloat("w");
            h = jsonData.getFloat("h");
            image = jsonData.getString("image");
        }

        public float getX()
        {
            return x;
        }

        public float getY()
        {
            return y;
        }

        public float getW()
        {
            return w;
        }

        public float getH()
        {
            return h;
        }

        public String getImage()
        {
            return image;
        }
    }

    public boolean validateBlocksForAdding(Map map, int atX, int atY, ObjectSet<ActiveData> ignore)
    {
        if (atX < 0 || atY < 0 || atX > map.getWidth() - getWidth() || atY > map.getHeight() - getHeight())
            return false;


        for (int j = 0; j < getHeight(); j++)
        {
            for (int i = 0; i < getWidth(); i++)
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


    public boolean validateBlocksForAdding(Map map, int atX, int atY)
    {
        if (atX < 0 || atY < 0 || atX > map.getWidth() - getWidth() || atY > map.getHeight() - getHeight())
            return false;


        for (int j = 0; j < getHeight(); j++)
        {
            for (int i = 0; i < getWidth(); i++)
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
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        width = jsonData.getInt("width");
        height = jsonData.getInt("height");
        limitAmount = jsonData.getInt("limit-amount", 0);

        if (jsonData.has("underlying-block"))
        {
            underlyingBlock = BrainOut.ContentMgr.get(jsonData.getString("underlying-block"), Block.class);
            if (underlyingBlock == null)
            {
                if (Log.ERROR) Log.error("No underlying block: " + jsonData.getString("underlying-block"));
            }
        }
        else
        {
            underlyingBlockMap = new Array<>(width * height);

            for (JsonValue value : jsonData.get("underlying-map"))
            {
                Block block = BrainOut.ContentMgr.get(value.asString(), Block.class);;

                if (block == null)
                    throw new RuntimeException("Bad underlying-map block: " + value.asString());

                underlyingBlockMap.add(block);
            }
        }

        blocksLayer = jsonData.getInt("blocks-layer");
        zIndex = jsonData.getInt("zIndex", 0);

        for (JsonValue value : jsonData.get("images"))
        {
            SpriteImage image = new SpriteImage();
            image.read(json, value);
            images.addLast(image);
        }
    }

    public int getLimitAmount()
    {
        return limitAmount;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public Queue<SpriteImage> getImages()
    {
        return images;
    }

    public boolean hasOnlyOneUnderlyingBlock()
    {
        return underlyingBlock != null;
    }

    public Block getUnderlyingBlock()
    {
        return underlyingBlock;
    }

    public Block getUnderlyingBlockAt(int x, int y)
    {
        int i = x + y * width;
        return underlyingBlockMap.get(i);
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
