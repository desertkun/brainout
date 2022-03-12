package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.SocialController;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.consumable.impl.RealEstateConsumableItem;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.ConfirmationPopup;
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.mode.ClientFreeRealization;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.utils.ContentImage;
import com.desertkun.brainout.utils.DurationUtils;
import com.desertkun.brainout.utils.MarketUtils;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.MarketService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class MarketMenu extends Menu
{
    private final String menuCategory;
    private Table data;
    private Table leftPanel;
    private Table content;

    private MainSelector mainSelector;
    private String accountFilter;
    private String accountFilterName;
    private String giveItemFilter;
    private String giveItemFilterTitle;
    private JSONObject giveItemPayloadFilter;
    private Table orders;
    private Table search;
    private int ruAvailable;
    private float scrollY;
    private Label ruAvailableLabel;
    private Table ruAvailableIcon;
    private Array<MarketCategoryFilter> filters;

    private ScrollPane ordersPane;
    private Table items;
    private BuyButton hoveredBuyButton;
    private ButtonGroup categoriesButtons;

    private final String NO_FILTER_CATEGORY_NAME = "MARKET_ALL_ITEMS";
    private TextButton sorting;
    private boolean forcePriceSort;
    private boolean hideSortingButton;

    public static class MarketCategoryFilter
    {
        public String title;
        public String kind;
        public JSONObject filter;

        public MarketCategoryFilter(String title)
        {
            this.title = title;
        }

        public MarketCategoryFilter(String title, String kind)
        {
            this(title);
            this.kind = kind;
            this.filter = new JSONObject();
        }
        public MarketCategoryFilter(String title, String kind, String key, String value)
        {
            this(title);
            this.kind = kind;
            this.filter = new JSONObject();
            this.filter.put(key, value);
        }
    }

    private class BuyButton extends TextButton
    {
        private boolean me;
        private int price;

        public BuyButton(boolean me, int price, com.badlogic.gdx.scenes.scene2d.ui.Skin skin, String styleName)
        {
            super(price + " RU", skin, styleName);

            this.me = me;
            this.price = price;
        }

        public void setDefaultState()
        {
            setStyle(BrainOutClient.Skin.get("button-default", TextButton.TextButtonStyle.class));
            setText(price + " RU");
        }

        public void setBuyState()
        {
            setStyle(BrainOutClient.Skin.get("button-green", TextButton.TextButtonStyle.class));
            setText(L.get("MENU_MARKET_BUY") + "\n" + price + " RU");
        }

        public void setCancelState()
        {
            setStyle(BrainOutClient.Skin.get("button-yellow", TextButton.TextButtonStyle.class));
            setText(L.get("MENU_CANCEL"));
        }

        public boolean isMe()
        {
            return me;
        }
    }

    public enum MainSelector
    {
        commonOrders,
        myOrders
    }

    public MarketMenu(String category)
    {
        mainSelector = MainSelector.commonOrders;
        menuCategory = category;

        filters = new Array<>();

        filters.add(new MarketCategoryFilter(NO_FILTER_CATEGORY_NAME));

        switch (category)
        {
            case "default":
            {
                filters.add(new MarketCategoryFilter("SLOT_CATEGORY_SUBMACHINE", "instrument", "ct", "submachine"));
                filters.add(new MarketCategoryFilter("SLOT_CATEGORY_ASSAULT", "instrument", "ct", "assault"));
                filters.add(new MarketCategoryFilter("SLOT_CATEGORY_PISTOLS", "instrument", "ct", "pistol"));
                filters.add(new MarketCategoryFilter("SLOT_CATEGORY_SHOTGUN", "instrument", "ct", "shotgun"));
                filters.add(new MarketCategoryFilter("SLOT_CATEGORY_SNIPER", "instrument", "ct", "sniper"));
                filters.add(new MarketCategoryFilter("SLOT_CATEGORY_OTHER", "instrument", "ct", "other"));
                filters.add(new MarketCategoryFilter("SLOT_CATEGORY_VALUABLES", "consumable", "k", "val"));
                filters.add(new MarketCategoryFilter("SLOT_CATEGORY_AMMO", "consumable", "k", "bull"));
                filters.add(new MarketCategoryFilter("SLOT_CATEGORY_ITEMS", "consumable", "k", "itm"));
                filters.add(new MarketCategoryFilter("SLOT_CATEGORY_ARMOR", "armor"));
                filters.add(new MarketCategoryFilter("MENU_CATEGORY_REAL_ESTATE", "realestate"));
                filters.add(new MarketCategoryFilter("MENU_CATEGORY_RESOURCES", "consumable", "k", "res"));
                break;
            }
            case "rs":
            {
                filters.add(new MarketCategoryFilter("MENU_CATEGORY_REAL_ESTATE_DECOR", "rsitem", "k", "decor"));
                filters.add(new MarketCategoryFilter("MENU_CATEGORY_REAL_ESTATE_UNIQUE", "rsitem", "k", "unique"));
                break;
            }
        }
    }

    public void setMainSelector(MainSelector mainSelector)
    {
        this.mainSelector = mainSelector;
    }

    @Override
    public Table createUI()
    {
        data = new Table();

        leftPanel = new Table();
        leftPanel.align(Align.top);
        content = new Table();
        content.align(Align.top);

        data.add().height(96).colspan(2).row();

        data.add(leftPanel).width(256).expandY().fill().padLeft(16).padRight(8);
        data.add(content).expand().fill().padRight(16).row();

        renderLeftPanel();
        renderContents();

        data.add().height(16).colspan(2).row();

        addListener(new ClickListener()
        {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y)
            {
                if (ordersPane == null) return super.mouseMoved(event, x, y);

                updateHoveredRow();

                return super.mouseMoved(event, x, y);
            }
        });

        addListener(new InputListener()
        {
            @Override
            public boolean keyDown(InputEvent event, int keycode)
            {
                if (keycode == Input.Keys.SHIFT_LEFT)
                    if (hoveredBuyButton != null)
                        hoveredBuyButton.setCancelState();

                return super.keyDown(event, keycode);
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode)
            {
                if (keycode == Input.Keys.SHIFT_LEFT)
                    if (hoveredBuyButton != null)
                        hoveredBuyButton.setBuyState();

                return super.keyUp(event, keycode);
            }
        });

        return data;
    }

    private void renderContents()
    {
        {
            Table hdr = new Table();

            {
                Table h = new Table(BrainOutClient.Skin);
                h.setBackground("form-gray");

                Label order = new Label(L.get("MENU_MARKET_ORDER"), BrainOutClient.Skin, "title-yellow");
                h.add(order).expand().center();

                hdr.add(h).expandX().fill();
            }

            {
                Table h = new Table(BrainOutClient.Skin);
                h.setBackground("form-gray");

                {
                    sorting = new TextButton(L.get(
                        getSortFilter() == MarketService.MarkerEntriesOrder.none ?
                            "MENU_ORDER_DATE" : "MENU_ORDER_PRICE"
                    ), BrainOutClient.Skin, "button-fill-gray");

                    sorting.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            forcePriceSort = !forcePriceSort;
                            refresh(false);
                        }
                    });

                    h.add(sorting).width(200).expandX().right().padRight(8);
                }

                Label order = new Label(L.get("MENU_PRICE_FOR_1_PIECE_SHORT"), BrainOutClient.Skin, "title-yellow");
                h.add(order).padRight(16);

                hdr.add(h).width(192 * 2).fill().row();
            }

            content.add(hdr).expandX().fill().row();
        }

        orders = new Table();
        content.add(orders).expand().fill().row();
    }

    private void renderLeftPanel()
    {
        leftPanel.clear();

        {
            ButtonGroup<Button> orderSelector = new ButtonGroup<>();
            Table btns = new Table();

            Button commonOrders = new Button(BrainOutClient.Skin, "button-notext-checkable");
            commonOrders.add(new Image(BrainOutClient.Skin, "skillpoints-small"));
            Button myOrders = new Button(BrainOutClient.Skin, "button-notext-checkable");
            myOrders.add(new Image(BrainOutClient.Skin, "icon-favorite-on"));

            btns.add(commonOrders).width(96);
            btns.add(myOrders).width(96);

            commonOrders.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    setMainSelector(MainSelector.commonOrders);
                    refresh(true);
                }
            });

            myOrders.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    setMainSelector(MainSelector.myOrders);
                    refresh(true);
                }
            });

            orderSelector.add(commonOrders, myOrders);

            leftPanel.add(btns).padBottom(8).expandX().left().height(32).row();
        }

        {
            search = new Table(BrainOutClient.Skin);
            search.setBackground("form-default");

            setSearchInactive();

            leftPanel.add(search).padBottom(8).expandX().fill().row();
        }

        {
            Table categories = new Table();

            categoriesButtons = new ButtonGroup();

            for (MarketCategoryFilter filter : filters)
            {
                TextButton ct = new TextButton(L.get(filter.title), BrainOutClient.Skin, "button-checkable");
                ct.getLabel().setWrap(true);
                ct.getLabel().setEllipsis(true);

                ct.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        hideSortingButton = false;
                        giveItemFilterTitle = L.get(filter.title);
                        giveItemFilter = filter.kind;
                        giveItemPayloadFilter = stripQuality(filter.filter);
                        refresh(true);
                    }
                });


                categoriesButtons.add(ct);
                categories.add(ct).expandX().fillX().row();
            }

            leftPanel.add(categories).expandX().fill().row();
        }

        search.toFront();
    }

    private JSONObject stripQuality(JSONObject filter)
    {
        if (filter == null)
        {
            return null;
        }

        String[] names = JSONObject.getNames(filter);

        if (names == null)
        {
            return null;
        }

        JSONObject o = new JSONObject(filter, names);
        if (o.has("q"))
        {
            o.remove("q");
        }
        return o;
    }

    private MarketService.MarkerEntriesOrder getSortFilter()
    {
        if (forcePriceSort)
        {
            return MarketService.MarkerEntriesOrder.takeAmountAsc;
        }

        if (giveItemFilterTitle != null)
        {
            return MarketService.MarkerEntriesOrder.none;
        }

        if (giveItemFilter != null || accountFilter != null || giveItemPayloadFilter != null)
        {
            return MarketService.MarkerEntriesOrder.takeAmountAsc;
        }

        return MarketService.MarkerEntriesOrder.none;
    }

    private void refresh(boolean resetScroll)
    {
        refresh(resetScroll, false);
    }

    private void refresh(boolean resetScroll, boolean uncheckAll)
    {
        if (resetScroll)
        {
            scrollY = 0;
        }

        if (giveItemFilter == null)
            categoriesButtons.setChecked(L.get(NO_FILTER_CATEGORY_NAME));

        if (uncheckAll)
            categoriesButtons.uncheckAll();

        if (giveItemFilterTitle == null && giveItemFilter != null && giveItemPayloadFilter != null)
        {
            search.clearChildren();
            search.setBackground("form-dark-blue");

            String itemText;

            if (giveItemFilterTitle != null)
            {
                itemText = giveItemFilterTitle;
            }
            else
            {
                ConsumableRecord record =
                        MarketUtils.MarketObjectToConsumableRecord(giveItemFilter, giveItemPayloadFilter, 1);

                if (record != null)
                {
                    itemText = getItemText(record);
                }
                else
                {
                    itemText = "???";
                }
            }

            Label searchFor = new Label(itemText, BrainOutClient.Skin, "title-small");
            searchFor.setEllipsis(true);
            searchFor.setWrap(true);
            searchFor.setAlignment(Align.center);

            search.add(searchFor).expand().fill();

            ImageButton closeSearch = new ImageButton(BrainOutClient.Skin, "button-reject");

            closeSearch.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    hideSortingButton = false;
                    giveItemFilterTitle = null;
                    giveItemFilter = null;
                    giveItemPayloadFilter = null;
                    refresh(true);
                }
            });

            search.add(closeSearch).size(32).row();
        }
        else
        {
            setSearchInactive();
        }

        setKeyboardFocus(null);
        orders.clearChildren();
        orders.add(new LoadingBlock()).expand().center().pad(128).row();

        if (sorting != null)
        {
            sorting.setVisible(!hideSortingButton);
            sorting.setText(L.get(
                getSortFilter() == MarketService.MarkerEntriesOrder.none ?
                    "MENU_ORDER_DATE" : "MENU_ORDER_PRICE"
            ));
        }

        MarketService marketService = MarketService.Get();
        LoginService loginService = LoginService.Get();

        if (marketService != null && loginService != null)
        {
            switch (mainSelector)
            {
                case commonOrders:
                {
                    marketService.listOrders("freeplay", accountFilter,
                        giveItemFilter, 0, MarketService.ListOrderComparison.none, giveItemPayloadFilter, "ru",
                        0, MarketService.ListOrderComparison.none, null, getSortFilter(),
                        loginService.getCurrentAccessToken(),
                    (request, result, entries) ->
                    {
                        if (result == Request.Result.success)
                        {
                            Gdx.app.postRunnable(() -> renderOrders(entries));
                        }
                        else
                        {
                            Gdx.app.postRunnable(this::renderFailure);
                        }
                    }, 0, 100);

                    break;
                }
                case myOrders:
                {
                    marketService.listMyOrders("freeplay", loginService.getCurrentAccessToken(),
                    (request, result, entries) ->
                    {
                        if (result == Request.Result.success)
                        {
                            Gdx.app.postRunnable(() -> renderOrders(entries));
                        }
                        else
                        {
                            Gdx.app.postRunnable(this::renderFailure);
                        }
                    });

                    break;
                }
            }
        }
    }

    private void setSearchInactive()
    {
        search.clearChildren();
        search.setBackground("form-default");

        SearchField searchField = new SearchField("search-default", result -> {

            Menu.playSound(MenuSound.select);
            hideSortingButton = false;
            giveItemFilterTitle = null;
            giveItemFilter = result.name;
            giveItemPayloadFilter = stripQuality(result.payload);
            refresh(true, true);

            return true;
        });

        search.add(searchField).expand().fill();
    }

    private void renderFailure()
    {
        orders.clearChildren();

        Label error = new Label(L.get("MENU_ERROR_TRY_AGAIN"), BrainOutClient.Skin, "title-red");
        error.setWrap(true);

        orders.add(error).width(256).expand().center().row();
    }

    private void renderOrders(List<MarketService.MarketOrderEntry> entries)
    {
        items = new Table();
        int currentRow = 0;

        for (MarketService.MarketOrderEntry entry : entries)
        {
            boolean selling = entry.takeItem.equals("ru");

            int objPrice = selling ? entry.takeAmount : entry.giveAmount;
            int objAmount = selling ? entry.giveAmount : entry.takeAmount;
            String obj = selling ? entry.giveItem : entry.takeItem;;
            JSONObject objPayload = selling ? entry.givePayload : entry.takePayload;

            if (!menuCategory.equals(MarketUtils.GetMarketItemCategory(obj)))
                continue;

            ConsumableRecord record = MarketUtils.MarketObjectToConsumableRecord(
                obj, objPayload, objAmount);

            if (record == null)
            {
                continue;
            }

            String seller = entry.orderPayload != null ? entry.orderPayload.optString("name", "???") : "???";
            String avatar = entry.orderPayload != null ? entry.orderPayload.optString("avatar", null) : null;

            Image av = new Image();
            if (avatar != null)
            {
                Avatars.Get(avatar, (has, avatar1) ->
                {
                    if (has)
                    {
                        av.setDrawable(new TextureRegionDrawable(avatar1));
                    }
                    else
                    {
                        av.setDrawable(new TextureRegionDrawable(BrainOutClient.getRegion("default-avatar")));
                    }
                });
            }
            else
            {
                av.setDrawable(new TextureRegionDrawable(BrainOutClient.getRegion("default-avatar")));
            }

            {
                Table holder = new Table(BrainOutClient.Skin);

                /*
                holder.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
                        {
                            pushMenu(new RemoteAccountMenu(entry.ownerId, null));
                            return;
                        }

                        accountFilter = entry.ownerId;
                        accountFilterName = seller;
                        refresh(true);
                    }
                });
                 */

                holder.setBackground("form-default");
                holder.add(av).size(32).pad(12);
                items.add(holder).uniformY().fill();
            }

            {
                Table holder = new Table(BrainOutClient.Skin);
                holder.setBackground("form-default");

                Label nameTitle = new Label(seller, BrainOutClient.Skin, "title-small");
                nameTitle.setEllipsis(true);
                nameTitle.setWrap(true);

                Label itemTitle = new Label(getItemText(record), BrainOutClient.Skin, "title-yellow");
                itemTitle.setEllipsis(true);
                itemTitle.setWrap(true);

                Label availableTitle = new Label(L.get("MENU_ORDERS_AVAILABLE",
                    String.valueOf(entry.available * objAmount)),
                    BrainOutClient.Skin, "title-gray");

                long now = System.currentTimeMillis();
                long end = entry.deadline.getTime();
                long secondLeft = (end - now) / 1000;

                String timeLeftText;
                if (secondLeft > 86400)
                {
                    timeLeftText = String.valueOf(secondLeft / 86400) + "d";
                }
                else
                {
                    timeLeftText = DurationUtils.GetDurationString((int)Math.max(secondLeft, 0));
                }

                Label timeLeft = new Label(timeLeftText, BrainOutClient.Skin, "title-gray");

                holder.add(nameTitle).padTop(-4).padLeft(4).expandX().fillX().colspan(2).row();
                holder.add(itemTitle).padLeft(4).expandX().fillX().colspan(2).row();

                holder.add(availableTitle).padBottom(-4).padLeft(4).expandX().left();
                holder.add(timeLeft).padBottom(-4).right();

                items.add(holder).uniformY().expandX().fill();
            }

            boolean useless = false, questRelated = false;

            GameMode gameMode = BrainOutClient.ClientController.getGameMode();

            if (gameMode != null && gameMode.getRealization() instanceof ClientFreeRealization)
            {
                ClientFreeRealization free = ((ClientFreeRealization) gameMode.getRealization());
                useless = free.isItemUseless(record.getItem());
                questRelated = free.isItemQuestRelated(record.getItem());
            }

            {
                Button holder = new Button(BrainOutClient.Skin, "button-notext");

                Table b = new Table();
                holder.add(b).size(192, 64);

                if (record.getItem() instanceof InstrumentConsumableItem)
                {
                    InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());
                    Skin skin = ici.getInstrumentData().getInfo().skin;

                    if (skin != null && skin.hasComponent(IconComponent.class) && skin.getComponent(IconComponent.class).hasIcon("big-icon"))
                    {
                        ContentImage.RenderImage(skin, b, 1);
                    }
                    else if (ici.getInstrumentData().getInstrument().getComponent(IconComponent.class) != null)
                    {
                        ContentImage.RenderImage(ici.getInstrumentData().getInstrument(), b, 1);
                    }
                    else
                    {
                        ContentImage.RenderInstrument(b, ici.getInstrumentData().getInfo());
                    }

                    if (ici.getInstrumentData() instanceof WeaponData)
                    {

                        Gdx.app.postRunnable(() ->
                            Tooltip.RegisterToolTip(holder,
                                new InventoryPanel.InstrumentTooltip(ici.getInstrumentData().getInfo(),
                                    record.getQuality()),
                                    MarketMenu.this));
                    }
                    else
                    {
                        if (useless)
                        {
                            Gdx.app.postRunnable(() ->
                                Tooltip.RegisterStandardToolTip(holder,
                                    ici.getContent().getTitle().get() + " (" + L.get("MENU_USELESS_ITEM") + ")",
                                    L.get("MENU_USELESS_ITEM_DESC"), MarketMenu.this));
                        }
                        else
                        {
                            if (record.hasQuality())
                            {
                                Gdx.app.postRunnable(() ->
                                    Tooltip.RegisterToolTip(holder,
                                        ici.getContent().getTitle().get() +
                                            " (" + L.get("MENU_CONDITION") + " " + record.getQuality() + "%)", MarketMenu.this));
                            }
                            else
                            {
                                Gdx.app.postRunnable(() ->
                                    Tooltip.RegisterToolTip(holder,
                                        ici.getContent().getTitle().get(), MarketMenu.this));
                            }
                        }
                    }
                }
                else if (record.getItem() instanceof RealEstateConsumableItem)
                {
                    ContentImage.RenderImage(record.getItem().getContent(), b, objAmount);

                    Gdx.app.postRunnable(() ->
                        Tooltip.RegisterToolTip(holder,
                            new InventoryPanel.ConsumableItemTooltip(record, false), MarketMenu.this));
                }
                else
                {
                    ContentImage.RenderImage(record.getItem().getContent(), b, objAmount);

                    boolean finalUseless = useless;

                    if (useless || InventoryPanel.ConsumableItemTooltip.Need(record))
                    {
                        Gdx.app.postRunnable(() ->
                            Tooltip.RegisterToolTip(holder,
                                new InventoryPanel.ConsumableItemTooltip(record, finalUseless),
                                MarketMenu.this));
                    }
                    else
                    {
                        Gdx.app.postRunnable(() ->
                            Tooltip.RegisterToolTip(holder,
                                record.getItem().getContent().getTitle().get(), MarketMenu.this));
                    }

                }

                ContentImage.RenderUsesAndAmount(record, holder);


                float y = 42;

                if (questRelated)
                {
                    Image star = new Image(BrainOutClient.Skin, "quest-icon-star");
                    star.setBounds(4, y, 19, 18); y -= 20;
                    star.setTouchable(Touchable.disabled);
                    holder.addActor(star);
                }
                if (useless)
                {
                    Image star = new Image(BrainOutClient.Skin, "useless-item");
                    star.setBounds(4, y, 19, 18);
                    star.setTouchable(Touchable.disabled);
                    holder.addActor(star);
                }

                holder.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        hideSortingButton = true;
                        giveItemFilterTitle = null;
                        giveItemFilter = entry.giveItem;
                        giveItemPayloadFilter = stripQuality(entry.givePayload);
                        refresh(true, true);
                    }
                });

                items.add(holder).uniformY().fill();
            }


            {
                final boolean me = BrainOutClient.ClientController.getMyAccount().equals(entry.ownerId);

                BuyButton buy = new BuyButton(me, objPrice,
                    BrainOutClient.Skin, "button-default");

                buy.setName("buy-button-" + currentRow);

                buy.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        if (me || (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && isMod()))
                        {
                            cancelItem(entry);
                        }
                        else
                        {
                            purchaseItem(entry, record);
                        }
                    }
                });

                items.add(buy).width(128).uniformY().fill();
            }

            items.row();
            currentRow++;
        }

        float originalScrollY = scrollY;

        ordersPane = new ScrollPane(items, BrainOutClient.Skin, "scroll-default")
        {
            @Override
            protected void scrollY(float pixelsY)
            {
                super.scrollY(pixelsY);

                scrollY = pixelsY;

                updateHoveredRow();
            }
        };

        ordersPane.setFadeScrollBars(false);
        ordersPane.setScrollbarsOnTop(false);

        Gdx.app.postRunnable(() -> {
            ordersPane.setScrollY(originalScrollY);
            ordersPane.updateVisualScroll();
        });

        ordersPane.setScrollingDisabled(true, false);
        orders.clearChildren();
        items.align(Align.top);
        orders.add(ordersPane).expand().fill().center().top().row();

        setScrollFocus(ordersPane);
    }

    private void updateHoveredRow()
    {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();

        Vector2 mousePos = items.stageToLocalCoordinates(new Vector2(x, y));

        Vector2 leftTopCorner = ordersPane.localToStageCoordinates(new Vector2(0, ordersPane.getHeight()));

        if (x < leftTopCorner.x || y > leftTopCorner.y) changeHoveredRow(-1);

        else changeHoveredRow(items.getRow(mousePos.y));
    }

    private void changeHoveredRow(int newIndex)
    {
        Actor newHovered = items.findActor("buy-button-" + newIndex);

        if (hoveredBuyButton == newHovered) return;

        if (hoveredBuyButton != null)
        {
            hoveredBuyButton.setDefaultState();
        }

        Actor newButton = items.findActor("buy-button-" + newIndex);
        if (newButton != null)
        {
            BuyButton buyButton = (BuyButton)newButton;
            setBuyButtonToHovered(buyButton);
        }
        else
        {
            hoveredBuyButton = null;
        }
    }

    private void setBuyButtonToHovered(BuyButton button)
    {
        hoveredBuyButton = button;

        if (button.isMe() || (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && isMod()))
        {
            button.setCancelState();
        }
        else
        {
            button.setBuyState();
        }
    }

    private boolean isMod()
    {
        RemoteClient me = BrainOutClient.ClientController.getMyRemoteClient();
        if (me == null)
        {
            return false;
        }

        return me.getRights() == PlayerRights.mod || me.getRights() == PlayerRights.admin;
    }

    private void purchaseItem(MarketService.MarketOrderEntry entry, ConsumableRecord record)
    {
        pushMenu(new FulfillOrderMenu(entry, record, () -> refresh(false), menuCategory));
    }

    private void cancelItem(MarketService.MarketOrderEntry entry)
    {
        pushMenu(new ConfirmationPopup(L.get("MENU_MARKET_CANCEL_ORDER_COMFIRM"))
        {
            @Override
            public void yes()
            {
                Gdx.app.postRunnable(() ->
                {
                    JSONObject args = new JSONObject();

                    args.put("market", "freeplay");
                    args.put("order_id", entry.orderId);
                    args.put("item_name", entry.giveItem);

                    BrainOutClient.SocialController.sendRequest("cancel_market_order", args,
                        new SocialController.RequestCallback()
                    {
                        @Override
                        public void success(JSONObject response)
                        {
                            refresh(false);
                        }

                        @Override
                        public void error(String reason)
                        {
                            pushMenu(new AlertPopup(L.get(reason)));
                        }
                    });
                });
            }
        });
    }

    private String getItemText(ConsumableRecord record)
    {
        if (record.getItem() instanceof InstrumentConsumableItem)
        {
            InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());
            return ici.getInstrumentData().getInstrument().getTitle().get();
        }

        return record.getItem().getContent().getTitle().get();
    }

    private void addRUButton()
    {
        Button ru = new Button(BrainOutClient.Skin, "button-notext");

        ruAvailableIcon = new Table();
        ru.add(ruAvailableIcon).row();

        ruAvailableLabel = new Label("...", BrainOutClient.Skin, "title-small");
        ruAvailableLabel.setAlignment(Align.center);
        ru.add(ruAvailableLabel).expandX().fillX().row();

        ru.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                exchangeRU();
            }
        });

        refreshRuAvailable();

        ru.setBounds(BrainOutClient.getWidth() - 212 - 64, BrainOutClient.getHeight() - 84, 192, 64);
        Tooltip.RegisterStandardToolTip(ru,
            L.get("MENU_MARKET_RU_BALANCE"), L.get("MENU_MARKET_RU_BALANCE_DESC"), this);

        addActor(ru);

        Button ex = new Button(BrainOutClient.Skin, "button-green");
        Image icon = new Image(BrainOutClient.Skin, "icon-exchange-ru");
        icon.setScaling(Scaling.none);
        ex.add(icon).expand().fill();

        ex.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                exchangeRU();
            }
        });

        ex.setBounds(BrainOutClient.getWidth() - 64 - 16, BrainOutClient.getHeight() - 84, 64, 64);

        addActor(ex);
    }

    private void exchangeRU()
    {
        Menu.playSound(MenuSound.select);

        addMoreRU(new TransferRUMenu.Callback()
        {
            @Override
            public void approve(int amount)
            {
                ruAvailable = amount;
                ruAvailableLabel.setText(String.valueOf(amount));
                ruAvailableIcon.clear();
                ContentImage.RenderStatImage("ru", amount, ruAvailableIcon);

                Menu.playSound(MenuSound.itemSold);
            }

            @Override
            public void cancel()
            {
                Menu.playSound(MenuSound.back);
            }
        });
    }

    private void refreshRuAvailable()
    {
        MarketUtils.GetMarketRU(new MarketUtils.GetRUCallback()
        {
            @Override
            public void success(int amount)
            {
                ruAvailable = amount;
                ruAvailableLabel.setText(String.valueOf(amount));
                ruAvailableIcon.clear();
                ContentImage.RenderStatImage("ru", amount, ruAvailableIcon);
            }

            @Override
            public void error()
            {

            }
        });
    }

    private int getProfileRU()
    {
        return BrainOutClient.ClientController.getUserProfile().getInt("ru", 0);
    }

    private void addMoreRU(TransferRUMenu.Callback callback)
    {
        if (ruAvailable == 0 && getProfileRU() == 0)
        {
            pushMenu(new AlertPopup(L.get("MENU_NOT_ENOUGH_RU")));
            return;
        }

        int max;
        boolean in;

        if (ruAvailable == 0)
        {
            max = getProfileRU();
            in = true;
        }
        else
        {
            max = ruAvailable;
            in = false;
        }


        pushMenu(new TransferRUMenu(1, max, in, ruAvailable, getProfileRU(), callback));
    }

    @Override
    public void onFocusIn()
    {
        super.onFocusIn();

        if (ClientConstants.Client.MOUSE_LOCK)
        {
            Gdx.input.setCursorCatched(false);
        }

        refreshRuAvailable();
    }

    @Override
    public void onInit()
    {
        super.onInit();

        {
            TextButton close = new TextButton(L.get("MENU_CLOSE"),
                    BrainOutClient.Skin, "button-default");

            close.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(Menu.MenuSound.back);

                    pop();
                }
            });

            close.setBounds(16, BrainOutClient.getHeight() - 84, 192, 64);
            addActor(close);
        }

        {
            TextButton refresh = new TextButton(L.get("MENU_REFRESH"),
                    BrainOutClient.Skin, "button-default");

            refresh.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    refresh(false);
                }
            });

            refresh.setBounds(BrainOutClient.getWidth() - 64 - 192 * 2 - 32, BrainOutClient.getHeight() - 84, 192, 64);
            addActor(refresh);
        }

        addRUButton();

        refresh(true);
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-clan");
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public boolean escape()
    {
        pop();
        return true;
    }
}
