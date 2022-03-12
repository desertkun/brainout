package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.events.AddImpulseEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SetSpeedEvent;
import com.desertkun.brainout.utils.AngleDifference;
import com.desertkun.brainout.utils.GroupTimeChanges;
import com.desertkun.brainout.utils.TimedChanges;
import com.esotericsoftware.minlog.Log;

public abstract class PlayerControllerComponentData extends Component<ContentComponent>
{
    private final PlayerData playerData;
    private final GroupTimeChanges groupTimeChanges;
    private TimedChanges aimChanges, sitChanges;

    protected static Vector2 tmp = new Vector2();
    private static final float BOTTOM_CONTACT_MAX = 0.125f;

    private Vector2 moveDirection, originalDirection;
    protected Player.State state;
    private float hasJumped;
    private float bottomContactAccumulator;
    private boolean jumpDirty;
    private Boost boost;

    public enum PositionMode
    {
        normal,
        sit,
        squat,
        crouch
    }

    private class Boost
    {
        private float time;
        private Vector2 velocity;

        public Boost(float time, float x, float y)
        {
            this.time = time;
            this.velocity = new Vector2(x, y);
        }

        private boolean update(float dt)
        {
            time -= dt;

            if (time < 0)
                return true;

            /*
            playerData.setPosition(
                playerData.getX() + velocity.x * dt,
                playerData.getY() + velocity.y * dt
            );
            */

            SimplePhysicsComponentData pcd = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

            if (pcd != null)
            {
                pcd.applyForce(velocity);
            }

            return false;
        }
    }
    public PlayerControllerComponentData(final PlayerData playerData,
         ContentComponent contentComponent)
    {
        super(playerData, contentComponent);

        this.playerData = playerData;
        this.hasJumped = 0;
        this.bottomContactAccumulator = 0;

        this.state = Player.State.normal;

        this.groupTimeChanges = new GroupTimeChanges();
        this.moveDirection = new Vector2();
        this.originalDirection = new Vector2();

        this.boost = null;
    }

    @Override
    public void init()
    {
        super.init();

        initChanges();
    }

    private void initChanges()
    {
        PlayerComponentData pcd = getComponentObject().getComponentWithSubclass(PlayerComponentData.class);

        groupTimeChanges.getChild().add(new TimedChanges("prevMousePosition", Constants.Moves.CHANGE_TIME_MOUSE)
        {
            private Vector2 prevMousePosition = new Vector2(pcd.getMousePosition());

            @Override
            public void reset(TimedChanges who)
            {
                prevMousePosition.set(pcd.getMousePosition());
            }

            @Override
            public boolean changesMade()
            {
                return prevMousePosition.dst2(pcd.getMousePosition()) > 4 * 4;
            }

            @Override
            public void sendChanges()
            {
                prevMousePosition.set(pcd.getMousePosition());
                sendPlayerData(true, 0);
            }
        });

        groupTimeChanges.getChild().add(new TimedChanges("angle", Constants.Moves.CHANGE_TIME_MOUSE)
        {
            private float angle = playerData.getAngle();

            @Override
            public void reset(TimedChanges who)
            {
                angle = playerData.getAngle();
            }

            @Override
            public boolean changesMade()
            {
                return AngleDifference.diff(angle, playerData.getAngle()) > 20;
            }

            @Override
            public void sendChanges()
            {
                angle = playerData.getAngle();
                sendPlayerData(false, 1);
            }
        });

        if (BrainOut.getInstance().getController().isServer())
        {
            groupTimeChanges.getChild().add(new TimedChanges("anyway", Constants.Moves.CHANGE_TIME_ANYWAY)
            {
                @Override
                public void reset(TimedChanges who)
                {
                    //
                }

                @Override
                public boolean changesMade()
                {
                    return true;
                }

                @Override
                public void sendChanges()
                {
                    sendPlayerData(false, 2);
                }
            });
        }

        groupTimeChanges.getChild().add(new TimedChanges("anyway_slow", Constants.Moves.CHANGE_TIME_ANYWAY_SLOW)
        {
            @Override
            public void reset(TimedChanges who)
            {
                //
            }

            @Override
            public boolean changesMade()
            {
                return true;
            }

            @Override
            public void sendChanges()
            {
                sendPlayerData(false, 1);
            }
        });

        groupTimeChanges.getChild().add(new TimedChanges("dimension_change", 0)
        {
            int dimension = playerData.getDimensionId();

            @Override
            public void reset(TimedChanges who)
            {
                dimension = playerData.getDimensionId();
            }

            @Override
            public boolean changesMade()
            {
                return dimension != playerData.getDimensionId();
            }

            @Override
            public void sendChanges()
            {
                sendPlayerData(false, 0);
            }
        });

        groupTimeChanges.getChild().add(new TimedChanges("fixtures", Constants.Moves.CHANGE_TIME_ANYWAY)
        {
            SimplePhysicsComponentData cmp = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

            @Override
            public boolean changesMade()
            {
                return cmp.hasFixture();
            }

            @Override
            public boolean canBeResetByOthers()
            {
                return false;
            }

            @Override
            public void reset(TimedChanges who)
            {
            }

            @Override
            public void sendChanges()
            {
                sendPlayerData(false, 1);
            }
        });

        //commented because now this function done in prevPosX/prevSpeedX and prevPosY/prevSpeedY sections
        /*
        groupTimeChanges.getChild().add(new TimedChanges("zeroSpeed", 0)
        {
            SimplePhysicsComponentData cmp = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

            private boolean zeroSpeed = calc();

            @Override
            public boolean changesMade()
            {
                return zeroSpeed != calc();
            }

            private boolean calc()
            {
                return Math.abs(cmp.getSpeed().x) < 0.02f;
            }

            @Override
            public boolean canBeResetByOthers()
            {
                return false;
            }

            @Override
            public void reset(TimedChanges who)
            {
                zeroSpeed = calc();
            }

            @Override
            public void sendChanges()
            {
                sendPlayerData(false);
            }
        });
        */

        groupTimeChanges.getChild().add(new TimedChanges("prevPosX/prevSpeedX", 0.05f)
        {
            SimplePhysicsComponentData cmp = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

            private float
                    prevPosX = playerData.getX(),
                    prevSpeedX = cmp.getSpeed().x;

            @Override
            public boolean changesMade()
            {
                return Math.abs(prevPosX - playerData.getX()) > Constants.Moves.CHANGE_MOVE_DIST ||
                        Math.abs(prevSpeedX - cmp.getSpeed().x) > Constants.Moves.CHANGE_SPEED_DIST;
            }

            @Override
            public boolean isForceUpdate()
            {
                float currentSpeedX = cmp.getSpeed().x;

                float prevDirection = Math.signum(prevSpeedX);
                float currentDirection = Math.signum(currentSpeedX);

                if (prevDirection != currentDirection)
                {
                    return true;
                }

                return false;
            }

            @Override
            public boolean canBeResetByOthers()
            {
                return false;
            }

            @Override
            public void reset(TimedChanges who)
            {
                prevPosX = playerData.getX();
                prevSpeedX = cmp.getSpeed().x;
            }

            @Override
            public void sendChanges()
            {
                sendPlayerData(false, 1);
            }
        });

        groupTimeChanges.getChild().add(new TimedChanges("prevPosY/prevSpeedY", 0.1f)
        {
            SimplePhysicsComponentData cmp = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

            private float
                    prevPosY = playerData.getY(),
                    prevSpeedY = cmp.getSpeed().y;

            @Override
            public boolean changesMade()
            {
                return Math.abs(prevPosY - playerData.getY()) > Constants.Moves.CHANGE_MOVE_DIST ||
                        Math.abs(prevSpeedY - cmp.getSpeed().y) > Constants.Moves.CHANGE_SPEED_DIST;
            }

            @Override
            public boolean isForceUpdate()
            {
                float currentSpeedY = cmp.getSpeed().y;

                float prevDirection = Math.signum(prevSpeedY);
                float currentDirection = Math.signum(currentSpeedY);

                if (prevDirection != currentDirection)
                {
                    return true;
                }

                return false;
            }

            @Override
            public boolean canBeResetByOthers()
            {
                return false;
            }

            @Override
            public void reset(TimedChanges who)
            {
                prevPosY = playerData.getY();
                prevSpeedY = cmp.getSpeed().y;
            }

            @Override
            public void sendChanges()
            {
                sendPlayerData(false, 1);
            }
        });

        groupTimeChanges.getChild().add(new TimedChanges("onGround")
        {
            SimplePhysicsComponentData cmp = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

            private boolean onGround = cmp.hasContact(SimplePhysicsComponentData.Contact.bottom);

            @Override
            public boolean changesMade()
            {
                return onGround != cmp.hasContact(SimplePhysicsComponentData.Contact.bottom);
            }

            @Override
            public void reset(TimedChanges who)
            {
                onGround = cmp.hasContact(SimplePhysicsComponentData.Contact.bottom);
            }

            @Override
            public void sendChanges()
            {
                sendPlayerData(false, 1);
            }
        });

        groupTimeChanges.getChild().add(new TimedChanges("jump", 0)
        {
            @Override
            public boolean changesMade()
            {
                return jumpDirty;
            }

            @Override
            public void sendChanges()
            {
                sendPlayerData(false, 1);
            }

            @Override
            public void reset(TimedChanges who)
            {
                jumpDirty = false;
            }
        });

        aimChanges = new TimedChanges("aim", 0)
        {
            PlayerComponentData cpc = playerData.getComponentWithSubclass(PlayerComponentData.class);

            private boolean aim = cpc.isAim();

            @Override
            public boolean changesMade()
            {
                return aim != cpc.isAim();
            }

            @Override
            public void sendChanges()
            {
                sendAim(aim);
            }

            @Override
            public void reset(TimedChanges who)
            {
                aim = cpc.isAim();
            }
        };

        sitChanges = new TimedChanges("state", 0)
        {
            PlayerComponentData cpc = playerData.getComponentWithSubclass(PlayerComponentData.class);

            private Player.State state = cpc.getState();

            @Override
            public boolean changesMade()
            {
                return state != cpc.getState();
            }

            @Override
            public void sendChanges()
            {
                sendState(state);
            }

            @Override
            public void reset(TimedChanges who)
            {
                state = cpc.getState();
            }
        };
    }

    protected abstract void sendPlayerData(boolean spectatorsOnly, int priority);
    protected abstract void sendAim(boolean aim);
    protected abstract void sendState(Player.State state);

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
    public boolean onEvent(Event event)
    {
        return false;
    }

    public PlayerData getPlayerData()
    {
        return playerData;
    }

    public boolean hasTopContact()
    {
        SimplePhysicsComponentData pcd = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (pcd != null)
        {
            float pX = playerData.getX(),
                    pY = playerData.getY() + pcd.getHalfSize().y + 0.5f;

            Map map = getMap();

            if (map == null)
                return false;

            BlockData block = map.getBlockAt(pX, pY, Constants.Layers.BLOCK_LAYER_FOREGROUND);

            if (block != null && block.isConcrete())
            {
                return true;
            }
        }

        return false;
    }

    public boolean hasBottomContact()
    {
        SimplePhysicsComponentData pcd = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        return pcd != null && pcd.hasContact(SimplePhysicsComponentData.Contact.bottom);
    }

    protected void setRun(boolean sit)
    {
        if (isWounded())
            return;

        if (playerData.getState() == Player.State.sit)
            return;

        if (playerData.getState() == Player.State.squat)
            return;

        if (isCrouching())
            return;

        if (playerData.canRun())
        {
            this.state = sit ? Player.State.run : Player.State.normal;
        }
        else
        {
            this.state = Player.State.normal;
        }

        PlayerComponentData cpc = playerData.getComponentWithSubclass(PlayerComponentData.class);

        if (cpc != null)
        {
            cpc.setState(this.state);
            updateMoveDirection();
        }
    }

    protected boolean isWounded()
    {
        return playerData.isWounded();
    }

    protected boolean isCrouching()
    {
        return state == Player.State.crawl;
    }

    protected void setPositionMode(PositionMode mode)
    {
        if (isWounded())
            return;

        switch (mode)
        {
            case normal:
            {
                this.state = Player.State.normal;
                break;
            }
            case sit:
            {
                this.state = Player.State.sit;
                break;
            }
            case squat:
            {
                this.state = Player.State.squat;
                break;
            }
            case crouch:
            {
                if (BrainOut.PackageMgr.getDefine("crawl", "").equals("disabled"))
                {
                    return;
                }

                this.state = Player.State.crawl;
                break;
            }
        }

        // in case we have something at top then we can't stand up
        if ((mode == PositionMode.normal && hasTopContact()))
        {
            return;
        }

        PlayerComponentData cpc = playerData.getComponentWithSubclass(PlayerComponentData.class);

        if (cpc != null)
        {
            cpc.setState(this.state);
            updateMoveDirection();
        }
    }

    protected boolean isEnabled()
    {
        return true;
    }

    protected boolean applyMoveDirection()
    {
        return true;
    }

    @Override
    public void update(float dt)
    {
        if (!isEnabled())
            return;

        Map map = getMap();

        if (map == null)
            return;

        SimplePhysicsComponentData pcd = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        boolean hasBottomContact = pcd.hasContact(SimplePhysicsComponentData.Contact.bottom);
        boolean hasFixture = pcd.hasFixture();

        if (hasBottomContact)
        {
            bottomContactAccumulator = BOTTOM_CONTACT_MAX;
        }
        else
        {
            if (BrainOut.getInstance().getController().isServer())
            {
                if (bottomContactAccumulator > 0)
                {
                    bottomContactAccumulator = Math.max(bottomContactAccumulator - dt, 0);
                }
            }
            else
            {
                bottomContactAccumulator = 0;
            }
        }

        boolean hadBottomContact = bottomContactAccumulator > 0;

        float forceX = 0, forceY = 0;

        if (playerData.getPlayer().isFlying())
        {
            pcd.getSpeed().x = moveDirection.x;
            pcd.getSpeed().y = moveDirection.y;
        }
        else
        {
            if (hadBottomContact || hasFixture)
            {
                if (hasBottomContact() || hasFixture)
                {
                    pcd.getSpeed().x = moveDirection.x;
                }

                if (hasFixture)
                {
                    pcd.getSpeed().y = moveDirection.y;
                }
                else
                {
                    if (moveDirection.y > 0.2f && hasJumped <= 0)
                    {
                        jump();
                    }
                    else
                    {
                        hasJumped = Math.max(hasJumped - dt, 0);
                    }
                }
            }
            else
            {
                if (moveDirection.y > 0)
                {
                    forceX = moveDirection.x * dt * 0.5f;
                }

                hasJumped = Math.max(hasJumped - dt, 0);
            }
        }

        float pW = 0.1f + pcd.getHalfSize().x, pH = 0.9f - pcd.getHalfSize().y,
                pX = playerData.getX(), pY = playerData.getY();
        float sign = Math.signum(moveDirection.x);
        float blockCheck = sign * pW;

        /*
        if (Math.abs(moveDirection.x) > 0.01f && moveDirection.y >= 0)
        {
            float checkX = pX + blockCheck, checkY = pY + pH;
            BlockData block = map.getBlockAt(checkX, checkY, Constants.Layers.BLOCK_LAYER_FOREGROUND);

            tmp.set(pcd.getSpeed());

            if (block != null && block.isConcrete() &&
                    block.isContact(null, checkX % 1, checkY % 1, tmp, tmp, 0))
            {
                boolean good = true;

                int checks = (int)(pcd.getHalfSize().y * 2.f + 0.1f);
                for (int i = 1; i <= checks; i++)
                {
                    BlockData neighbor = block.getNeightbor(0, i);
                    if (neighbor != null && neighbor.isConcrete())
                    {
                        good = false;
                        break;
                    }
                }

                if (good && boost == null)
                {
                    switch (block.getNeighborsMask())
                    {
                        case 0:
                        case 1:
                        case 2:
                        case 4:
                        case 8:
                        {
                            BlockData bottom = map.getBlockAt(pX, checkY - 1, Constants.Layers.BLOCK_LAYER_FOREGROUND);

                            if (bottom instanceof ConcreteBD)
                            {
                                jump();
                            }
                            break;
                        }
                        default:
                        {
                            //boost(0.25f, sign * 5.0f, 5.0f);
                        }
                    }
                }
            }
        }
        */

        boolean w = isWounded();

        if ((state == Player.State.wounded) != w)
        {
            if (w)
            {
                setState(Player.State.wounded);
            }
            else
            {
                setState(Player.State.normal);
            }
        }

        if (boost != null)
        {
            if (boost.update(dt))
            {
                boost = null;
            }
        }

        BrainOut.EventMgr.sendDelayedEvent(playerData, AddImpulseEvent.obtain(forceX, forceY));

        groupTimeChanges.update(dt);

        if (aimChanges != null)
            aimChanges.update(dt);

        if (sitChanges != null)
            sitChanges.update(dt);
    }

    private void setState(Player.State state)
    {
        this.state = state;

        PlayerComponentData cpc = playerData.getComponentWithSubclass(PlayerComponentData.class);

        if (cpc != null)
        {
            cpc.setState(this.state);
            updateMoveDirection();
        }
    }

    private void boost(float time, float x, float y)
    {
        this.boost = new Boost(time, x, y);
    }

    private void jump()
    {
        if (isWounded())
            return;

        if (state == Player.State.sit)
            return;

        if (state == Player.State.squat)
            return;

        if (state == Player.State.crawl)
            return;

        if (hasJumped > 0)
            return;

        SimplePhysicsComponentData pcd = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (pcd != null)
        {
            BrainOut.EventMgr.sendDelayedEvent(playerData,
                SetSpeedEvent.obtain(0, playerData.getPlayer().getJumpForce(), 0, 1));

            hasJumped = 0.5f;
            bottomContactAccumulator = BOTTOM_CONTACT_MAX;
            setJumpDirty();
        }
    }

    private void setJumpDirty()
    {
        jumpDirty = true;
    }

    protected void updateMoveDirection()
    {
        PlayerData pl = playerData;

        if (pl.getCurrentState() == null)
            return;

        this.moveDirection.set(originalDirection);
        this.moveDirection.scl(pl.getCurrentState().speed);

        if (pl.getCurrentInstrument() != null)
        {
            InstrumentData current = pl.getCurrentInstrument();
            this.moveDirection.x *= getInstrumentSpeedCoef(current);
        }

        this.moveDirection.x *= pl.getSpeedCoef();
    }

    public Vector2 getMaximumSpeed()
    {
        tmp.set(playerData.getCurrentState().speed);

        if (playerData.getCurrentInstrument() != null)
        {
            InstrumentData current = playerData.getCurrentInstrument();
            tmp.x *= getInstrumentSpeedCoef(current);
        }

        tmp.x *= playerData.getSpeedCoef();

        return tmp;
    }

    protected float getInstrumentSpeedCoef(InstrumentData current)
    {
        return current.getInstrument().getSpeedCoef();
    }

    public void setMoveDirection(float moveX, float moveY)
    {
        this.originalDirection.set(
            MathUtils.clamp(moveX, -1.0f, 1.0f),
            MathUtils.clamp(moveY, -1.0f, 1.0f));

        updateMoveDirection();
    }

    public void setMoveDirection(Vector2 direction)
    {
        if (isCrouching() && direction.y > 0.5f)
        {
            setPositionMode(PositionMode.normal);
            return;
        }

        this.originalDirection.set(direction);

        updateMoveDirection();
    }

    public Vector2 getOriginalDirection()
    {
        return originalDirection;
    }

    public Vector2 getMoveDirection()
    {
        return moveDirection;
    }
}
