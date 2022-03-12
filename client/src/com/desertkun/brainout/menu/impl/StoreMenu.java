package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.content.gamecase.Case;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.managers.LocalizationManager;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.ConfirmationPopup;
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.online.IAP;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.DurationUtils;
import org.anthillplatform.runtime.services.StoreService;
import org.anthillplatform.runtime.services.StoreService.Store;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StoreMenu extends Menu
{
    private static DecimalFormat PriceFormat = new DecimalFormat("#.##");
    private static DateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static DateFormat TimeFormat = new SimpleDateFormat("HH:mm", Locale.US);

    static
    {
        DateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        TimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private Table content;
    private Date offerDate = null;
    private float offerDateTimer = 0;
    private RichLabel offer;
    private final Case focusCase;
    private Table focusTable;
    private Table stats;
    private ScrollPane pane;
    private Table items;
    private ObjectMap<String, Table> focusTables;

    public StoreMenu()
    {
        this(null);
    }

    public StoreMenu(Case focusCase)
    {
        this.focusCase = focusCase;
        this.focusTables = new ObjectMap<>();
    }


    protected TextButton addStat(String value, String icon, float pad, float width)
    {
        TextButton btn = new TextButton("", BrainOutClient.Skin, "button-default");

        Table border = new Table();
        border.align(Align.bottom);
        border.setFillParent(true);
        border.setTouchable(Touchable.disabled);

        btn.addActor(border);

        Image statIcon = new Image(BrainOutClient.getRegion(icon));
        statIcon.setScaling(Scaling.none);

        Label statValue = new Label(value, BrainOutClient.Skin, "title-small");
        statValue.setAlignment(Align.center);

        border.add(statIcon).expandY().fillY().padTop(pad).row();
        border.add(statValue).expandX().padTop(2).padBottom(4).fillX().row();

        btn.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(Menu.MenuSound.select);
            }
        });

        stats.add(btn).size(width, 64);

        return btn;
    }

    @Override
    public void pop()
    {
        focusTables.clear();

        super.pop();
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();
        data.align(Align.center);

        this.content = new Table();
        this.content.align(Align.center);

        data.add(content).pad(16).expandX().fillX().row();

        return data;
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-ingame");
    }

    @Override
    public void initTable()
    {
        super.initTable();

        if (BrainOutClient.Env.hasProbabilitiesMenu())
        {
            Table leftButtons = MenuHelper.AddLeftButtonsContainers(this);
            TextButton btn = new TextButton(L.get("MENU_PROBABILITIES"), BrainOutClient.Skin, "button-default");

            btn.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    BrainOutClient.Env.openProbabilitiesMenu(getGameState());
                }
            });

            leftButtons.add(btn).size(192, 64).padRight(10);

            stats = new Table();
            leftButtons.add(stats);
        }

        MenuHelper.AddCloseButton(this, this::pop);

        renderLoading();

        if (!BrainOut.OnlineEnabled())
        {
            Gdx.app.postRunnable(new Runnable()
            {
                @Override
                public void run()
                {
                    StoreService fake = new StoreService(null, "");
                    Store store = fake.new Store("fake");
                    store.parse(new JSONObject(Gdx.files.local("store.json").readString("UTF-8")));
                    renderStore(store);
                }
            });
            return;
        }

        IAP.GetStore(BrainOutClient.Env.getStoreName(), new IAP.StoreCallback()
        {
            @Override
            public void succeed(Store store)
            {
                Gdx.app.postRunnable(() -> renderStore(store));
            }

            @Override
            public void failed()
            {
                Gdx.app.postRunnable(() -> renderError());
            }
        });
    }

    protected void userProfileUpdated()
    {
        Gdx.app.postRunnable(this::updateStats);
    }

    private void updateStats()
    {
        if (stats == null)
            return;

        stats.clear();

        int goldPieces = BrainOutClient.ClientController.getUserProfile().getInt("ch", 0);
        addStat(String.valueOf(goldPieces), "icon-big-gold", -8, 68);
    }

    private static String GetDefaultCurrency()
    {
        return BrainOutClient.Env.getDefaultCurrency();
    }

    public class MenuStoreItem
    {
        private final Store.Item item;
        private int position;

        private int amount = 1;
        private Label amountTitle;
        private TextButton buyButton;
        private double totalPrice;
        private boolean twice;

        public MenuStoreItem(Store.Item item)
        {
            this.item = item;

            JSONObject payload = item.getPublicPayload();

            Store.Campaign.CampaignItem campaignItem = item.getCampaignItem();
            if (campaignItem != null)
            {
                payload = campaignItem.getUpdatedPublicPayload();
            }

            if (payload != null)
            {
                this.position = payload.optInt("position", 999);
                this.twice = payload.optBoolean("wide", true);
            }
            else
            {
                this.position = 999;
                this.twice = true;
            }
        }

        private Store.Tier.Price getCampaignPrice()
        {
            Store.Campaign.CampaignItem campaignItem = item.getCampaignItem();

            if (campaignItem == null)
                return null;

            Store.Tier tier = campaignItem.getUpdatedTier();

            return tier.getPrices().getOrDefault(getCurrency(), tier.getPrices().get(GetDefaultCurrency()));
        }

        private Store.Tier.Price getPrice()
        {
            Store.Item.Billing billing = item.getBilling();

            return billing.getTier().getPrices().getOrDefault(
                getCurrency(),
                billing.getTier().getPrices().get(GetDefaultCurrency()));
        }

        public boolean checkLimit(String func, float a, float b)
        {
            switch (func)
            {
                case "=":
                    return a == b;
                case ">":
                    return a > b;
                case "<":
                    return a < b;
                case ">=":
                    return a >= b;
                case "<=":
                    return a <= b;
                case "!=":
                    return a != b;
                default:
                    return false;
            }
        }

        public Table render()
        {
            JSONObject payload = item.getPublicPayload();

            Store.Campaign.CampaignItem campaignItem = item.getCampaignItem();
            if (campaignItem != null)
            {
                payload = campaignItem.getUpdatedPublicPayload();
            }

            boolean onlyOne = payload.optBoolean("only-one", false);

            String limitVersion = payload.optString("limit-version", "");

            if (!limitVersion.isEmpty())
            {
                if (!Objects.equals(limitVersion, Version.VERSION))
                {
                    return null;
                }
            }

            int limitBuild = payload.optInt("limit-build", 0);

            if (limitBuild != 0)
            {
                if (limitBuild > Constants.Version.BUILD)
                {
                    return null;
                }
            }

            JSONArray limitStats = payload.optJSONArray("limit-stats");

            UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

            if (limitStats != null)
            {
                for (int i = 0, t = limitStats.length(); i < t; i++)
                {
                    JSONObject statLimit = limitStats.optJSONObject(i);

                    if (statLimit != null)
                    {
                        String id = statLimit.optString("id");
                        String func = statLimit.optString("func");
                        float value = (float)statLimit.optDouble("value", 0);

                        if (id != null && func != null)
                        {
                            float statValue = userProfile.getStats().get(id, 0.0f);

                            if (!checkLimit(func, statValue, value))
                            {
                                return null;
                            }
                        }
                    }
                }
            }

            Table itemTable = new Table();
            focusTables.put(item.getId(), itemTable);

            boolean focusThisOne = false;

            JSONArray limitItems = payload.optJSONArray("limit-items");

            if (limitItems != null)
            {
                for (int i = 0, t = limitItems.length(); i < t; i++)
                {
                    JSONObject itemLimit = limitItems.optJSONObject(i);

                    if (itemLimit != null)
                    {
                        String id = itemLimit.optString("id");
                        String func = itemLimit.optString("func");
                        float value = (float)itemLimit.optDouble("value", 0);

                        if (id != null && func != null)
                        {
                            if (focusCase != null && id.equals(focusCase.getID()))
                            {
                                focusThisOne = true;
                            }

                            int statValue = userProfile.getItems().get(id, 0);

                            if (!checkLimit(func, statValue, value))
                            {
                                return null;
                            }
                        }
                    }
                }
            }

            Store.Tier.Price price = getPrice();

            if (price == null)
            {
                return null;
            }

            JSONObject limitTo = payload.optJSONObject("limit-date-to");

            if (limitTo != null)
            {
                boolean enabled = limitTo.optBoolean("enable", false);

                if (enabled)
                {
                    String date = limitTo.optString("date");
                    String time = limitTo.optString("time");

                    if (date != null && time != null)
                    {
                        try
                        {
                            Date date_ = DateFormat.parse(date);
                            Date time_ = TimeFormat.parse(time);

                            long tm = date_.getTime() + time_.getTime();
                            offerDate = new Date(tm);

                        }
                        catch (ParseException e)
                        {
                            //
                        }
                    }
                }
            }

            boolean specialOffer = payload.optBoolean("special-offer", false);
            boolean showDescription = payload.optBoolean("show-description", false);
            String descriptionColor = payload.optString("description-bg-color", "blue");
            int discount = payload.optInt("discount", 0);
            boolean boostPrice = true;
            boolean strike = discount >= 75;

            if (discount == 0 && onlyOne)
            {
                Store.Tier.Price campaignPrice = getCampaignPrice();

                if (campaignPrice != null)
                {
                    float oldPrice = price.getPrice();
                    float newPrice = campaignPrice.getPrice();

                    if (newPrice < oldPrice)
                    {
                        float diff = oldPrice - newPrice;
                        float d = (diff / oldPrice) * 100.0f;
                        discount = (int)d;
                        boostPrice = false;
                        strike = true;
                    }
                }
            }

            itemTable.align(Align.top);
            itemTable.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));

            if (payload.has("image"))
            {
                String imagePath = payload.optString("image", "");
                TextureRegion imageRegion = BrainOutClient.getRegion(imagePath);

                if (imageRegion != null)
                {
                    Table image = new Table();
                    image.align(Align.center | Align.top);
                    image.setBackground(new TextureRegionDrawable(imageRegion));
                    image.setTouchable(Touchable.enabled);

                    if (payload.has("title") && !showDescription)
                    {
                        JSONObject titleValue = payload.optJSONObject("title");

                        if (titleValue != null)
                        {
                            String titleString = titleValue.optString(
                                    BrainOutClient.LocalizationMgr.getCurrentLanguage(), null);

                            if (titleString == null)
                            {
                                titleString = titleValue.optString(LocalizationManager.GetDefaultLanguage(), null);
                            }

                            if (titleString != null)
                            {
                                Label title = new Label(titleString, BrainOutClient.Skin, "title-small");
                                title.setAlignment(Align.center);
                                title.setWrap(true);
                                image.add(title).pad(16).width(200).row();

                            }
                        }
                    }

                    if (strike)
                    {
                        Label offerLabel = new Label(
                            L.get(discount >= 75 ? "MENU_SUPER_DISCOUNT" : "MENU_DISCOUNT",
                                    String.valueOf(discount)),
                                BrainOutClient.Skin, "title-yellow"
                        );
                        offerLabel.setAlignment(Align.center);

                        BorderActor offerBorder = new BorderActor(offerLabel,
                                discount >= 75 ? "form-magenta" : "form-red");
                        offerBorder.getCell().expandX().fillX();

                        image.add(offerBorder).expand().fillX().bottom().row();
                    }
                    else if (showDescription)
                    {
                        JSONObject descriptionValue = payload.optJSONObject("description");

                        if (descriptionValue != null)
                        {
                            String descriptionString = descriptionValue.optString(
                                    BrainOutClient.LocalizationMgr.getCurrentLanguage(), null);

                            if (descriptionString == null)
                            {
                                descriptionString = descriptionValue.optString(LocalizationManager.GetDefaultLanguage(), null);
                            }

                            if (descriptionString != null)
                            {
                                Label offerLabel = new Label(descriptionString, BrainOutClient.Skin, "title-small");

                                offerLabel.setAlignment(Align.center);

                                BorderActor offerBorder = new BorderActor(offerLabel, "form-" + descriptionColor);
                                offerBorder.getCell().expandX().fillX();

                                image.add(offerBorder).expand().fillX().bottom().row();
                            }
                        }
                    }
                    else if (specialOffer)
                    {
                        Label offerLabel = new Label(L.get("MENU_SPECIAL_OFFER"), BrainOutClient.Skin, "title-yellow");
                        offerLabel.setAlignment(Align.center);

                        BorderActor offerBorder = new BorderActor(offerLabel, "form-red");
                        offerBorder.getCell().expandX().fillX();

                        image.add(offerBorder).expand().fillX().bottom().row();
                    }

                    if (payload.has("tooltip-image") && payload.has("tooltip-text"))
                    {
                        String tooltipImage = payload.getString("tooltip-image");
                        TextureRegion tooltipRegion = BrainOutClient.getRegion(tooltipImage);

                        String tooltipText = L.get(payload.getString("tooltip-text"));

                        if (tooltipRegion != null)
                        {
                            com.desertkun.brainout.menu.ui.Tooltip.RegisterToolTip(image, () ->
                            {
                                Table tooltip = new Tooltip.TooltipTable();
                                tooltip.setBackground(new TextureRegionDrawable(tooltipRegion));
                                tooltip.align(Align.bottom);

                                RichLabel title = new RichLabel(tooltipText, BrainOutClient.Skin, "title-small");
                                tooltip.add(title).expandX().fillX().center().bottom().pad(8);
                                tooltip.setSize(tooltipRegion.getRegionWidth(), tooltipRegion.getRegionHeight());

                                return tooltip;
                            }, StoreMenu.this);
                        }
                    }

                    itemTable.add(new BorderActor(image, "form-fit")).pad(10).row();
                }
            }

            Table bottomRow = new Table();
            itemTable.add(bottomRow).expand().fill().pad(0, 10, 10, 10).row();

            calculatePrice();

            boolean hasOldPrice = false;

            if (strike)
            {
                Table oldPrice = new Table();
                oldPrice.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-gray")));

                double oldPriceValue = boostPrice ? totalPrice / ((100.0d - discount) / 100.0d) :
                        (double)price.getPrice() / 100.0d;

                String v;

                if (item.getPublicPayload().has("offline-price"))
                {
                    v = String.valueOf((int)oldPriceValue);
                }
                else
                {
                    v = price.getFormat().replace("{0}", PriceFormat.format(oldPriceValue));
                }

                Label oldTitle = new Label(v,
                    BrainOutClient.Skin,
                    "title-gray");

                oldTitle.setAlignment(Align.center);
                oldTitle.setFillParent(true);

                oldPrice.addActor(oldTitle);

                Image stroke = new Image(BrainOutClient.getRegion("stroke-ui"));
                stroke.setScaling(Scaling.none);
                stroke.setFillParent(true);
                oldPrice.addActor(stroke);

                hasOldPrice = true;

                bottomRow.add(oldPrice).size(128, 48).expandX().right();
            }

            this.buyButton = new TextButton("", BrainOutClient.Skin, "button-green");

            buyButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    purchase();
                }
            });

            Cell buyCell = bottomRow.add(buyButton).size(128, 48).expandX().left();

            Table quantity = new Table();

            TextButton minus = new TextButton("-", BrainOutClient.Skin, "button-fill");
            this.amountTitle = new Label(String.valueOf(amount), BrainOutClient.Skin, "title-small");
            amountTitle.setAlignment(Align.center);
            TextButton plus = new TextButton("+", BrainOutClient.Skin, "button-fill");

            quantity.add(minus).width(24).padLeft(-2).expandY().fillY();
            quantity.add(amountTitle).expand().fill();
            quantity.add(plus).width(24).padRight(-2).expandY().fillY();

            plus.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                Menu.playSound(MenuSound.select);
                addOne();
                }
            });

            minus.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                Menu.playSound(MenuSound.select);
                subtractOne();
                }
            });

            update();

            if (onlyOne)
            {
                if (!hasOldPrice)
                {
                    buyCell.center();
                }
            }
            else
            {
                BorderActor border = new BorderActor(quantity, "form-fit");
                border.getCell().expand().fill();

                bottomRow.add(border).size(96, 24).padRight(12).row();
            }

            if (focusThisOne)
            {
                focusTable = itemTable;
            }

            return itemTable;
        }

        private void purchase()
        {
            if (!BrainOutClient.Env.storeEnabled())
                return;

            JSONObject offlinePrice = item.getPublicPayload().optJSONObject("offline-price");
            if (offlinePrice != null)
            {
                purchaseOffline();
                return;
            }

            Store.Tier.Price price = getPrice();

            if (price == null)
            {
                return;
            }

            String component = BrainOutClient.Env.getStoreComponent();
            String store = item.getStore().getName();
            String currency = price.getCurrency();
            String itemName = item.getId();

            HashMap<String, String> env = new HashMap<>();
            BrainOutClient.Env.getStoreEnvironment(env);

            BrainOutClient.ClientController.createNewOrder(
                store, itemName, amount, currency, component, env);

            pushMenu(new NewOrderResultMenu("")
            {
                @Override
                protected void userProfileUpdated()
                {
                    StoreMenu.this.userProfileUpdated();
                }
            });
        }

        private void purchaseOffline()
        {
            JSONObject offlinePrice = item.getPublicPayload().optJSONObject("offline-price");
            if (offlinePrice == null)
                return;

            String currency = offlinePrice.getString("currency");

            if (BrainOutClient.ClientController.getUserProfile().getStats().get(currency, 0.0f) < totalPrice)
            {
                Menu.playSound(MenuSound.denied);

                switch (currency)
                {
                    case "ch":
                    {
                        pushMenu(new ConfirmationPopup(L.get("MENU_NOT_ENOUGH_GOLD_BARS"))
                        {
                            @Override
                            public void yes()
                            {
                                Gdx.app.postRunnable(() ->
                                {
                                    Table focusTable_ = focusTables.get("item-gold-3");

                                    if (focusTable_ == null)
                                        return;

                                    pane.scrollTo(0,
                                        focusTable_.getY(),
                                        items.getWidth(),
                                        focusTable_.getHeight(),
                                        true, true);
                                });
                            }
                        });

                        break;
                    }
                }

                return;
            }

            JSONObject args = new JSONObject();
            args.put("id", item.getId());
            args.put("amount", amount);

            WaitLoadingMenu loadingMenu = new WaitLoadingMenu("");
            pushMenu(loadingMenu);

            BrainOutClient.SocialController.sendRequest("purchase_offline_item", args,
                new SocialController.RequestCallback()
            {
                @Override
                public void success(JSONObject response)
                {
                    GameState gs = getGameState();

                    if (gs == null)
                        return;

                    loadingMenu.pop();

                    reset();
                }

                @Override
                public void error(String reason)
                {
                    loadingMenu.pop();
                    pushMenu(new AlertPopup(L.get("MENU_PURCHASE_ERROR")));
                }
            });
        }

        private void subtractOne()
        {
            if (amount == 1)
                return;

            amount--;
            update();
        }

        private void addOne()
        {
            amount++;
            update();
        }

        private void calculatePrice()
        {
            if (item.getPublicPayload().has("offline-price"))
            {
                JSONObject offlinePrice = item.getPublicPayload().optJSONObject("offline-price");

                if (offlinePrice != null && offlinePrice.has("amount"))
                {
                    int priceValue = offlinePrice.getInt("amount");
                    totalPrice = priceValue * amount;
                    return;
                }
            }

            Store.Item.Billing billing = item.getBilling();

            Store.Tier.Price price =
                    billing.getTier().getPrices().getOrDefault(
                            getCurrency(),
                            billing.getTier().getPrices().get(GetDefaultCurrency()));

            if (price == null)
            {
                return;
            }

            int priceValue = price.getPrice();

            Store.Tier.Price campaignPrice = getCampaignPrice();

            if (campaignPrice != null)
            {
                priceValue = campaignPrice.getPrice();
            }

            int totalPriceInCents = priceValue * amount;
            totalPrice = (double) totalPriceInCents / 100.d;
        }

        private void update()
        {
            Store.Item.Billing billing = item.getBilling();

            Store.Tier.Price price =
                billing.getTier().getPrices().getOrDefault(
                    getCurrency(),
                    billing.getTier().getPrices().get(GetDefaultCurrency()));

            if (price == null)
            {
                return;
            }

            calculatePrice();

            amountTitle.setText(String.valueOf(amount));

            if (item.getPublicPayload().has("offline-price"))
            {
                String currency = item.getPublicPayload().getJSONObject("offline-price").getString("currency");

                String icon;

                switch (currency)
                {
                    case "ch":
                    default:
                    {
                        icon = "icon-small-gold";
                    }
                }

                Image image = new Image(BrainOutClient.Skin, icon);

                buyButton.setText("");
                buyButton.clearChildren();

                Label l = new Label(String.valueOf((int)totalPrice), BrainOutClient.Skin, "title-small");
                buyButton.add(l).padRight(4);
                buyButton.add(image);
            }
            else
            {
                String v = price.getFormat().replace("{0}",  PriceFormat.format(totalPrice));
                buyButton.setText(v);
            }

        }

        public boolean isTwiceAsWide()
        {
            return twice;
        }
    }

    private String getCurrency()
    {
        return BrainOutClient.Env.getUserCurrency();
    }

    private void renderStore(Store store)
    {
        content.clear();

        items = new Table();

        {
            Label title = new Label(L.get("MENU_STORE_TITLE"), BrainOutClient.Skin,
                    "title-yellow");
            title.setAlignment(Align.center);

            items.add(title).colspan(2).fillX().pad(16).row();
        }

        {
            Label title = new Label(L.get("MENU_STORE_DESC"), BrainOutClient.Skin,
                    "title-small");
            title.setAlignment(Align.center);

            items.add(title).colspan(2).expandX().fillX().pad(16).row();
        }

        Array<MenuStoreItem> storeItems = new Array<>();

        for (Store.Item item : store.getItems())
        {
            storeItems.add(new MenuStoreItem(item));
        }

        storeItems.sort((o1, o2) -> (int)Math.signum(o1.position - o2.position));

        int cnt = 0;

        for (MenuStoreItem item : storeItems)
        {
            Table itemTable = item.render();

            if (itemTable == null)
                continue;

            if (item.isTwiceAsWide())
            {
                items.add(itemTable).pad(8).colspan(2).row();
            }
            else
            {
                cnt++;

                Cell<Table> cell = items.add(itemTable).pad(8);

                switch (cnt)
                {
                    case 1:
                    {
                        cell.right().padRight(15);
                        break;
                    }
                    case 2:
                    default:
                    {
                        cnt = 0;

                        cell.left().padLeft(15);
                        cell.row();
                        break;
                    }
                }

            }

        }

        pane = new ScrollPane(items, BrainOutClient.Skin, "scroll-default");
        pane.setScrollingDisabled(true, false);
        pane.setFadeScrollBars(false);

        Gdx.app.postRunnable(() -> setScrollFocus(pane));

        content.add(pane).expand().fill().row();

        if (focusTable != null)
        {
            Gdx.app.postRunnable(() ->
            {
                Table focusTable_ = focusTable;

                pane.scrollTo(0, focusTable_.getY(), items.getWidth(), focusTable_.getHeight(), true, true);
            });
        }

        if (offerDate != null)
        {
            offer = new RichLabel(L.get("MENU_SPECIAL_OFFER_END_IN",
                DurationUtils.GetDurationString(getOfferDateEnd())),
                BrainOutClient.Skin, "title-small");

            content.add(new BorderActor(offer, "form-red")).padTop(32).expandX().fillX().row();
        }

        updateStats();
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        if (offerDate != null)
        {
            offerDateTimer += delta;

            if (offerDateTimer > 1.0f)
            {
                offerDateTimer = 0;

                updateOfferDate();
            }
        }
    }

    private void updateOfferDate()
    {
        if (offer == null)
            return;

        offer.update(L.get("MENU_SPECIAL_OFFER_END_IN",
            DurationUtils.GetDurationString(getOfferDateEnd())),
            BrainOutClient.Skin, "title-small");
    }

    private int getOfferDateEnd()
    {
        long now = System.currentTimeMillis();
        long end = offerDate.getTime();
        long left = (end - now) / 1000;;
        return (int)Math.max(left, 0);
    }

    private void renderError()
    {
        content.clear();

        Label loading = new Label(L.get("MENU_ONLINE_COMMON_ERROR"),
                BrainOutClient.Skin, "title-messages-red");
        loading.setAlignment(Align.center);
        content.add(loading).pad(8).expand().fill().row();
    }

    private void renderLoading()
    {
        content.clear();

        Label loading = new Label(L.get("MENU_LOADING"),
                BrainOutClient.Skin, "title-gray");
        loading.setAlignment(Align.center);
        content.add(loading).pad(8).expand().fill().row();
    }
}
