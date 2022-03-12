package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.ItemStatsComponent;
import com.desertkun.brainout.content.components.QuestOnlyComponent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.RoundLockSafeData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.menu.ui.ColorDrawable;
import com.desertkun.brainout.mode.ClientFreeRealization;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ItemStatsComponent")
@ReflectAlias("data.components.ItemStatsComponentData")
public class ItemStatsComponentData extends ActiveStatsComponentData<ItemStatsComponent>
{
    private static final float DISPLAY_DISTANCE = 10.0f;

    private final ItemData itemData;
    private Label stats;
    private float dst;
    private Table statsBG;
    private ColorDrawable statsBGDrawable;
    private float updateTimer;

    public ItemStatsComponentData(ItemData itemData, ItemStatsComponent contentComponent)
    {
        super(itemData, contentComponent);

        this.itemData = itemData;
        dst = -1;
    }

    @Override
    public void init()
    {
        super.init();

        BrainOutClient.EventMgr.subscribe(Event.ID.updated, this);
    }

    @Override
    public void release()
    {
        super.release();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.updated, this);

        if (statsBGDrawable != null)
        {
            statsBGDrawable.dispose();
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        if (event.getID() == Event.ID.updated)
        {
            updateStats();
        }

        return super.onEvent(event);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        if (dst > 0 && dst < DISPLAY_DISTANCE * DISPLAY_DISTANCE)
        {
            super.render(batch, context);
        }
    }

    private float getPlayerDistance()
    {
        CSGame game = BrainOutClient.ClientController.getState(CSGame.class);

        if (game == null)
            return -1;

        PlayerData playerData = game.getPlayerData();

        if (playerData == null)
            return -1;

        return Vector2.dst2(playerData.getX(), playerData.getY(),
            itemData.getX(), itemData.getY());
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (stats == null)
            return;

        if (!itemData.hasItems() && !getContentComponent().isShowEmpty())
        {
            dst = 0;
            return;
        }

        dst = getPlayerDistance();

        float a = MathUtils.clamp(1.0f - dst / (DISPLAY_DISTANCE * DISPLAY_DISTANCE), 0.f, 1.f);

        stats.getColor().a = a;
        statsBGDrawable.getColor().a = a * 0.5f;

        updateTimer -= dt;
        if (updateTimer < 0)
        {
            updateTimer = 5.0f;
            updateStats();
        }
    }

    private void updateStats()
    {
        stats.setText(getStats());
        stats.setColor(isRed() ? Color.SCARLET : Color.WHITE);
    }

    private boolean isRed()
    {
        if (itemData instanceof RoundLockSafeData && ((RoundLockSafeData) itemData).isLocked())
            return true;

        return !itemData.hasItems();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void initUI(Table ui, float width, float height)
    {
        super.initUI(ui, width, height);

        initStats();
    }

    private int itemCount()
    {
        int itemsCount = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : itemData.getRecords().getData())
        {
            ConsumableItem item = entry.value.getItem();

            if (item.isPrivate() && item.getPrivate() != BrainOutClient.ClientController.getMyId())
            {
                continue;
            }

            Content c = item.getContent();

            {
                QuestOnlyComponent qc = c.getComponent(QuestOnlyComponent.class);
                if (qc != null)
                {
                    GameMode gameMode = BrainOutClient.ClientController.getGameMode();

                    if (gameMode != null && gameMode.getRealization() instanceof ClientFreeRealization)
                    {
                        ClientFreeRealization free = ((ClientFreeRealization) gameMode.getRealization());

                        if (!free.isQuestActive(qc.getQuest()))
                        {
                            continue;
                        }
                    }
                }
            }

            itemsCount++;
        }

        return itemsCount;
    }

    private String getStats()
    {
        ClientItemComponentData ci = getComponentObject().getComponent(ClientItemComponentData.class);
        if (ci != null && ci.isDiscover())
        {
            return L.get(itemData.hasItems() ? "MENU_OPEN" : "MENU_WEAPON_UI_EMPTY");
        }

        if (itemData instanceof RoundLockSafeData && ((RoundLockSafeData) itemData).isLocked())
            return L.get("MENU_DOOR_LOCKED_UP");

        /*
        if (itemData.getRecords().size() == 1)
        {
            ConsumableRecord record = itemData.getRecords().getByIndex(0);
            ConsumableItem item = record.getItem();

            String title = item.getContent().getTitle().get();

            if (title.length() > 16)
            {
                title = title.substring(0, 13) + "...";
            }

            return title;
        }
        */

        int itemsCount = itemCount();

        if (itemsCount == 0)
            return L.get("MENU_WEAPON_UI_EMPTY");

        return L.get("MENU_ITEM_STATS", String.valueOf(itemsCount));
    }

    protected void initStats()
    {
        super.initStats();

        this.statsBGDrawable = new ColorDrawable(0, 0, 0, 0);

        this.statsBG = new Table(BrainOutClient.Skin);
        statsBG.setBackground(statsBGDrawable);
        statsBG.setRound(false);

        this.stats = new Label(getStats(), BrainOutClient.Skin, "title-ingame-item-stats");
        stats.setColor(itemData.getRecords().size() == 0 ? Color.SCARLET : Color.WHITE);
        stats.setEllipsis(true);
        stats.setWrap(false);

        statsBG.add(stats).expand().fill().row();

        getUi().add(statsBG).height(1f).expandX().fillX().row();
        stats.setFontScale(1.0f / ClientConstants.Graphics.RES_SIZE);
        stats.setAlignment(Align.center);
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
