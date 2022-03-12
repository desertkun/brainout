package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.upgrades.DoNotApply;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.SelectableItem;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.IconComponent")
public class IconComponent extends ContentComponent implements SelectableItem, DoNotApply
{
    private ObjectMap<String, TextureAtlas.AtlasRegion> icons;
    private ObjectMap<String, String> iconsToLoad;

    public static String DEFAULT_ICON = "icon";

    public IconComponent()
    {
        iconsToLoad = new ObjectMap<>();
        icons = new ObjectMap<>();
    }

    public TextureAtlas.AtlasRegion getIcon()
    {
        return getIcon(DEFAULT_ICON);
    }

    public TextureAtlas.AtlasRegion getIcon(String id)
    {
        return getIcon(id, icons.get(DEFAULT_ICON));
    }

    public String getIconName(String id, String def)
    {
        return iconsToLoad.get(id, def);
    }

    public String getIconName()
    {
        return getIconName(DEFAULT_ICON, "");
    }

    public TextureAtlas.AtlasRegion getIcon(String id, TextureAtlas.AtlasRegion fallback)
    {
        return icons.get(id, fallback);
    }

    public boolean hasIcon(String id)
    {
        return icons.containsKey(id);
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public String getItemName()
    {
        return getContent().getTitle().get();
    }

    @Override
    public TextureAtlas.AtlasRegion getItemIcon()
    {
        return getIcon();
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.isObject())
        {
            for (JsonValue v: jsonData)
            {
                String name = v.name();

                if (!name.equals("class") && !name.equals("tag"))
                {
                    iconsToLoad.put(name, v.asString());
                }
            }
        }
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        if (iconsToLoad != null)
        {
            for (ObjectMap.Entry<String, String> entry: iconsToLoad)
            {
                TextureAtlas.AtlasRegion region = BrainOutClient.getRegion(entry.value);

                if (region == null)
                {
                    System.err.println("Cannot find icon " + entry.value +
                        " for " + getContent().getID() + "/" + entry.key);
                    continue;
                }

                icons.put(entry.key, region);
            }
        }
    }
}
