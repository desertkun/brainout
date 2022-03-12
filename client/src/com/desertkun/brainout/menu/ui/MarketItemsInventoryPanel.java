package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.components.ItemComponent;
import com.desertkun.brainout.content.components.MaxWeightComponent;
import com.desertkun.brainout.content.upgrades.ExtendedStorage;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ClientMarketContainerComponentData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.MarketUtils;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.MarketService;

import java.util.List;

public class MarketItemsInventoryPanel extends TargetInventoryPanel
{
    private final ClientMarketContainerComponentData marketContainer;
    private final String itemsCategory;
    private Table loading;
    private ItemsLoadedHook itemsLoadedHook;

    public interface ItemsLoadedHook
    {
        void loaded(List<MarketService.MarketItemEntry> entries);
    }

    public static class MarketInventoryRecord extends InventoryRecord
    {
        private final MarketService.MarketItemEntry marketEntry;

        public MarketInventoryRecord(ConsumableRecord record, MarketService.MarketItemEntry marketEntry)
        {
            super(record);

            this.marketEntry = marketEntry;
        }

        @Override
        public boolean withdrawable()
        {
            return !"realestate".equals(marketEntry.name) && !"rsitem".equals(marketEntry.name);
        }

        public MarketService.MarketItemEntry getMarketEntry()
        {
            return marketEntry;
        }
    }

    public void setItemsLoadedHook(ItemsLoadedHook itemsLoadedHook)
    {
        this.itemsLoadedHook = itemsLoadedHook;
    }

    public MarketItemsInventoryPanel(ActiveData marketContainer, InventoryDragAndDrop dragAndDrop, String itemsCategory)
    {
        super(dragAndDrop);

        this.itemsCategory = itemsCategory;
        this.placeInto = marketContainer;
        this.marketContainer = marketContainer.getComponent(ClientMarketContainerComponentData.class);
    }

    public ClientMarketContainerComponentData getMarketContainer()
    {
        return marketContainer;
    }

    @Override
    protected void initBackground()
    {
        loading = new Table();
        add(loading).expandX().fillX().row();

        super.initBackground();
    }

    public void init()
    {
        super.init();

        startLoading();
        requestItems();
    }

    private void requestItems()
    {
        LoginService loginService = LoginService.Get();
        MarketService marketService = MarketService.Get();

        marketService.getMarketItems("freeplay",
            loginService.getCurrentAccessToken(), (request, result, entries) -> Gdx.app.postRunnable(() ->
        {
            stopLoading();

            if (result == Request.Result.success)
            {
                renderItems(entries);
                if (itemsLoadedHook != null)
                {
                    itemsLoadedHook.loaded(entries);
                }
            }
            else
            {
                renderFailure();
            }
        }));
    }

    protected void updated(float weight)
    {

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

    private void renderItems(List<MarketService.MarketItemEntry> entries)
    {
        currentWeight = 0;

        for (MarketService.MarketItemEntry entry: entries)
        {
            if (entry.amount <= 0)
                continue;

            ConsumableRecord r = MarketUtils.MarketObjectToConsumableRecord(entry.name, entry.payload, entry.amount);

            if (r == null)
                continue;

            String category = MarketUtils.GetMarketItemCategory(entry.name);
            if (!itemsCategory.equals(category))
            {
                continue;
            }

            if (r.getItem().getContent().hasComponent(ItemComponent.class))
            {
                ItemComponent item = r.getItem().getContent().getComponent(ItemComponent.class);

                currentWeight += item.getWeight() * r.getAmount();
            }

            addItem(new MarketInventoryRecord(r, entry));
        }

        updated(currentWeight);
    }

    public int canFitAnItem(ConsumableRecord record)
    {
        ItemComponent itemComponent = record.getItem().getContent().getComponent(ItemComponent.class);

        UserProfile profile = BrainOutClient.ClientController.getUserProfile();
        Active containerContent = ((ActiveData)marketContainer.getComponentObject()).getCreator();
        float maxWeight = MarketUtils.GetMaxMarketWeightForPlayer(containerContent, profile,
            itemsCategory);

        if (itemComponent == null || maxWeight == 0)
        {
            return record.getAmount();
        }

        float w = itemComponent.getWeight();
        float left = maxWeight - currentWeight;
        if (left <= 0)
        {
            return 0;
        }
        int canFit = (int)(left / w);
        return Math.min(canFit, record.getAmount());
    }

    private void startLoading()
    {
        loading.clearChildren();

        LoadingBlock loadingBlock = new LoadingBlock();
        loading.add(loadingBlock).expandX().size(16, 16).pad(24);
    }

    private void stopLoading()
    {
        loading.clearChildren();
    }

    @Override
    public String getTitle()
    {
        return marketContainer.getContentComponent().getTitle().get();
    }

    public String getMarket()
    {
        return "freeplay";
    }

    public void refresh()
    {
        clearItems();
        startLoading();
        requestItems();
    }

    @Override
    public ActiveData getPlaceInto()
    {
        ClientMarketContainerComponentData marketContainer = getMarketContainer();
        if (marketContainer != null)
        {
            return (ActiveData) marketContainer.getComponentObject();
        }

        return null;
    }

    public float getMaxWeightForUser()
    {
        if ("rs".equals(itemsCategory))
        {
            return 500;
        }

        float maxWeight = 0;

        if (placeInto != null)
        {
            MaxWeightComponent mx = placeInto.getCreator().getComponent(MaxWeightComponent.class);

            if (mx != null)
            {
                maxWeight += mx.getMaxWeight();
            }
        }

        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        int addWeight = 0;

        if (userProfile != null)
        {
            OrderedMap<ExtendedStorage, Integer> extStorages = userProfile.getItemsOf(ExtendedStorage.class);

            for (ExtendedStorage extStorage : extStorages.keys())
            {
                if (addWeight < extStorage.getExtraWeight())
                    addWeight = extStorage.getExtraWeight();
            }
        }

        maxWeight += addWeight;

        return maxWeight;
    }
}
