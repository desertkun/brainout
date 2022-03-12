package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.components.InstrumentParticleComponent;
import com.desertkun.brainout.content.components.SkinParticleComponent;
import com.desertkun.brainout.content.effect.ParticleEffect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.ParticleEffectData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.BonePointData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.spine.Bone;

@Reflect("InstrumentParticleComponent")
@ReflectAlias("data.components.InstrumentParticleComponentData")
public class InstrumentParticleComponentData extends Component<InstrumentParticleComponent>
{
    private final InstrumentData instrumentData;
    private BonePointData bonePoint;

    private ParticleEffectData particle;
    private ParticleEffect particleEffect;

    private String initialDimension;

    public InstrumentParticleComponentData(InstrumentData instrumentData, InstrumentParticleComponent particleComponent)
    {
        super(instrumentData, particleComponent);

        this.instrumentData = instrumentData;
    }

    public EffectData getParticle()
    {
        return particle;
    }

    @Override
    public void init()
    {
        super.init();
    }

    @Override
    public void release()
    {
        super.release();
        removeParticle();
    }

    @Override
    public void updated(ActiveData owner) {
        super.updated(owner);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID()) {

            case setInstrument: {

                if (particle == null)
                    updateParticles();

                break;
            }

            case simple:
            {
                SimpleEvent e = ((SimpleEvent) event);
                if (e.getAction() == SimpleEvent.Action.deselected)
                {
                    removeParticle();
                }

                break;
            }
        }

        return false;
    }

    private void removeParticle()
    {
        if (particle != null)
        {
            ClientMap map = instrumentData.getOwner().getMap(ClientMap.class);
            particle.release();
            map.removeEffect(particle);
            particle = null;
        }
    }

    private void createParticleEffect()
    {
        Skin skin = instrumentData.getInfo().skin;

        if (skin != null && skin.hasComponent(SkinParticleComponent.class))
        {
            SkinParticleComponent spc = skin.getComponent(SkinParticleComponent.class);
            particleEffect = spc.getParticleEffect();
        }
    }

    private void createBonePoint()
    {
        WeaponAnimationComponentData cwp = instrumentData.getComponent(WeaponAnimationComponentData.class);
        Bone bone = cwp.getSkeleton().findBone("laser-bone");

        if (bone == null) return;

        PlayerAnimationComponentData playerAnimation = instrumentData.getOwner().getComponent(PlayerAnimationComponentData.class);

        if (playerAnimation == null) return;

        bonePoint = new BonePointData(bone, playerAnimation.getPrimaryBonePointData());
    }

    private void updateParticles()
    {
        if (particleEffect == null)
            createParticleEffect();

        if (particleEffect != null && particleEffect.isEnabled())
        {
            ClientMap map = instrumentData.getOwner().getMap(ClientMap.class);
            if (map == null)
                return;

            if (particle != null)
            {
                map.removeEffect(particle);
                particle = null;
            }

            if (bonePoint == null)
                createBonePoint();

            initialDimension = map.getDimension();
            particle = (ParticleEffectData) map.addEffect(particleEffect, new LaunchData()
            {
                @Override
                public float getX()
                {
                    float x = 0;
                    if (bonePoint != null)
                        x = bonePoint.getX();

                    return x;
                }

                @Override
                public float getY()
                {
                    float y = 0;
                    if (bonePoint != null)
                        y = bonePoint.getY();
                    return y;
                }

                @Override
                public float getAngle()
                {
                    return 0;
                }

                @Override
                public String getDimension()
                {
                    return initialDimension;
                }

                @Override
                public boolean getFlipX()
                {
                    return false;
                }
            });
        }
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if (particle == null)
            return;

        WeaponAnimationComponentData wac = instrumentData.getComponent(WeaponAnimationComponentData.class);

        if (wac != null)
        {
            float offset = wac.getAngle();
            if (wac.getFlipX())
                offset = -offset;
            particle.setAngleOffset(offset);
        }

        if ( ! instrumentData.getDimension().equals(initialDimension))
            updateParticles();
    }
}
