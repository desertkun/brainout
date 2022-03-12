package com.desertkun.brainout.data;

import com.badlogic.gdx.utils.Json;
import com.desertkun.brainout.content.active.RealEstateItem;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.components.SpriteWithBlocksComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.RealEstateItemComponentData;
import com.desertkun.brainout.data.components.SpriteBlockComponentData;
import com.desertkun.brainout.data.components.SpriteWithBlocksComponentData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.RealEstateInfo;
import org.json.JSONObject;

@Reflect("data.ServerFreeplayMap")
public class ServerFreeplayMap extends ServerMap
{
    private RealEstateInfo realEstateInfo = null;

    public void setRealEstateInfo(RealEstateInfo realEstateInfo)
    {
        this.realEstateInfo = realEstateInfo;
    }

    public RealEstateInfo getRealEstateInfo()
    {
        return realEstateInfo;
    }

    public ServerFreeplayMap(String dimension, int width, int height)
    {
        super(dimension, width, height);
    }

    public ServerFreeplayMap(String dimension, int width, int height, boolean init)
    {
        super(dimension, width, height, init);
    }

    public ServerFreeplayMap(String dimension)
    {
        super(dimension);
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        if (realEstateInfo != null)
        {
            json.writeObjectStart("rs");
            realEstateInfo.write(json);
            json.writeObjectEnd();
        }
    }

    public void placeRealEstateObject(String itemKey, int x, int y, RealEstateItem content)
    {
        if (content.hasComponent(SpriteWithBlocksComponent.class))
        {
            SpriteWithBlocksComponent sp = content.getComponent(SpriteWithBlocksComponent.class);

            if (sp.validateBlocksForAdding(this, x, y))
            {
                ActiveData activeData = content.getData(getDimension());

                {
                    RealEstateItemComponentData rsItem = new RealEstateItemComponentData(itemKey);
                    activeData.addComponent(rsItem);
                    rsItem.init();
                }

                activeData.setLayer(sp.getBlocksLayer());
                activeData.setzIndex(sp.getzIndex());
                activeData.setPosition(x, y);

                addActive(generateServerId(), activeData, true, true, false);

                for (int j = 0; j < sp.getHeight(); j++)
                {
                    for (int i = 0; i < sp.getWidth(); i++)
                    {
                        int x_ = x + i, y_ = y + j;

                        BlockData b;

                        if (sp.hasOnlyOneUnderlyingBlock())
                        {
                            Block underlyingBlock = sp.getUnderlyingBlock();
                            b = underlyingBlock.getBlock();
                        }
                        else
                        {
                            Block underlyingBlock = sp.getUnderlyingBlockAt(i, j);
                            b = underlyingBlock.getBlock();
                        }

                        SpriteBlockComponentData sbc = b.getComponent(SpriteBlockComponentData.class);

                        if (sbc != null)
                        {
                            sbc.setSprite(activeData);
                        }

                        setBlock(x_, y_, b, sp.getBlocksLayer(), false, false);
                    }
                }
            }
        }
    }

    public void removeRealEstateObject(ActiveData activeData)
    {
        if (activeData == null)
            return;

        SpriteWithBlocksComponentData spi = activeData.getComponent(SpriteWithBlocksComponentData.class);

        if (spi != null)
        {
            SpriteWithBlocksComponent sp = spi.getContentComponent();

            int x_ = (int)activeData.getX(), y_ = (int)activeData.getY();

            for (int j = 0; j < sp.getHeight(); j++)
            {
                for (int i = 0; i < sp.getWidth(); i++)
                {
                    int x = x_ + i, y = y_ + j;

                    setBlock(x, y, null, sp.getBlocksLayer(), false, false);
                }
            }

            removeActive(activeData, true);
        }
    }
}
