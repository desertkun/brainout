package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.RevSoundEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.RandomValue;

@Reflect("content.effect.RevSoundEffect")
public class RevSoundEffect extends Effect
{
    private Sound sound, reverbSound;
    private String fileName;
    private boolean distantDelay;
    private RandomValue pitch;
    private boolean loop;
    private float soundDistance;
    private String reverbFileName;

    public RevSoundEffect()
    {
        this.distantDelay = false;
        this.loop = false;
    }

    @Override
    public EffectData getEffect(LaunchData launchData)
    {
        return new RevSoundEffectData(this, launchData);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        fileName = jsonData.getString("sound");
        reverbFileName = getReverbFilename(fileName);
        loop = jsonData.getBoolean("loop", false);
        soundDistance = jsonData.getFloat("distance", 1.0f);

        if (jsonData.has("pitch"))
        {
            this.pitch = new RandomValue(1.0f, 1.0f);
            pitch.read(json, jsonData.get("pitch"));
        }

        distantDelay = jsonData.getBoolean("distantDelay", false);
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        sound = assetManager.get(fileName, Sound.class);
        reverbSound = assetManager.get(reverbFileName, Sound.class);
    }

    private String getReverbFilename(String fileName)
    {
        String[] split = fileName.split(":");
        String package_ = split[0];
        String path = split[1];

        String[] folders = path.split("/");
        String[] resFolders = new String[folders.length + 1];

        if (folders.length - 2 >= 0)
            System.arraycopy(folders, 0, resFolders, 0, folders.length - 1);

        resFolders[folders.length - 1] = "reverb";
        resFolders[folders.length] = folders[folders.length - 1];

        return package_ + ":" + String.join("/", resFolders);
    }

    @Override
    public void loadContent(AssetManager assetManager)
    {
        super.loadContent(assetManager);

        assetManager.load(fileName, Sound.class);

        try
        {
            assetManager.load(reverbFileName, Sound.class);
        }
        catch (Exception e)
        {
            // ignored
        }
    }

    public Sound getSound()
    {
        return sound;
    }

    public Sound getReverbSound() {
        return reverbSound;
    }

    public boolean isDistantDelay()
    {
        return distantDelay;
    }

    public RandomValue getPitch()
    {
        return pitch;
    }

    public boolean isLoop()
    {
        return loop;
    }

    public float getSoundDistance()
    {
        return soundDistance;
    }
}
