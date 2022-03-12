package com.desertkun.brainout.content.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.ContentLockTree;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.InstrumentPartialIconComponent")
public class InstrumentPartialIconComponent extends ContentComponent
{
    private InstrumentSlotItem content;
    private Array<String> images;

    public InstrumentPartialIconComponent()
    {
        images = new Array<>();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        content = BrainOutClient.ContentMgr.get(jsonData.getString("content"), InstrumentSlotItem.class);

        JsonValue images = jsonData.get("images");
        for (JsonValue image : images)
        {
            this.images.add(image.asString());
        }
    }

    @Override
    public Content getContent()
    {
        return content;
    }

    public void renderImage(Table renderTo)
    {
        ContentLockTree.LockItem lockItem = content.getLockItem();

        if (lockItem == null)
            return;

        int alreadyHave = MathUtils.clamp(
            (int)(float)BrainOutClient.ClientController.getUserProfile().getStats().get(
                lockItem.getUnlockFor(), 0.0f),
            0, lockItem.getParam() - 1
        );

        String image = images.get(alreadyHave);

        Image iconImage = new Image(BrainOutClient.Skin, image);
        iconImage.setScaling(Scaling.none);

        renderTo.add(iconImage).expand().fill().row();
    }
}
