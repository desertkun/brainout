package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.SpriteWithBlocksComponent;
import com.desertkun.brainout.content.components.UserSpriteWithBlocksComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.components.UserSpriteWithBlocksComponent")
public class UserSpriteWithBlocksComponentData extends Component<UserSpriteWithBlocksComponent>
    implements Json.Serializable, WithTag
{
    private String sprite;
    private int width;
    private int height;

    public UserSpriteWithBlocksComponentData(ComponentObject componentObject,
                                             UserSpriteWithBlocksComponent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public void init()
    {
        super.init();

        ActiveData activeData = ((ActiveData) getComponentObject());

        activeData.setLayer(getLayer());
        activeData.setzIndex(getContentComponent().getzIndex());
    }

    @Override
    public int getLayer()
    {
        UserSpriteWithBlocksComponent sp = getContentComponent();

        switch (sp.getBlocksLayer())
        {
            case Constants.Layers.BLOCK_LAYER_BACKGROUND:
                return Constants.Layers.ACTIVE_LAYER_1;
            case Constants.Layers.BLOCK_LAYER_FOREGROUND:
            default:
                return Constants.Layers.ACTIVE_LAYER_2;
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

                BlockData b = map.getBlock(x, y, getContentComponent().getBlocksLayer());

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

                BlockData b = map.getBlock(x, y, getContentComponent().getBlocksLayer());

                if (b != null)
                    return false;
            }
        }

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

    public void init(String sprite, int width, int height)
    {
        this.sprite = sprite;
        this.width = width;
        this.height = height;
    }

    public String getSprite()
    {
        return sprite;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("sp", sprite);
        json.writeValue("w", width);
        json.writeValue("h", height);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        sprite = jsonData.getString("sp", "");
        width = jsonData.getInt("w", 1);
        height = jsonData.getInt("h", 1);
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.USER_IMAGE);
    }
}
