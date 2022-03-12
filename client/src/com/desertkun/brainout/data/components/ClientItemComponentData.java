package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntSet;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.components.*;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.Animable;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.ItemActionEvent;
import com.desertkun.brainout.graphics.CenterSprite;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientItemComponent")
@ReflectAlias("data.components.ClientItemComponentData")
public class ClientItemComponentData extends Component<ClientItemComponent> implements WithTag
{
    private final ItemData itemData;
    private final float offsetY;
    private LaunchData launchData;
    private InstrumentAnimationComponentData animation;
    private float counter;
    private IntSet discoveredItems;

    private CenterSprite iconSprite, badgeSprite;

    public ClientItemComponentData(ItemData itemData, ClientItemComponent clientItemComponent)
    {
        super(itemData, clientItemComponent);

        this.counter = 0;
        this.itemData = itemData;
        this.offsetY = clientItemComponent.getOffsetY();

        if (clientItemComponent.isDiscover())
        {
            discoveredItems = new IntSet();
        }
    }

    public ConsumableContainer getRecords()
    {
        return itemData.getRecords();
    }

    public boolean hasFilters()
    {
        return ((Item) itemData.getContent()).getFilters() != null;
    }

    public boolean isAutoRemove()
    {
        return itemData.isAutoRemove();
    }

    public float getWeight()
    {
        return itemData.getRecords().getWeight();
    }

    public void updateWeight()
    {
        itemData.getRecords().updateWeight();
    }

    public void openSound()
    {
        if (itemData.openSound == null)
            return;

        SoundEffect openSound = BrainOutClient.ContentMgr.get(itemData.openSound, SoundEffect.class);

        if (openSound == null)
            return;

        ClientMap clientMap = getMap(ClientMap.class);
        clientMap.addEffect(openSound, launchData);
    }

    public boolean isDiscover()
    {
        return getContentComponent().isDiscover();
    }

    public void closeSound()
    {
        if (itemData.closeSound == null)
            return;

        SoundEffect closeSound = BrainOutClient.ContentMgr.get(itemData.closeSound, SoundEffect.class);

        if (closeSound == null)
            return;

        ClientMap clientMap = getMap(ClientMap.class);
        clientMap.addEffect(closeSound, launchData);
    }

    public boolean isRecordDiscovered(ConsumableRecord record)
    {
        if (record.getWho() == BrainOutClient.ClientController.getMyId())
            return true;

        if (discoveredItems == null)
            return false;

        return discoveredItems.contains(record.getId());
    }

    public ConsumableRecord discoverARecord()
    {
        for (ConsumableRecord record : itemData.getRecords().getData().values())
        {
            if (isRecordDiscovered(record))
                continue;

            if (discoveredItems == null)
            {
                discoveredItems = new IntSet();
            }

            discoveredItems.add(record.getId());
            return record;
        }

        return null;
    }

    public boolean hasUndiscoveredRecords()
    {
        for (ConsumableRecord record : itemData.getRecords().getData().values())
        {
            if (!isRecordDiscovered(record))
                return true;
        }

        return false;
    }

    @Override
    public void init()
    {
        super.init();

        this.launchData = new LaunchData()
        {

            @Override
            public float getX()
            {
                return itemData.getX();
            }

            @Override
            public float getY()
            {
                return itemData.getY() + offsetY;
            }

            @Override
            public float getAngle()
            {
                return 0;
            }

            @Override
            public String getDimension()
            {
                return itemData.getDimension();
            }

            @Override
            public boolean getFlipX()
            {
                return false;
            }
        };

        Animable animable = new Animable()
        {
            @Override
            public float getX()
            {
                return itemData.getX();
            }

            @Override
            public float getY()
            {
                return itemData.getY() + offsetY;
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

        IconComponent iconComponent = getComponentObject().getContent().getComponent(IconComponent.class);

        if (iconComponent != null)
        {
            if (getContentComponent().isIconBadge())
            {
                LaunchData badge = new LaunchData()
                {
                    @Override
                    public float getX()
                    {
                        return itemData.getX() + 0.5f;
                    }

                    @Override
                    public float getY()
                    {
                        return itemData.getY() + 2.0f +(float) Math.cos(counter) * 0.25f + offsetY;
                    }

                    @Override
                    public float getAngle()
                    {
                        return 0;
                    }

                    @Override
                    public String getDimension()
                    {
                        return itemData.getDimension();
                    }

                    @Override
                    public boolean getFlipX()
                    {
                        return false;
                    }
                };

                this.badgeSprite = new CenterSprite(iconComponent.getIcon("game-icon"), badge);
            }
            else
            {
                LaunchData badge = new LaunchData()
                {
                    @Override
                    public float getX()
                    {
                        return itemData.getX();
                    }

                    @Override
                    public float getY()
                    {
                        return itemData.getY() + offsetY;
                    }

                    @Override
                    public float getAngle()
                    {
                        return 0;
                    }

                    @Override
                    public String getDimension()
                    {
                        return itemData.getDimension();
                    }

                    @Override
                    public boolean getFlipX()
                    {
                        return false;
                    }
                };

                this.badgeSprite = new CenterSprite(iconComponent.getIcon("game-icon"), badge);
            }
        }

        if (getContentComponent().isFetchIcon())
        {

            // look for the instrument first
            for (ConsumableRecord record : itemData.getRecords().getData().values())
            {
                if (record.getItem() instanceof InstrumentConsumableItem)
                {
                    InstrumentConsumableItem instrumentConsumableItem = ((InstrumentConsumableItem) record.getItem());
                    InstrumentData instrumentData = instrumentConsumableItem.getInstrumentData();
                    InstrumentInfo info = instrumentData.getInfo();

                    InstrumentAnimationComponent animation = info.instrument.
                            getComponentFrom(InstrumentAnimationComponent.class);

                    if (animation == null)
                    {
                        throw new RuntimeException("Instrument " + info.instrument.getID() +
                                " should have some animation.");
                    }

                    this.animation = animation.getComponent(null);

                    this.animation.setSkin(info.skin);

                    this.animation.init();

                    for (String key : info.upgrades.orderedKeys())
                    {
                        Upgrade upgrade = info.upgrades.get(key);
                        ReplaceSlotComponent replaceSlot = upgrade.getComponent(ReplaceSlotComponent.class);

                        if (replaceSlot != null)
                        {
                            replaceSlot.upgradeSkeleton(this.animation.getSkeleton());
                        }
                    }

                    this.animation.attachTo(animable);

                    return;
                }
            }

            // then for the icons
            for (ConsumableRecord record : itemData.getRecords().getData().values())
            {
                IconComponent icon;

                ConsumableItem item = record.getItem();

                if (item instanceof InstrumentConsumableItem)
                {
                    InstrumentConsumableItem i = ((InstrumentConsumableItem) item);

                    icon = i.getInstrumentData().getInfo().skin.getComponent(IconComponent.class);
                } else
                {
                    icon = item.getContent().getComponent(IconComponent.class);
                }

                if (icon != null)
                {
                    TextureRegion r = icon.getIcon("icon-small");

                    if (r != null)
                    {

                        LaunchData itemLaunchData = new LaunchData()
                        {
                            @Override
                            public float getX()
                            {
                                return itemData.getX();
                            }

                            @Override
                            public float getY()
                            {
                                return itemData.getY() + offsetY;
                            }

                            @Override
                            public float getAngle()
                            {
                                return 0;
                            }

                            @Override
                            public String getDimension()
                            {
                                return itemData.getDimension();
                            }

                            @Override
                            public boolean getFlipX()
                            {
                                return false;
                            }
                        };


                        this.iconSprite = new CenterSprite(icon.getIcon("icon-small"), itemLaunchData);

                        if (r.getRegionWidth() > 32)
                        {
                            this.iconSprite.setScale(32.0f / r.getRegionWidth());
                        } else if (r.getRegionHeight() > 32)
                        {
                            this.iconSprite.setScale(32.0f / r.getRegionHeight());
                        }

                        return;
                    }
                }
            }
        }
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (animation != null)
        {
            animation.update(dt);
        }

        counter += dt * ClientConstants.Items.WAVING;
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

            if (Vector2.dst(playerData.getX(), playerData.getY(), itemData.getX(), itemData.getY())
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

        return false;
    }

    protected boolean canTake(PlayerData playerData)
    {
        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

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

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        if (animation != null)
        {
            animation.render(batch, context);
        }
        else if (iconSprite != null)
        {
            iconSprite.draw(batch);
        }

        if (badgeSprite != null)
        {
            badgeSprite.draw(batch);
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case itemAction:
            {
                ItemActionEvent ev = ((ItemActionEvent) event);

                action(ev.action);
                break;
            }
        }

        return false;
    }

    private void action(String action)
    {
        switch (action)
        {
            case "open":
            {
                openSound();

                break;
            }
            case "close":
            {
                closeSound();

                break;
            }
        }
    }

    @Override
    public void release()
    {
        super.release();

        if (animation != null)
        {
            animation.release();
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

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.ITEM);
    }
}
