package com.desertkun.brainout.data.bullet;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pool;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.BlockHitConfirmationComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.ColliderComponentData;
import com.desertkun.brainout.data.components.ShieldComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.*;
import com.desertkun.brainout.events.BlockHitConfirmationEvent;
import com.desertkun.brainout.events.DamageBlockEvent;
import com.desertkun.brainout.events.DamageEvent;
import com.desertkun.brainout.events.LaunchEffectEvent;

public abstract class BulletData extends ComponentObject implements Renderable, CompleteUpdatable,
        BlockData.ContactPayload, Watcher, Pool.Poolable, Animable
{
    private final Bullet bullet;
    private final Vector2 speed;
    private final float mass;
    private final Pool<BulletData> pool;
    private float powerResponse;
    private boolean done;
    private boolean silent;

    private float power;
    private BlockData blockIn;
    private BulletLaunchData launchData;
    private PlayerData playerData;
    private Team team;
    private int ownerId;

    private Vector2 position;
    private float time;
    private float angle;
    protected float distance;
    private boolean collided;

    private static Vector2 tmpEnd = new Vector2();
    private static Vector2 tmpStart = new Vector2();

    public abstract class BulletLaunchData extends LaunchData
    {
        private final BulletData bullet;

        public BulletLaunchData(BulletData bullet)
        {
            super();

            this.bullet = bullet;
        }

        public BulletData getBullet()
        {
            return bullet;
        }
    }

    private float damage;

    private static Vector2 TMP = new Vector2();
    private InstrumentInfo instrumentInfo;

    public BulletData(Bullet bullet, Pool<BulletData> pool)
    {
        super(bullet, null);

        this.position = new Vector2();
        this.bullet = bullet;
        this.mass = Constants.Core.GRAVITY * bullet.getMass();

        this.instrumentInfo = null;

        this.launchData = new BulletLaunchData(this)
        {
            @Override
            public float getX() {
                return BulletData.this.getX();
            }

            @Override
            public float getY() {
                return BulletData.this.getY();
            }

            @Override
            public float getAngle() {
                return BulletData.this.getAngle();
            }

            @Override
            public String getDimension()
            {
                return BulletData.this.getDimension();
            }

            @Override
            public boolean getFlipX() {
                return false;
            }
        };

        this.speed = new Vector2();
        this.pool = pool;
    }

    public void setup(LaunchData launchData, float damage, String dimension)
    {
        setDimension(dimension);

        this.blockIn = null;
        this.done = false;
        this.ownerId = -1;
        this.distance = 0;
        this.time = bullet.getTimeToLive();
        this.power = bullet.getPower();
        this.powerResponse = power / bullet.getPowerDistance();
        this.collided = false;
        this.silent = false;

        this.position.set(launchData.getX(), launchData.getY());

        this.angle = launchData.getAngle();
        this.damage = damage * bullet.getDamageCoeficient();

        this.speed.set(
            (float)Math.cos(Math.toRadians(angle)) * bullet.getSpeed(),
            (float)Math.sin(Math.toRadians(angle)) * bullet.getSpeed()
        );
    }

    public void init()
    {
        super.init();
    }

    public void release()
    {
        pool.free(this);

        super.release();
    }

    @Override
    public boolean disposeOnRelease()
    {
        // dispose components is disabled for BulletData since it is a pool'able object
        return false;
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);
    }

    @Override
    public void reset()
    {
        Component it = getFistComponent();

        while (it != null)
        {
            if (it instanceof Pool.Poolable)
            {
                ((Pool.Poolable) it).reset();
            }

            it = it.getNext();
        }
    }

    /** @return -1 to filter, 0 to terminate, fraction to clip the ray for closest hit, 1 to continue **/
    private float collision(Fixture fixture, Vector2 point, Vector2 normal, float fraction)
    {
        Map map = getMap();

        if (map == null)
            return 0;

        point.scl(Constants.Physics.SCALE_OF);
        normal.scl(Constants.Physics.SCALE_OF);

        tmpEnd.set(normal).scl(-0.1f).add(point);

        Object userData = fixture.getUserData();

        // No user data means it's a static object (say a block)
        if (userData == null)
        {
            return 1;
        }

        if (userData instanceof ShieldComponentData)
        {
            ShieldComponentData shield = ((ShieldComponentData) userData);

            InstrumentData shieldItself = ((InstrumentData) shield.getComponentObject());

            if (shieldItself.getOwner() == null || playerData == null)
                return 1;

            if (!BrainOut.getInstance().getController().isEnemies(
                playerData.getOwnerId(), shieldItself.getOwner().getOwnerId()))
                    return 1;

            LaunchData hit = new PointLaunchData(point.x, point.y,
                bullet.isHitEffectNormal() ? normal.angleDeg() : getAngle(), getDimension());

            /*
            BrainOut.EventMgr.sendEvent(
                this, LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.hit, hit));
            */

            BrainOut.EventMgr.sendEvent(
                shield.getComponentObject(),
                LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.hit, hit));

            float damage = bullet.calculateDamage(power, getDamage());

            BrainOut.EventMgr.sendEvent(shield.getComponentObject(),
                DamageEvent.obtain(damage, getOwnerId(),
                    instrumentInfo, this,
                    tmpEnd.x, tmpEnd.y, angle,
                    Constants.Damage.DAMAGE_HIT));

            done = true;

            return 0;
        }

        return 1;
    }

    @Override
    public void update(float dt)
    {
        Map map = getMap();

        if (map == null)
            return;

        super.update(dt);

        float stepDt = dt / (float) Constants.Core.BULLET_UPDATE_STEPS;
        float speedLength = speed.len();

        tmpStart.set(position).scl(Constants.Physics.SCALE);
        tmpEnd.set(tmpStart).add(speed.x * dt * 1.05f * Constants.Physics.SCALE,
            speed.y * dt * 1.05f * Constants.Physics.SCALE);

        if (!tmpStart.equals(tmpEnd))
        {
            World world = map.getPhysicWorld();

            if (world == null)
                return;

            world.rayCast(this::collision, tmpStart, tmpEnd);

            if (done)
                return;
        }

        for (int i = 0; i < Constants.Core.BULLET_UPDATE_STEPS; i++)
        {
            float oldX = position.x, oldY = position.y;
            position.add(speed.x * stepDt, speed.y * stepDt);

            float r = (float)Math.atan2(speed.y, speed.x);
            angle = (float)Math.toDegrees(r);

            speed.y -= mass * 12.0f * stepDt;

            power -= powerResponse * speedLength * stepDt;

            int blockX = (int)position.x, blockY = (int)position.y;

            BlockData blockData = map.getBlockAt(
                blockX, blockY, Constants.Layers.BLOCK_LAYER_FOREGROUND);

            if (blockData != null && !blockData.isContact(this,
                position.x - (int)position.x, position.y - (int)position.y , null, speed, TMP, 0,
                map, blockX, blockY))
            {
                blockData = null;
            }

            if (blockIn != blockData)
            {
                LaunchData hitLaunchData = null;

                if ((blockIn == null) != (blockData == null))
                {
                    if (blockData != null)
                    {
                        if (bullet.isHitEffectNormal())
                        {
                            // we're hit the block
                            hitLaunchData = blockData.calculateContact(
                                getLaunchData(),
                                new PointLaunchData(oldX, oldY, getAngle(), getDimension()),
                                true, map, blockX, blockY);
                        }
                        else
                        {
                            hitLaunchData = new PointLaunchData(oldX, oldY, getAngle(), getDimension());
                        }

                        BrainOut.EventMgr.sendEvent(this,
                            LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.hit, hitLaunchData));

                        if (bullet.isBlockEffectEnabled())
                        {
                            BrainOut.EventMgr.sendEvent(blockData,
                                LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.hit, hitLaunchData));
                        }

                        if (blockData.getCreator().hasComponent(BlockHitConfirmationComponent.class))
                        {
                            BrainOut.EventMgr.sendEvent(
                                BlockHitConfirmationEvent.obtain(
                                    playerData, blockData, blockX, blockY,
                                    map.getDimensionId(), (int)calculateDamage()));
                        }
                    }
                    else
                    {
                        if (bullet.isHitEffectNormal())
                        {
                            // we're not hitting the block anymore
                            hitLaunchData = blockIn.calculateContact(
                                new PointLaunchData(oldX, oldY, getAngle(), getDimension()),
                                getLaunchData(), false,
                                map, blockX, blockY);
                        }
                        else
                        {
                            hitLaunchData = new PointLaunchData(oldX, oldY, getAngle(), getDimension());
                        }

                        BrainOut.EventMgr.sendEvent(this,
                            LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.hit, hitLaunchData));

                        if (bullet.isBlockEffectEnabled())
                        {
                            BrainOut.EventMgr.sendEvent(blockIn,
                                LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.hit, hitLaunchData));
                        }
                    }
                }

                if (blockData != null)
                {
                    float damage = bullet.calculateDamage(blockData.limitPower(power), getDamage());

                    BrainOut.EventMgr.sendEvent(blockData,
                        DamageBlockEvent.obtain(map, damage, blockX, blockY,
                            Constants.Layers.BLOCK_LAYER_FOREGROUND,
                            instrumentInfo, this));
                }
            }

            blockIn = blockData;

            if (blockIn != null)
            {
                power -= blockIn.getCreator().getResist() * stepDt;

                if (power <= 0)
                {
                    done = true;

                    return;
                }
            }

            if (power > 0)
            {
                collide(position.x, position.y);
            }
        }
        time -= dt;

        distance += speed.len() * dt;
    }

    private void collide(float x, float y)
    {
        if (isCollided()) return;

        Map map = getMap();

        if (map == null)
            return;

        for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.COLLIDER, false))
        {
            if (activeData.getTeam() instanceof SpectatorTeam)
                continue;

            ColliderComponentData cc = activeData.getComponent(ColliderComponentData.class);

            if (cc != null)
            {
                cc.collide(x, y, this);
            }
        }
    }

    @Override
    public boolean done()
    {
        return time <= 0 || done;
    }

    public float getX()
    {
        return position.x;
    }

    public float getY()
    {
        return position.y;
    }

    public float getAngle()
    {
        return angle;
    }

    public BulletLaunchData getLaunchData()
    {
        return launchData;
    }

    public Bullet getBullet()
    {
        return bullet;
    }

    public float getPower()
    {
        return power;
    }

    public int getOwnerId()
    {
        return ownerId;
    }

    public Vector2 getSpeed()
    {
        return speed;
    }

    public void setOwnerId(int ownerId)
    {
        this.ownerId = ownerId;
    }

    public PlayerData getPlayerData()
    {
        return playerData;
    }

    public void setPlayerData(PlayerData playerData)
    {
        this.playerData = playerData;

        if (playerData != null)
        {
            this.team = playerData.getTeam();
        }
    }

    public void setTeam(Team team)
    {
        this.team = team;
    }

    public Team getTeam()
    {
        return team;
    }

    public float getDamage()
    {
        return damage;
    }

    public float calculateDamage()
    {
        return getBullet().calculateDamage(getPower(), getDamage());
    }

    public InstrumentInfo getInstrumentInfo()
    {
        return instrumentInfo;
    }

    public void setInstrumentInfo(InstrumentInfo instrumentInfo)
    {
        this.instrumentInfo = instrumentInfo;
    }

    public boolean isCollided()
    {
        return collided;
    }

    public void setCollided(boolean collided)
    {
        this.collided = collided;
    }

    public boolean isSilent()
    {
        return silent;
    }

    public void setSilent(boolean silent)
    {
        this.silent = silent;
    }

    @Override
    public float getWatchX()
    {
        return position.x;
    }

    @Override
    public float getWatchY()
    {
        return position.y;
    }

    @Override
    public boolean allowZoom()
    {
        return false;
    }

    @Override
    public float getScale()
    {
        return 1;
    }

    @Override
    public boolean getFlipX()
    {
        return false;
    }
}
