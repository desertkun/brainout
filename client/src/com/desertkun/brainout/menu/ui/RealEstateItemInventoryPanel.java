package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.components.InventoryMoveSoundComponent;
import com.desertkun.brainout.content.components.ItemComponent;
import com.desertkun.brainout.content.components.MaxWeightComponent;
import com.desertkun.brainout.content.components.RealEstateItemContainerComponent;
import com.desertkun.brainout.content.upgrades.ExtendedStorage;
import com.desertkun.brainout.data.FreePlayMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ClientMarketContainerComponentData;
import com.desertkun.brainout.data.components.RealEstateItemComponentData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.MarketUtils;
import com.desertkun.brainout.utils.RealEstateInfo;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.MarketService;
import org.json.JSONObject;

import java.util.List;

public class RealEstateItemInventoryPanel extends TargetInventoryPanel
{
    private ActiveData rsItem;
    private Table loading;

    public class RealEstateItemInventoryRecord extends InventoryRecord
    {
        private final String key;

        public RealEstateItemInventoryRecord(ConsumableRecord record, String key)
        {
            super(record);

            this.key = key;
        }

        public String getKey()
        {
            return key;
        }
    }

    @Override
    protected DragAndDropInventory newDragAndDropInventory()
    {
        return new DragAndDropInventory(pane, this::updateBackgroundPanel)
        {
            @Override
            protected boolean extraRecordCheck(InventoryRecord record)
            {
                RealEstateItemContainerComponent cc = rsItem.getContent().getComponent(RealEstateItemContainerComponent.class);
                if (cc != null)
                {
                    ItemComponent itemComponent = record.getRecord().getItem().getContent().getComponent(ItemComponent.class);

                    if (itemComponent == null || itemComponent.getTags(record.getRecord().getItem().getContent()) == null)
                    {
                        return false;
                    }

                    if (!(itemComponent.getTags(record.getRecord().getItem().getContent()).contains(cc.getTagLimit(), false)))
                    {
                        return false;
                    }
                }

                return true;
            }
        };
    }

    public ActiveData getRsItem()
    {
        return rsItem;
    }

    public String getRealEstateItemKey()
    {
        RealEstateItemComponentData rsi = rsItem.getComponent(RealEstateItemComponentData.class);
        if (rsi == null)
        {
            return null;
        }

        return rsi.getKey();
    }

    public RealEstateInfo.RealEstatePayload getRealEstateObject()
    {
        FreePlayMap map = rsItem.getMap(FreePlayMap.class);
        if (map == null)
        {
            return null;
        }

        return map.getRealEstateInfo().payload;
    }

    public RealEstateItemInventoryPanel(
        ActiveData rsItem, InventoryDragAndDrop dragAndDrop)
    {
        super(dragAndDrop);

        this.rsItem = rsItem;
    }

    public void init()
    {
        super.init();

        clearItems();
        requestItems();
    }

    @Override
    protected void initBackground()
    {
        loading = new Table();
        add(loading).expandX().fillX().row();

        super.initBackground();
    }

    private void requestItems()
    {
        JSONObject args = new JSONObject();

        args.put("map", rsItem.getDimension());
        args.put("key", getRealEstateItemKey());

        BrainOutClient.SocialController.sendRequest("get_market_item_container", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                renderItems(response);
            }

            @Override
            public void error(String reason)
            {
                if (Log.ERROR) Log.error(reason);
                renderFailure();
            }
        });
    }

    private void renderFailure()
    {
        currentWeight = 0;
        updated(0);

        loading.clearChildren();
        Label l = new Label(L.get("MENU_ERROR_TRY_AGAIN"), BrainOutClient.Skin, "title-red");
        l.setAlignment(Align.center);
        l.setWrap(true);
        loading.add(l).expandX().fillX().row();
    }

    @Override
    public void refresh()
    {
        clearItems();
        requestItems();
    }

    private void createPlaceholders(String tagLimit, Array<RealEstateItemContainerComponent.Placeholder> placeholders)
    {
        for (RealEstateItemContainerComponent.Placeholder entry : placeholders)
        {
            if (BrainOutClient.getRegion(entry.placeholder) == null)
                continue;

            for (int i = 0; i < entry.amount; i++)
            {
                Group group = new Group();
                group.setSize(192, 64);

                Image image = new Image(BrainOutClient.Skin, entry.placeholder);

                image.setFillParent(true);
                image.setScaling(Scaling.none);
                image.setTouchable(Touchable.disabled);

                group.addActor(image);

                addPlaceholder(group, "", tagLimit);
            }
        }
    }

    private void renderItems(JSONObject object)
    {
        currentWeight = 0;
        doRenderItems(object);
        updated(currentWeight);
    }

    private void doRenderItems(JSONObject object)
    {
        RealEstateItemContainerComponent cc = rsItem.getContent().getComponent(RealEstateItemContainerComponent.class);
        if (cc != null && cc.getTagLimit() != null && cc.getPlaceholders() != null)
        {
            createPlaceholders(cc.getTagLimit(), cc.getPlaceholders());
        }
        RealEstateInfo.RealEstatePayload.ObjectAtLocation.ObjectContainer c =
            new RealEstateInfo.RealEstatePayload.ObjectAtLocation.ObjectContainer();
        c.read(object);

        for (ObjectMap.Entry<String, MarketService.MarketItemEntry> entry : c.items)
        {
            ConsumableRecord r = MarketUtils.MarketObjectToConsumableRecord(
                    entry.value.name, entry.value.payload, entry.value.amount);

            if (r == null)
                continue;

            if (r.getItem().getContent().hasComponent(ItemComponent.class))
            {
                ItemComponent item = r.getItem().getContent().getComponent(ItemComponent.class);

                currentWeight += item.getWeight() * r.getAmount();
            }

            addItem(new RealEstateItemInventoryRecord(r, entry.key));
        }
    }

    protected void updated(float weight)
    {

    }

    @Override
    public String getTitle()
    {
        return "ITEMS";
    }

    @Override
    public ActiveData getPlaceInto()
    {
        return rsItem;
    }

    public float getMaxWeightForUser()
    {
        return rsItem.getContent().getComponent(RealEstateItemContainerComponent.class).getWeightLimit();
    }
}
