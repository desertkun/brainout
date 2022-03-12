package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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

@Reflect("content.components.AnimatedIconComponent")
public class AnimatedIconComponent extends ContentComponent
{
    private class Frame implements Json.Serializable
    {
        private String frameName;
        private float delay;

        public Frame()
        {
        }

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            frameName = jsonData.getString("frame");
            delay = jsonData.getFloat("delay");
        }

        public String getFrameName()
        {
            return frameName;
        }

        public float getDelay()
        {
            return delay;
        }
    }

    private Array<Frame> frames;

    public AnimatedIconComponent()
    {
        frames = new Array<>();
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

    public void setupImage(Image image)
    {
        SequenceAction seq = new SequenceAction();

        for (Frame frame : frames)
        {
            seq.addAction(Actions.run(() -> {
                try
                {
                    image.setDrawable(BrainOutClient.Skin, frame.getFrameName());
                }
                catch (IllegalArgumentException ignored)
                {
                    //
                }
            }));
            seq.addAction(Actions.delay(frame.getDelay()));
        }

        image.addAction(Actions.repeat(RepeatAction.FOREVER, seq));
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        JsonValue frames = jsonData.get("frames");

        for (JsonValue frame : frames)
        {
            Frame f = new Frame();
            f.read(json, frame);
            this.frames.add(f);
        }
    }
}
