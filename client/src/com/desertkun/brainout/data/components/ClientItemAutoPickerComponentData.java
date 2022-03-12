package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.common.msg.client.PickUpItemMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.*;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.interfaces.Animable;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.ItemTakingEvent;
import com.desertkun.brainout.menu.ui.ActiveProgressBar;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientItemAutoPickerComponent")
@ReflectAlias("data.components.ClientItemAutoPickerComponentData")
public class ClientItemAutoPickerComponentData extends Component<ClientItemAutoPickerComponent>
{
    private final ItemData itemData;
    private SimplePhysicsComponentData phy;
    private ActiveProgressBar progressBar;

    private float pickCheck;
    private float pickTimer;
    private State state;
    private PlayerData pickingPlayer;

    public enum State
    {
        none,
        picking
    }

    public ClientItemAutoPickerComponentData(ItemData itemData, ClientItemAutoPickerComponent clientItemComponent)
    {
        super(itemData, clientItemComponent);

        this.pickCheck = ClientConstants.Items.START_PICK;
        this.pickTimer = 0;
        this.state = State.none;

        this.itemData = itemData;
    }

    public ConsumableContainer getRecords()
    {
        return itemData.getRecords();
    }

    @Override
    public void init()
    {
        super.init();

        this.phy = getComponentObject().getComponentWithSubclass(SimplePhysicsComponentData.class);

        BrainOut.EventMgr.subscribe(Event.ID.itemTaking, this);

        Animable animable = new Animable()
        {
            @Override
            public float getX()
            {
                return phy.getX();
            }

            @Override
            public float getY()
            {
                return phy.getY();
            }

            @Override
            public float getAngle()
            {
                return 0;
            }

            @Override
            public boolean getFlipX()
            {
                return false;
            }
        };

        progressBar = new ActiveProgressBar(animable);
        progressBar.setForegroundColor(Color.GREEN);
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        switch (state)
        {
            case none:
            {
                if (pickCheck > 0)
                {
                    pickCheck -= dt;
                }
                else
                {
                    PlayerData playerData = findPlayer();
                    if (playerData != null && checkItems(playerData))
                    {
                        pickCheck = ClientConstants.Items.PICK_TIMEOUT;

                        if (BrainOut.EventMgr.sendEvent(ItemTakingEvent.obtain(playerData, itemData)))
                        {
                            break;
                        }

                        pickingPlayer = playerData;
                        pickTimer = getContentComponent().getPickTime();
                        state = State.picking;
                    }
                }

                break;
            }
            case picking:
            {
                pickTimer -= dt;

                pickingPlayer = findPlayer();
                if (pickingPlayer == null)
                {
                    state = State.none;
                    break;
                }

                if (getContentComponent().getPickTime() > 0)
                {
                    progressBar.setValue(1.0f - pickTimer / getContentComponent().getPickTime());
                }

                if (pickTimer < 0)
                {
                    if (checkItems(pickingPlayer))
                    {
                        takeIt();
                    }

                    done();

                    state = State.none;
                }

                break;
            }
        }
    }

    private PlayerData findPlayer()
    {
        CSGame game = BrainOutClient.ClientController.getState(CSGame.class);

        if (game != null)
        {
            PlayerData playerData = game.getPlayerData();

            if (playerData == null)
            {
                return null;
            }

            if (Vector2.dst(playerData.getX(), playerData.getY(), phy.getX(), phy.getY())
                    <= ClientConstants.Items.PICK_DISTANCE)
            {
                return playerData;
            }
        }

        return null;
    }

    private boolean checkItems(PlayerData playerData)
    {
        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        float w = 0;

        if (!canTake(playerData))
        {
            return false;
        }

        /*

        for (ConsumableRecord record : itemData.getRecords().getData().values())
        {
            Content itemContent = record.getItem().getContent();

            if (itemContent.hasComponent(ItemComponent.class))
            {
                ItemComponent itemComponent = itemContent.getComponent(ItemComponent.class);
                w += itemComponent.getWeight() * record.getAmount();
            }
        }

        return poc != null &&
            w + poc.getConsumableContainer().getWeight() <= playerData.getPlayer().getMaxOverweight();
        */

        return true;
    }

    protected boolean canTake(PlayerData playerData)
    {
        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return false;

        for (ConsumableRecord record : itemData.getRecords().getData().values())
        {
            if (record.getItem() instanceof InstrumentConsumableItem)
            {
                // in case of instrument we should approve picking up
                InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());
                Content content = ici.getInstrumentData().getContent();
                if (content instanceof Instrument)
                {
                    Slot slot = ((Instrument) content).getSlot();

                    if (poc.getInstrumentForSlot(slot) != null)
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void takeIt()
    {
        BrainOutClient.ClientController.sendUDP(new PickUpItemMsg(itemData.getId()));
    }

    private void done()
    {
        ActiveData activeData = phy.getActiveData();

        if (getContentComponent().getEffect() != null)
        {
            getContentComponent().getEffect().launchEffects(new PointLaunchData(
                    activeData.getX(), activeData.getY(), 0, activeData.getDimension()));
        }
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        switch (state)
        {
            case picking:
            {
                progressBar.render(batch, context);

                break;
            }
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case itemTaking:
            {
                ItemTakingEvent itemTakingEvent = ((ItemTakingEvent) event);

                if (state == State.picking && pickingPlayer == itemTakingEvent.playerData)
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void release()
    {
        super.release();

        BrainOut.EventMgr.unsubscribe(Event.ID.itemTaking, this);

        if (progressBar != null)
        {
            progressBar.dispose();
            progressBar = null;
        }
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
