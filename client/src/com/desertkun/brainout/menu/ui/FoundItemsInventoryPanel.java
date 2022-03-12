package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.components.ClientItemComponent;
import com.desertkun.brainout.content.components.InventoryMoveSoundComponent;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.RoundLockSafeData;
import com.desertkun.brainout.data.components.ClientItemComponentData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.events.ActiveActionEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;

public class FoundItemsInventoryPanel extends TargetInventoryPanel implements EventReceiver
{
    private Table loading;
    private RepeatAction looking;

    class RegisteredRecord
    {
        public boolean flag;
    }

    private final PlayerData playerData;
    private ObjectMap<ConsumableRecord, RegisteredRecord> registeredRecords;
    private boolean discovery;

    public static class FoundInventoryRecord extends InventoryRecord
    {
        private final ActiveData itemData;

        public FoundInventoryRecord(ConsumableRecord record, ActiveData itemData)
        {
            super(record);

            this.itemData = itemData;
        }

        public ActiveData getItemData()
        {
            return itemData;
        }
    }

    public FoundItemsInventoryPanel(InventoryDragAndDrop dragAndDrop, PlayerData playerData)
    {
        super(dragAndDrop);

        this.playerData = playerData;
        this.registeredRecords = new ObjectMap<>();

        process();

        addAction(Actions.repeat(RepeatAction.FOREVER,
            Actions.sequence(
                Actions.delay(0.125f),
                Actions.run(this::process)
            )));
    }

    @Override
    protected void initBackground()
    {
        loading = new Table();
        add(loading).expandX().fillX().row();

        super.initBackground();
    }

    private boolean good(ActiveData itemData)
    {
        float diffX = (itemData.getX() - this.playerData.getX()),
              diffY = (itemData.getY() - this.playerData.getY());
        float dist = diffX * diffX + diffY * diffY;

        return dist < ClientConstants.Items.FOUND_DISTANCE;
    }

    private void process()
    {
        ClientMap map = Map.GetWatcherMap(ClientMap.class);

        if (map == null)
            return;

        if (placeInto != null && !discovery)
        {
            ClientItemComponentData ci = placeInto.getComponent(ClientItemComponentData.class);
            if (ci != null && ci.isDiscover() && ci.hasUndiscoveredRecords())
            {
                discovery = true;

                loading.clearChildren();

                LoadingBlock loadingBlock = new LoadingBlock();
                loading.add(loadingBlock).expandX().size(16, 16).pad(24);

                looking = Actions.repeat(RepeatAction.FOREVER,
                Actions.sequence(
                    Actions.delay(0.6f),
                    Actions.run(() ->
                    {
                        ConsumableRecord discovered = ci.discoverARecord();
                        if (discovered != null)
                        {
                            playDiscovered(discovered);

                            if (ci.hasUndiscoveredRecords())
                            {
                                return;
                            }
                        }

                        loading.clearChildren();
                        removeAction(looking);
                        looking = null;
                    })
                ));

                addAction(looking);
            }
        }

        for (ObjectMap.Entry<ConsumableRecord, RegisteredRecord> entry : registeredRecords)
        {
            entry.value.flag = false;
        }

        map.getListActivesForTag(
            ClientConstants.Items.FOUND_DISTANCE,
            this.playerData.getX(),
            this.playerData.getY(),
            ActiveData.class, Constants.ActiveTags.ITEM,
            this::processItem);

        for (ObjectMap.Entry<ConsumableRecord, RegisteredRecord> entry : registeredRecords)
        {
            if (!entry.value.flag)
            {
                removeItem(entry.key);
            }
        }
    }

    private void playDiscovered(ConsumableRecord discovered)
    {
        if (placeInto == null)
            return;
        Content content = discovered.getItem().getContent();
        ClientMap map = placeInto.getMap(ClientMap.class);
        if (map == null)
            return;

        InventoryMoveSoundComponent snd = content.getComponent(InventoryMoveSoundComponent.class);

        if (snd == null)
        {
            String effectId = (content instanceof Weapon) ? "move-weapon-snd" : "move-other-snd";

            SoundEffect sound = BrainOutClient.ContentMgr.get(effectId, SoundEffect.class);

            if (sound != null)
            {
                map.addEffect(sound, new PointLaunchData(
                    placeInto.getX(), placeInto.getY(), 0, placeInto.getDimension()));
            }
        }
        else
        {
            snd.play(placeInto);
        }
    }

    private void createPlaceholders(ObjectMap<String, Item.ItemFilter> filters, ObjectMap<String, String> placeholders)
    {
        for (ObjectMap.Entry<String, Item.ItemFilter> entry : filters)
        {
            String name = entry.key;
            String placeholder = placeholders.get(name);

            if (placeholder == null)
                continue;

            if (BrainOutClient.getRegion(placeholder) == null)
                continue;

            Item.ItemFilter filter = entry.value;

            for (int i = 0; i < filter.getLimit(); i++)
            {
                Group group = new Group();
                group.setSize(192, 64);

                Image image = new Image(BrainOutClient.Skin, placeholder);

                image.setFillParent(true);
                image.setScaling(Scaling.none);
                image.setTouchable(Touchable.disabled);

                group.addActor(image);

                addPlaceholder(group, name, filter);
            }
        }
    }

    public void updated(ActiveData item)
    {
        ClientItemComponentData cic = item.getComponent(ClientItemComponentData.class);

        if (cic == null)
            return;

        if (cic.getRecords().getData().size == 0)
            return;

        for (ConsumableRecord record : cic.getRecords().getData().values())
        {
            InventoryItem item_ = getItem(record);

            if (item_ != null)
            {
                item_.update();
            }
        }

        process();
    }

    private boolean processItem(ActiveData item)
    {
        ClientItemComponentData cic = item.getComponent(ClientItemComponentData.class);

        if (cic == null)
            return false;

        if (item instanceof RoundLockSafeData && ((RoundLockSafeData) item).isLocked())
        {
            return false;
        }

        if (placeInto == null && !cic.isAutoRemove())
        {
            placeInto = item;

            if (placeInto instanceof ItemData)
            {
                Item c = ((Item) placeInto.getContent());

                if (c.getFilters() != null)
                {
                    if (cic.getContentComponent().getPlaceholders() != null)
                    {
                        createPlaceholders(c.getFilters(), cic.getContentComponent().getPlaceholders());
                    }
                }
            }
        }

        if (cic.getRecords().getData().size == 0)
            return false;

        for (ConsumableRecord record : cic.getRecords().getData().values())
        {
            if (!hasItem(record))
            {
                if (item == placeInto && !displayRecord(record))
                    continue;

                addItem(new FoundInventoryRecord(record, item));
            }

            RegisteredRecord r = registeredRecords.get(record);

            if (r == null)
            {
                r = new RegisteredRecord();
                registeredRecords.put(record, r);
            }

            r.flag = true;
        }

        return false;
    }



    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case activeAction:
            {
                ActiveActionEvent ev = ((ActiveActionEvent) event);

                switch (ev.action)
                {
                    case updated:
                    {
                        if (ev.activeData instanceof ItemData)
                        {
                            ItemData itemData = ((ItemData) ev.activeData);

                            if (good(itemData))
                            {
                                updated(itemData);
                            }
                        }

                        break;
                    }
                }
                break;
            }
        }

        return false;
    }

    @Override
    public void init()
    {
        super.init();

        BrainOutClient.EventMgr.subscribe(Event.ID.activeAction, this);
    }

    @Override
    public void release()
    {
        super.release();

        registeredRecords.clear();
        registeredRecords = null;

        BrainOutClient.EventMgr.unsubscribe(Event.ID.activeAction, this);
    }

    @Override
    protected boolean displayRecord(ConsumableRecord record)
    {
        if (placeInto != null)
        {
            ClientItemComponentData ci = placeInto.getComponent(ClientItemComponentData.class);
            if (ci != null && ci.isDiscover() && !ci.isRecordDiscovered(record))
            {
                return false;
            }
        }

        return super.displayRecord(record);
    }

    @Override
    public String getTitle()
    {
        if (placeInto == null)
            return null;

        ClientItemComponentData cic = placeInto.getComponent(ClientItemComponentData.class);

        if (cic != null && cic.hasFilters())
        {
            return placeInto.getContent().getTitle().get();
        }
        else
        {
            ClientItemComponent it = placeInto.getContent().getComponent(ClientItemComponent.class);
            if (it != null && it.getTitle() != null)
            {
                return it.getTitle().get();
            }
        }

        return null;
    }
}
