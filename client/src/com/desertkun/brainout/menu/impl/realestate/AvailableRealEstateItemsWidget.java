package com.desertkun.brainout.menu.impl.realestate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.active.RealEstateItem;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.SpriteWithBlocksComponent;
import com.desertkun.brainout.content.consumable.impl.RealEstateItemConsumableItem;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.InventoryPanel;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.utils.MarketUtils;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.MarketService;
import org.json.JSONObject;

public class AvailableRealEstateItemsWidget extends Table
{
    private final DragAndDrop dropInto;
    private final RealEstateEditMenu menu;
    private final String dimension;
    private Queue<DragAndDrop.Source> sources;
    private Table panel;
    private static String currentTag = "CUSTOM_WALL";
    private Table objectsTable;

    public static final float TOTAL_WIDTH = 212;
    private ScrollPane pane;

    public AvailableRealEstateItemsWidget(String dimension, DragAndDrop dropInto, RealEstateEditMenu menu)
    {
        this.dimension = dimension;
        this.dropInto = dropInto;
        this.sources = new Queue<>();
        this.menu = menu;

        Group panelWidget = new Group();

        panel = new Table(BrainOutClient.Skin);
        renderPanel(panel, menu);
        panel.setFillParent(true);
        panelWidget.addActor(panel);

        add(panelWidget).width(TOTAL_WIDTH).expandY().fillY();

        panel.setX(TOTAL_WIDTH);
        panel.addAction(Actions.moveTo(0, 0, 0.25f, Interpolation.pow2Out));

        setTouchable(Touchable.childrenOnly);
    }

    public interface UpdateOffsetCallback
    {
        void update(float x, float y);
    }

    public class RealEstateItemPayload extends DragAndDrop.Payload
    {
        private final float offsetX;
        private final float offsetY;
        private final Queue<UpdateOffsetCallback> offsetCallbacks;
        public String rsItem;
        public JSONObject rsPayload;

        public RealEstateItemPayload(float offsetX, float offsetY, String rsItem, JSONObject rsPayload)
        {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.rsItem = rsItem;
            this.rsPayload = rsPayload;

            offsetCallbacks = new Queue<>();
        }

        public JSONObject getRsPayload()
        {
            return rsPayload;
        }

        public String getRsItem()
        {
            return rsItem;
        }

        public float getOffsetY()
        {
            return offsetY;
        }

        public float getOffsetX()
        {
            return offsetX;
        }

        public void updateOffset(float x, float y)
        {
            for (UpdateOffsetCallback callback : offsetCallbacks)
            {
                callback.update(x, y);
            }
        }

        public void addOffsetCallback(UpdateOffsetCallback offsetCallback)
        {
            this.offsetCallbacks.addLast(offsetCallback);
        }
    }

    private void clearSources()
    {
        for (DragAndDrop.Source source : sources)
        {
            dropInto.removeSource(source);
        }

        sources.clear();
    }

    private void renderPanel(Table panel, Menu menu)
    {
        {
            Table holder = new Table(BrainOutClient.Skin);
            holder.setBackground("edit-console");

            objectsTable = new Table(BrainOutClient.Skin);
            objectsTable.align(Align.top | Align.left);

            renderObjects();

            pane = new ScrollPane(objectsTable, BrainOutClient.Skin, "scroll-default");
            pane.setFadeScrollBars(false);
            pane.setScrollingDisabled(true, false);

            holder.add(pane).expand().fill().pad(2);
            panel.add(holder).expand().fill().row();
        }
    }

    private void fail()
    {
        Menu.playSound(Menu.MenuSound.denied);
    }

    public void refresh()
    {
        renderObjects();
    }

    private void renderObjects()
    {
        clearSources();
        objectsTable.clearChildren();

        MarketService marketService = MarketService.Get();
        LoginService loginService = LoginService.Get();
        
        marketService.getMarketItems("freeplay", loginService.getCurrentAccessToken(), 
            (request, result, entries) ->
        {
            Array<MarketService.MarketItemEntry> e = new Array<>();
            
            for (MarketService.MarketItemEntry entry : entries)
            {
                if (!"rsitem".equals(entry.name))
                {
                    continue;
                }
                
                e.add(entry);
            }
            
            Gdx.app.postRunnable(() ->
            {
                if (result != Request.Result.success)
                {
                    Label noItems = new Label(L.get("MENU_ERROR_TRY_AGAIN"), BrainOutClient.Skin, "title-red");
                    noItems.setWrap(true);
                    noItems.setAlignment(Align.center | Align.top);
                    objectsTable.add(noItems).expand().fill();
                    return;
                }
                
                renderMarketItems(e);
            });
        });
    }

    private void renderMarketItems(Array<MarketService.MarketItemEntry> items)
    {
        if (items.size == 0)
        {
            Label noItems = new Label(L.get("MENU_REAL_ESTATE_NO_ITEMS"), BrainOutClient.Skin, "title-gray");
            noItems.setWrap(true);
            noItems.setAlignment(Align.center | Align.top);
            objectsTable.add(noItems).expand().fill();
            return;
        }

        for (MarketService.MarketItemEntry entry : items)
        {
            ConsumableRecord record = MarketUtils.MarketObjectToConsumableRecord(
                entry.name, entry.payload, entry.amount);
            
            if (record == null)
                continue;
            
            if (!(record.getItem() instanceof RealEstateItemConsumableItem))
                continue;

            RealEstateItemConsumableItem eici = ((RealEstateItemConsumableItem) record.getItem());
            RealEstateItem rsContent = eici.getContent();

            SpriteWithBlocksComponent sprite = rsContent.getComponent(SpriteWithBlocksComponent.class);

            if (sprite == null)
                continue;

            Button button = new Button(BrainOutClient.Skin, "button-notext");

            if (entry.amount > 1)
            {
                Label a = new Label(String.valueOf(entry.amount), BrainOutClient.Skin, "title-yellow");
                a.setTouchable(Touchable.disabled);
                a.setFillParent(true);
                a.setAlignment(Align.right | Align.bottom);
                button.addActor(a);
            }

            button.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(Menu.MenuSound.select);
                }
            });

            Gdx.app.postRunnable(() ->
                Tooltip.RegisterToolTip(button,
                    new InventoryPanel.ConsumableItemTooltip(record, false), menu));

            IconComponent iconComponent = rsContent.getComponent(IconComponent.class);

            if (iconComponent != null)
            {
                Image icon = new Image(BrainOutClient.Skin, iconComponent.getIconName("icon", null));
                button.add(icon);
            }
            else
            {
                Group ee = renderSprite(sprite, 1.0f);

                button.add(ee).size(sprite.getWidth() * Constants.Graphics.BLOCK_SIZE,
                        sprite.getHeight() * Constants.Graphics.BLOCK_SIZE);
            }

            DragAndDrop.Source source = new DragAndDrop.Source(button)
            {
                @Override
                public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target)
                {
                    if (target == null)
                    {
                        fail();
                    }
                }

                @Override
                public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer)
                {
                    Group object = new Group();
                    object.setTouchable(Touchable.disabled);
                    Group invalid = new Group();
                    invalid.setTouchable(Touchable.disabled);
                    Group valid = new Group();
                    valid.setTouchable(Touchable.disabled);

                    float scale = 1.0f / Map.GetWatcher().getScale();

                    float offsetX = - sprite.getWidth() * scale * Constants.Graphics.BLOCK_SIZE * 0.5f + Constants.Graphics.BLOCK_SIZE * 0.5f,
                            offsetY = - sprite.getHeight() * scale * Constants.Graphics.BLOCK_SIZE * 0.5f + Constants.Graphics.BLOCK_SIZE * 0.5f;

                    RealEstateItemPayload payload = new RealEstateItemPayload(offsetX, offsetY,
                        entry.name, entry.payload);

                    {
                        Group entry = renderSprite(sprite, scale);
                        entry.setTouchable(Touchable.disabled);
                        entry.setPosition(offsetX, offsetY);
                        object.addActor(entry);
                    }
                    {
                        Image border = new Image(BrainOutClient.Skin, "form-drag-good");

                        border.setBounds(
                                offsetX - 2,
                                offsetY - 2,
                                sprite.getWidth() * scale * Constants.Graphics.BLOCK_SIZE + 4,
                                sprite.getHeight() * scale * Constants.Graphics.BLOCK_SIZE + 4
                        );

                        valid.addActor(border);

                        Group entry = renderSprite(sprite, scale);
                        entry.setTouchable(Touchable.disabled);
                        entry.setPosition(offsetX, offsetY);
                        valid.addActor(entry);

                        payload.addOffsetCallback((x1, y1) ->
                        {
                            border.setPosition(offsetX - 2 + x1, offsetY - 2 + y1);
                            entry.setPosition(offsetX + x1, offsetY + y1);
                        });
                    }
                    {
                        Image border = new Image(BrainOutClient.Skin, "form-border-red");

                        border.setBounds(
                                offsetX - 2,
                                offsetY - 2,
                                sprite.getWidth() * scale * Constants.Graphics.BLOCK_SIZE + 4,
                                sprite.getHeight() * scale * Constants.Graphics.BLOCK_SIZE + 4
                        );

                        invalid.addActor(border);

                        Group entry = renderSprite(sprite, scale);
                        entry.setTouchable(Touchable.disabled);
                        entry.setPosition(offsetX, offsetY);
                        invalid.addActor(entry);

                        payload.addOffsetCallback((x1, y1) ->
                        {
                            border.setPosition(offsetX - 2 + x1, offsetY - 2 + y1);
                            entry.setPosition(offsetX + x1, offsetY + y1);
                        });
                    }

                    payload.setDragActor(object);
                    payload.setObject(sprite);
                    payload.setValidDragActor(valid);
                    payload.setInvalidDragActor(invalid);

                    return payload;
                }
            };

            dropInto.addSource(source);
            objectsTable.add(button).expandX().fillX().pad(2).uniformY().fillY().row();
        }
    }

    private Group renderSprite(SpriteWithBlocksComponent sprite, float scale)
    {
        Group entry = new Group();

        for (SpriteWithBlocksComponent.SpriteImage spriteImage : sprite.getImages())
        {
            Image image = new Image(BrainOutClient.Skin, spriteImage.getImage());
            image.setTouchable(Touchable.disabled);
            image.setBounds(
                    spriteImage.getX() * scale * Constants.Graphics.BLOCK_SIZE,
                    spriteImage.getY() * scale * Constants.Graphics.BLOCK_SIZE,
                    spriteImage.getW() * scale * Constants.Graphics.BLOCK_SIZE,
                    spriteImage.getH() * scale * Constants.Graphics.BLOCK_SIZE);
            entry.addActor(image);
        }

        return entry;
    }

    @Override
    public boolean remove()
    {
        clearSources();

        return super.remove();
    }

    public void exit(Runnable done)
    {
        panel.clearActions();
        panel.addAction(Actions.sequence(
            Actions.moveTo(TOTAL_WIDTH, 0, 0.25f, Interpolation.pow2In),
            Actions.run(done)
        ));
    }

    public ScrollPane getPane()
    {
        return pane;
    }
}
