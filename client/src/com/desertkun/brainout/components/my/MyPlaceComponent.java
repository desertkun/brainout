package com.desertkun.brainout.components.my;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.common.msg.client.PlaceBlockMsg;
import com.desertkun.brainout.common.msg.client.RemoveBlockMsg;
import com.desertkun.brainout.components.ClientPlayerComponent;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.consumable.impl.DefaultConsumableItem;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.SimplePhysicsComponentData;
import com.desertkun.brainout.data.components.PlaceAnimationComponentData;
import com.desertkun.brainout.data.components.PlayerAnimationComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.PlaceBlockData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.events.LaunchEffectEvent;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.graphics.CenterSprite;
import com.desertkun.brainout.gs.ActionPhaseState;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.impl.SelectContentMenu;

public class MyPlaceComponent extends Component
{
    private final PlaceBlockData placeBlockData;
    private final CenterSprite placeSprite;
    private final PointLaunchData placeLaunchData;
    private final ConsumableRecord record;
    private State state;
    protected float timer;
    protected boolean launching;
    private Mode mode;

    private Block currentBlock;

    private int placeX;
    private int placeY;

    public enum State
    {
        idle,
        done
    }

    private enum Mode
    {
        add,
        remove
    }

    public MyPlaceComponent(PlaceBlockData placeBlockData, ConsumableRecord record)
    {
        super(placeBlockData, null);

        this.placeBlockData = placeBlockData;
        this.state = State.idle;
        this.timer = 0;
        this.launching = false;
        this.currentBlock = null;

        this.placeLaunchData = new PointLaunchData(0, 0, 0, getComponentObject().getDimension());
        this.placeSprite = new CenterSprite(BrainOutClient.getRegion("place-cursor"), placeLaunchData);
        this.record = record;
    }

    public Block getCurrentBlock()
    {
        return currentBlock;
    }

    @Override
    public void update(float dt)
    {
        switch (state)
        {
            case idle:
            {
                if (isLaunching())
                {
                    doLaunch();
                }

                break;
            }
            case done:
            {
                timer -= dt;
                if (timer <= 0)
                {
                    setState(State.idle);
                }

                break;
            }
        }

        if (currentBlock != null)
        {
            PlayerOwnerComponent poc = placeBlockData.getOwner().getComponent(PlayerOwnerComponent.class);

            if (!poc.getConsumableContainer().hasConsumable(currentBlock))
            {
                switchSource(null);
            }
        }

        MyPlayerComponent mpc = placeBlockData.getOwner().getComponent(MyPlayerComponent.class);

        this.placeX = mpc.getPosX();
        this.placeY = mpc.getPosY();

        placeLaunchData.setX(placeX + 0.5f);
        placeLaunchData.setY(placeY + 0.5f);
    }

    private void doLaunch()
    {
        PlaceAnimationComponentData crc = getComponentObject().getComponent(PlaceAnimationComponentData.class);

        timer = placeBlockData.getPlaceBlock().getPlaceTime();

        setState(State.done);

        switch (mode)
        {
            case add:
            {
                if (!place(placeX, placeY)) return;
                break;
            }
            case remove:
            {
                if (!remove(placeX, placeY, Constants.Layers.BLOCK_LAYER_FOREGROUND)) return;
                break;
            }
        }

        BrainOut.EventMgr.sendDelayedEvent(getComponentObject(), LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.shoot,
                crc.getLaunchPointData()));
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        placeSprite.draw(batch);
    }

    private LaunchData getPlayerData()
    {
        if (placeBlockData.getOwner() != null)
        {
            return placeBlockData.getOwner().getComponent(PlayerAnimationComponentData.class).getPrimaryLaunchData();
        }

        return null;
    }

    @Override
    public void init()
    {
        super.init();

        PlayerOwnerComponent poc = placeBlockData.getOwner().getComponent(PlayerOwnerComponent.class);

        checkCurrentBlock();

        BrainOut.EventMgr.subscribe(Event.ID.simple, this);
    }

    @Override
    public void release()
    {
        super.release();

        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
    }

    public boolean place(int x, int y)
    {
        if (currentBlock != null)
        {
            ActiveData playerData = placeBlockData.getOwner();

            MyPlayerComponent mpc = playerData.getComponent(MyPlayerComponent.class);
            ClientPlayerComponent cpc = playerData.getComponent(ClientPlayerComponent.class);

            SimplePhysicsComponentData phy = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

            BrainOutClient.ClientController.sendUDP(new PlaceBlockMsg(
                playerData.getX(),
                playerData.getY(),
                mpc.getOriginalDirection(),
                cpc.getMousePosition().x, cpc.getMousePosition().y,
                x, y, currentBlock.getDefaultLayer(), currentBlock.getID(), record));

            return true;
        }

        return false;
    }


    public boolean remove(int x, int y, int layer)
    {
        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return false;

        ActiveData playerData = placeBlockData.getOwner();
        BlockData blockData = map.getBlock(x, y, layer);

        if (blockData != null)
        {
            MyPlayerComponent mpc = playerData.getComponent(MyPlayerComponent.class);
            SimplePhysicsComponentData phy = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);
            ClientPlayerComponent cpc = playerData.getComponent(ClientPlayerComponent.class);

            BrainOutClient.ClientController.sendUDP(new RemoveBlockMsg(
                playerData.getX(),
                playerData.getY(),
                mpc.getOriginalDirection(),
                    cpc.getMousePosition().x, cpc.getMousePosition().y,
                x, y, layer, record));

            return true;
        }

        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case simple:
            {
                SimpleEvent simpleEvent = ((SimpleEvent) event);

                if (simpleEvent.getAction() == SimpleEvent.Action.consumablesUpdated)
                {
                    checkCurrentBlock();
                }

                break;
            }
            case gameController:
            {
                GameControllerEvent gcEvent = ((GameControllerEvent) event);

                switch (gcEvent.action)
                {
                    case beginLaunch:
                    {
                        setMode(Mode.add);
                        beginLaunching();

                        return true;
                    }

                    case beginLaunchSecondary:
                    {
                        setMode(Mode.remove);
                        beginLaunching();

                        return true;
                    }

                    case endLaunch:
                    {
                        endLaunching();

                        return true;
                    }

                    case switchSource:
                    {
                        switchSource();

                        return true;
                    }

                    case selectSource:
                    {
                        selectSource();

                        return true;
                    }
                }

                return false;
            }
        }

        return false;
    }

    private void checkCurrentBlock()
    {
        PlayerOwnerComponent poc = placeBlockData.getOwner().getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return;

        if (currentBlock == null || poc.getConsumableContainer().getConsumable(currentBlock) == null)
        {
            currentBlock = (Block)poc.switchConsumable(Block.class);

            BrainOut.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.playerInfoUpdated));
        }
    }

    private void switchSource()
    {
        PlayerOwnerComponent poc = placeBlockData.getOwner().getComponent(PlayerOwnerComponent.class);
        switchSource((Block)poc.switchConsumable(currentBlock, Block.class));
    }

    private void switchSource(Block block)
    {
        currentBlock = block;
    }

    private void selectSource()
    {
        PlayerOwnerComponent poc = placeBlockData.getOwner().getComponent(PlayerOwnerComponent.class);
        final ConsumableContainer container = poc.getConsumableContainer();

        GameState topState = BrainOutClient.getInstance().topState();
        if (topState instanceof ActionPhaseState)
        {
            Array<ConsumableRecord> contents = new Array<ConsumableRecord>();

            for (ObjectMap.Entry<Integer, ConsumableRecord> entry: container.getData())
            {
                if (entry.value.getItem() instanceof DefaultConsumableItem && entry.value.getItem().getContent() instanceof Block)
                {
                    contents.add(entry.value);
                }
            }

            topState.pushMenu(new SelectContentMenu(currentBlock, contents, new SelectContentMenu.OnSelect()
            {
                @Override
                public void selected(ConsumableRecord content)
                {
                    currentBlock = (Block)content.getItem().getContent();

                    BrainOut.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.playerInfoUpdated));
                }
            }));
        }
    }

    public void setState(State state)
    {
        this.state = state;
    }

    public boolean isLaunching()
    {
        return launching;
    }

    private void endLaunching()
    {
        launching = false;
    }

    private void beginLaunching()
    {
        launching = true;
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

    public Mode getMode()
    {
        return mode;
    }

    public void setMode(Mode mode)
    {
        this.mode = mode;
    }
}
