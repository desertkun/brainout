package com.desertkun.brainout.data.effect;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.client.states.ControllerState;
import com.desertkun.brainout.content.effect.ShakeEffect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.ShakeEffectData")
public class ShakeEffectData extends EffectData
{
    public ShakeEffectData(ShakeEffect effect,
       LaunchData launchData)
    {
        super(effect, launchData);
    }

    @Override
    public boolean done()
    {
        return true;
    }

    @Override
    public void init()
    {
        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        ShakeEffect effect = ((ShakeEffect) getEffect());

        float distance = effect.getDistance();
        float power = effect.getPower();

        ControllerState playState = BrainOutClient.ClientController.getState();

        if (playState instanceof CSGame)
        {
            CSGame game = ((CSGame) playState);

            if (game.getPlayerData() != null)
            {
                PlayerData me = game.getPlayerData();

                float d = Vector2.dst(
                    getX(), getY(), me.getX(), me.getY()
                );

                if (d < distance)
                {
                    float p = power * (Interpolation.pow3In.apply(1.0f - d / distance));

                    map.shake(p);
                }
            }
        }
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {

    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public void update(float dt)
    {

    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }
}
