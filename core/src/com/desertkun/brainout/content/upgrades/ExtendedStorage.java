package com.desertkun.brainout.content.upgrades;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

import java.util.Comparator;

@Reflect("content.upgrades.ExtendedStorage")
public class ExtendedStorage extends OwnableContent
{
    private int extraWeight;
    private String category;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        category = jsonData.getString("category");
        if (jsonData.has("extra-weight"))
        {
            extraWeight = jsonData.getInt("extra-weight");
        }
    }

    public String getCategory()
    {
        return category;
    }

    public static ExtendedStorage HasRoomToExtend(UserProfile userProfile)
    {
        Array<ExtendedStorage> nonUnlockedContent =
            BrainOut.ContentMgr.queryContent(ExtendedStorage.class,
                check -> !userProfile.hasItem(check));

        if (nonUnlockedContent.isEmpty())
        {
            return null;
        }

        nonUnlockedContent.sort(Comparator.comparingInt(ExtendedStorage::getExtraWeight));

        return nonUnlockedContent.first();
    }

    public int getExtraWeight()
    {
        return extraWeight;
    }
}
