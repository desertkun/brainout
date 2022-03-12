package com.desertkun.brainout.content;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.SkeletonData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.Animation")
public class Animation extends Content
{
    private SkeletonData skeletonData;
    private AnimationStateData stateData;
    private Array<String> skipMix;
    private float timeScale;

    public Animation()
    {
        skeletonData = null;
        stateData = null;
        timeScale = 1;
        skipMix = new Array<>();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        String animationLocation = jsonData.getString("animation");

        if (animationLocation != null && !animationLocation.isEmpty())
        {
            FileHandle file = BrainOut.PackageMgr.getFile(animationLocation);

            try
            {
                skeletonData = BrainOut.JsonSkeleton.readSkeletonData(file);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return;
            }

            skeletonData.setName(file.nameWithoutExtension());
            stateData = new AnimationStateData(skeletonData)
            {
                @Override
                public float getMix(com.esotericsoftware.spine.Animation from, com.esotericsoftware.spine.Animation to)
                {
                    if (skipMix.contains(from.getName(), false))
                        return 0;

                    if (skipMix.contains(to.getName(), false))
                        return 0;

                    return super.getMix(from, to);
                }
            };

            timeScale = jsonData.getFloat("timeScale");
            JsonValue states = jsonData.get("states");
            stateData.setDefaultMix(jsonData.getFloat("defaultMix", 0.0f));

            if (states != null && states.isArray())
            {
                for (JsonValue state: states)
                {
                    String from = state.getString("from");
                    String to = state.getString("to");
                    float duration = state.getFloat("duration");

                    stateData.setMix(from, to, duration);

                    if (state.getBoolean("both", false))
                    {
                        stateData.setMix(to, from, duration);
                    }
                }
            }
        }

        if (jsonData.has("skip-mix"))
        {
            for (JsonValue value : jsonData.get("skip-mix"))
            {
                this.skipMix.add(value.asString());
            }
        }
    }

    public Array<String> getSkipMix()
    {
        return skipMix;
    }

    public SkeletonData getSkeletonData()
    {
        return skeletonData;
    }

    public AnimationStateData getStateData()
    {
        return stateData;
    }

    public float getTimeScale()
    {
        return timeScale;
    }
}
