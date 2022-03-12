package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.LightEntity;
import com.desertkun.brainout.content.active.Light;
import com.desertkun.brainout.content.components.ClientCampFireComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.LightData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.effect.ParticleEffectData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.ClientFreeRealization;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.GameModeFree;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("CampFireComponent")
@ReflectAlias("data.components.CampFireComponentData")
public class ClientCampFireComponentData extends Component<ClientCampFireComponent> implements Json.Serializable
{
    private LightEntity lightEntity;

    private float counter;
    private float light;
    private float target;
    private float pcheck;
    private float burnSpeed;

    private float duration;

    public ClientCampFireComponentData(ComponentObject componentObject,
                                       ClientCampFireComponent contentComponent)
    {
        super(componentObject, contentComponent);

        lightEntity = new LightEntity(contentComponent.getLightEntity());

        light = 0.7f;
        target = 0.7f;
        duration = 60f;
        burnSpeed = 1;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        counter -= dt;
        if (duration > 0)
        {
            duration -= dt * burnSpeed;
        }

        if (counter < 0)
        {
            counter = MathUtils.random(0.15f, 0.25f);

            target = 0.3f + MathUtils.random(0.1f);
        }

        light = MathUtils.lerp(light, target, 0.2f);

        ClientLightComponentData lightc = getComponentObject().getComponent(ClientLightComponentData.class);
        Color c = lightc.getContentComponent().getLightEntity().getColor();

        float duration = Math.min(this.duration, 120f) / 120.f;
        float d = Interpolation.circleIn.apply(duration);

        if (BrainOutClient.ClientSett.isLightsEnabled())
        {
            if (lightc.getLight() != null && lightc.getLight().getLight() != null)
            {
                lightc.getLight().getLight().setColor(c.r, c.g, c.b, 0.45f + light * d);
            }
        }

        pcheck -= dt;
        if (pcheck < 0)
        {
            pcheck = 1.0f;

            ActiveParticleComponentData p = getComponentObject().getComponent(ActiveParticleComponentData.class);
            ActiveData activeData = ((ActiveData) getComponentObject());

            ActiveData w = getMap().getClosestActiveForTag(64, activeData.getX(), activeData.getY(), ActiveData.class,
                Constants.ActiveTags.WIND, activeData1 -> true);

            if (w == null)
            {
                ((ParticleEffectData) p.getParticle()).setHorizontalWind(0);
            }
            else
            {
                float dst = 1.0f - (Math.abs(activeData.getX() - w.getX()) / 64.f);
                float sign = Math.signum(w.getComponent(WindComponentData.class).getMovement()) * 4.0f;
                ((ParticleEffectData) p.getParticle()).setHorizontalWind(
                    Interpolation.circle.apply(dst) * sign
                );
            }

            ((ParticleEffectData) p.getParticle()).setHighEmission(d * 60);
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        duration = jsonData.getFloat("d");
        burnSpeed = jsonData.getFloat("bs");
    }
}
