package com.desertkun.brainout.data.components;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.LaserComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.data.interfaces.*;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.graphics.CenterSprite;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.spine.Bone;

@Reflect("LaserComponent")
@ReflectAlias("data.components.LaserComponentData")
public class LaserComponentData extends Component<LaserComponent>
{
    private final Vector2 pointPos, prevPointPos, tmp;
    private boolean enabled;
    private final WeaponData weaponData;

    private PlayerData playerData;
    private BonePointData laserPoint;

    private CenterSprite laserSprite;
    private CenterSprite pointA;
    private PointLight light;
    private boolean active;
    private String generatedDimension = "";

    private boolean got;
    private float updateTime;
    private float pointCounter;

    private Color startColor, endColor;

    private static Color START_COLOR = new Color(1, 0, 0, ClientConstants.Components.Laser.ALPHA);

    private static Vector2 TMP = new Vector2();
    private static Vector2 TMP2 = new Vector2();

    public LaserComponentData(WeaponData weaponData, LaserComponent laserComponent)
    {
        super(weaponData, laserComponent);

        this.weaponData = weaponData;
        this.pointPos = new Vector2();
        this.prevPointPos = new Vector2();
        this.tmp = new Vector2();
        this.got = false;
        this.updateTime = 0;
        this.endColor = new Color(1, 0, 0, 0);
        this.startColor = new Color(START_COLOR);
        this.pointCounter = 0;
        this.enabled = false;
    }

    @Override
    public void init()
    {
        super.init();

        this.playerData = ((PlayerData) weaponData.getOwner());

        WeaponAnimationComponentData cwp = getComponentObject().getComponent(WeaponAnimationComponentData.class);
        Bone bone = cwp.getSkeleton().findBone("laser-bone");

        if (bone == null) return;

        if (weaponData.getOwner() == null) return;

        PlayerAnimationComponentData playerAnimation = weaponData.getOwner().getComponent(PlayerAnimationComponentData.class);

        if (playerAnimation == null) return;

        laserPoint = new BonePointData(bone, playerAnimation.getPrimaryBonePointData());

        this.laserSprite = new CenterSprite(BrainOutClient.getRegion("laser-point"), new LaunchData()
        {
            @Override
            public float getX()
            {
                return (pointPos.x + prevPointPos.x) / 2.0f;
            }

            @Override
            public float getY()
            {
                return (pointPos.y + prevPointPos.y) / 2.0f;
            }

            @Override
            public float getAngle()
            {
                return tmp.set(pointPos).sub(prevPointPos).angleDeg();
            }

            @Override
            public String getDimension()
            {
                return playerData.getDimension();
            }

            @Override
            public boolean getFlipX()
            {
                return false;
            }
        });

        this.pointA = new CenterSprite(BrainOutClient.getRegion("laser-point-edge"), new LaunchData()
        {
            @Override
            public float getX()
            {
                return pointPos.x;
            }

            @Override
            public float getY()
            {
                return pointPos.y;
            }

            @Override
            public float getAngle()
            {
                return 0;
            }

            @Override
            public String getDimension()
            {
                return playerData.getDimension();
            }

            @Override
            public boolean getFlipX()
            {
                return false;
            }
        });
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);
        updateLight();

        if (laserSprite == null) return;

        updateTime -= dt;
        pointCounter += dt;

        laserSprite.setScale(1.5f + (float) Math.cos(pointCounter * 4f) * 0.5f);

        if (updateTime < 0 && laserPoint != null)
        {
            updateTime = ClientConstants.Components.Laser.UPDATE_TIME;

            ClientMap map = ((ClientMap) getMap());

            float laserAngle = laserPoint.getAngle();

            got = map.trace(laserPoint.getX(), laserPoint.getY(), Constants.Layers.BLOCK_LAYER_FOREGROUND,
                    laserAngle,
                ClientConstants.Components.Laser.DISTANCE, pointPos);

            float d1 = pointPos.dst(laserPoint.getX(), laserPoint.getY());
            float d2 = prevPointPos.dst(laserPoint.getX(), laserPoint.getY());

            if (prevPointPos.dst2(pointPos) > 4.0f * 4.0f)
            {
                prevPointPos.set(pointPos);
            }

            float distance = MathUtils.clamp(pointPos.dst(laserPoint.getX(), laserPoint.getY()) / ClientConstants.Components.Laser.DISTANCE, 0f, 1f);
            endColor.a = ClientConstants.Components.Laser.ALPHA * (1f - distance);
            startColor.a = ClientConstants.Components.Laser.ALPHA;

            float mult = 1.0f;

            mult *= 0.25f * ((float)Math.cos(pointCounter * 40f)) + 0.75f;
            mult *= 0.25f * ((float)Math.cos(pointCounter * 10f)) + 0.75f;
            mult *= 0.25f * ((float)Math.cos(pointCounter * 4f)) + 0.75f;

            Watcher watcher = Map.GetWatcher();

            if (watcher != null && watcher.getDimension().equals(playerData.getDimension()))
            {
                float alpha = 0;

                TMP.set(watcher.getWatchX() - playerData.getX(), watcher.getWatchY() - playerData.getY());

                TMP2.set(1, 0);
                TMP2.setAngle(laserAngle);

                float angleBetween = Math.abs(TMP.angle(TMP2));

                if (angleBetween <= ClientConstants.Components.Laser.SEE_ANGLE ||
                    angleBetween >= 180 - ClientConstants.Components.Laser.SEE_ANGLE)
                {
                    float m;
                    if (angleBetween <= ClientConstants.Components.Laser.SEE_ANGLE)
                    {
                        m = 1 - angleBetween / ClientConstants.Components.Laser.SEE_ANGLE;
                    }
                    else
                    {
                        m = 1 - (180 - angleBetween) / ClientConstants.Components.Laser.SEE_ANGLE;
                    }

                    alpha = MathUtils.clamp(m * 2, 0f, 1f);
                }

                startColor.a *= alpha * mult;
                endColor.a *= alpha * mult;
            }
        }
    }

    private boolean canRenderLights()
    {
        return BrainOutClient.ClientSett.isLightsEnabled();
    }

    private void updateLight()
    {
        if (!canRenderLights())
        {
            return;
        }

        if (Map.GetWatcher() == null)
            return;

        boolean generated = light != null;

        if (generated)
        {
            if (!generatedDimension.equals(Map.GetWatcher().getDimension()))
            {
                free();
                generate();
            }
            updateTransform();
        }

        boolean isVisible = weaponData.getOwner() != null && weaponData.getOwner().isVisible();
        boolean should = got && enabled && isVisible;

        if (should != generated)
        {
            if (should)
            {
                generate();
            }
            else
            {
                free();
            }
        }
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        if (!enabled)
            return;

        if (laserSprite == null) return;

        if (!playerData.isVisible())
        {
            return;
        }

        if (BrainOutClient.ShapeRenderer != null)
        {
            batch.end();

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

            try
            {
                BrainOutClient.ShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                BrainOutClient.ShapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

                BrainOutClient.ShapeRenderer.line(laserPoint.getX(), laserPoint.getY(), pointPos.x, pointPos.y,
                    startColor, endColor);

                BrainOutClient.ShapeRenderer.end();
            }
            catch (IllegalStateException ignore)
            {
                //
            }

            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            batch.begin();

            if (got)
            {
                drawLaserPoint(batch);
            }
        }
    }

    private void drawLaserPoint(Batch batch)
    {
        float l = prevPointPos.dst(pointPos);

        laserSprite.setSize(l, 8.f / Constants.Graphics.RES_SIZE);
        pointA.draw(batch);
        laserSprite.draw(batch);

        prevPointPos.set(pointPos);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case setInstrument:
            {
                SetInstrumentEvent e = ((SetInstrumentEvent) event);

                if (getComponentObject() instanceof InstrumentData)
                {
                    InstrumentData instrumentData = ((InstrumentData) getComponentObject());
                    if (e.playerData == instrumentData.getOwner())
                    {
                        setEnabled(instrumentData == e.selected);
                    }
                }

                break;
            }
            case ownerChanged:
            {
                OwnerChangedEvent e = ((OwnerChangedEvent) event);

                if (e.newOwner == null)
                {
                    setEnabled(false);
                }
                break;
            }
            case simple:
            {
                SimpleEvent e = ((SimpleEvent) event);

                if (e.getAction() == SimpleEvent.Action.deselected)
                {
                    setEnabled(false);
                    break;
                }
                break;
            }
            case hookInstrument:
            {
                if (((HookInstrumentEvent) event).selected == getComponentObject())
                {
                    setEnabled(false);
                }

                break;
            }
        }

        return false;
    }

    private void generate()
    {
        if (light != null)
            return;

        ClientMap clientMap = ((ClientMap) getMap());

        if (clientMap == null)
        {
            return;
        }

        if (clientMap.getLights() == null)
        {
            return;
        }

        light = new PointLight(clientMap.getLights(), 8,
            new Color(1, 0.5f, 0.5f, 0.8f), 2,
            pointPos.x, pointPos.y);
        light.setXray(true);
        light.setStaticLight(true);

        if (Map.GetWatcher() != null)
        {
            generatedDimension = Map.GetWatcher().getDimension();
        }
        else
        {
            generatedDimension = "";
        }
    }

    private void updateTransform()
    {
        if (light != null)
        {
            light.setPosition(pointPos.x, pointPos.y);
        }
    }

    private void free()
    {
        if (light != null)
        {
            try
            {
                light.remove(true);
            }
            catch (IllegalArgumentException ignored) {}

            light = null;
        }
    }

    @Override
    public void release()
    {
        super.release();

        free();
    }

    private void setEnabled(boolean b)
    {
        enabled = b;
        updateLight();
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }
}
