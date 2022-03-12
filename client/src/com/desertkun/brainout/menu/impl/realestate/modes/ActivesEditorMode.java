package com.desertkun.brainout.menu.impl.realestate.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.content.Sound;
import com.desertkun.brainout.content.components.RealEstateItemContainerComponent;
import com.desertkun.brainout.content.components.SpriteWithBlocksComponent;
import com.desertkun.brainout.data.FreePlayMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.ActiveProgressVisualComponentData;
import com.desertkun.brainout.data.components.RealEstateItemComponentData;
import com.desertkun.brainout.data.components.SpriteBlockComponentData;
import com.desertkun.brainout.data.components.SpriteWithBlocksComponentData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.ActiveActionEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.ExchangeInventoryMenu;
import com.desertkun.brainout.menu.impl.realestate.AvailableRealEstateItemsWidget;
import com.desertkun.brainout.menu.impl.realestate.RealEstateEditMenu;
import com.desertkun.brainout.menu.impl.realestate.RealEstateItemExchangeInventoryMenu;
import org.json.JSONObject;

public class ActivesEditorMode extends EditorMode
{
    private final String dimension;
    private AvailableRealEstateItemsWidget spritesWidget;

    private ObjectSet<ActiveData> hoveredItems;
    private Vector2 selectionStart, selectionEnd, dragStart;
    private ObjectSet<ActiveData> updatedActiveDataList;

    public ActivesEditorMode(String dimension, RealEstateEditMenu menu)
    {
        super(menu);

        this.dimension = dimension;
        hoveredItems = new ObjectSet<>();
        selectionStart = new Vector2();
        selectionEnd = new Vector2();
        dragStart = new Vector2();
        updatedActiveDataList = new ObjectSet<>();
    }

    private void clearHoveredItems()
    {
        hoveredItems.clear();
    }

    private void hoverOneItem(ActiveData activeData)
    {
        clearHoveredItems();
        hoveredItems.add(activeData);
    }

    private void hoverItem(ActiveData activeData)
    {
        hoveredItems.add(activeData);
    }

    @Override
    public void renderPanels(Table toolbar)
    {
        {
            Table panel = new Table(BrainOutClient.Skin);
            panel.setBackground("buttons-group");
            renderModesPanel(panel);
            toolbar.add(panel).padRight(8);
        }
    }

    @Override
    public void init()
    {
        spritesWidget = new AvailableRealEstateItemsWidget(dimension, getMenu().getDragAndDrop(), getMenu());
        getMenu().addActor(spritesWidget);
        getMenu().setScrollFocus(spritesWidget.getPane());

        spritesWidget.setBounds(
            BrainOutClient.getWidth() - AvailableRealEstateItemsWidget.TOTAL_WIDTH,
            0,
            AvailableRealEstateItemsWidget.TOTAL_WIDTH,
            BrainOutClient.getHeight()
        );
    }

    public AvailableRealEstateItemsWidget getSpritesWidget()
    {
        return spritesWidget;
    }

    @Override
    public void release()
    {
        spritesWidget.exit(() -> spritesWidget.remove());
    }

    private void boom()
    {
        Sound s = BrainOutClient.ContentMgr.get("editor-place", Sound.class);
        s.play();
    }

    @Override
    public void selected()
    {
        // mode = Mode.moveObject;
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        return false;
    }

    private void renderModesPanel(Table panel)
    {
    }

    @Override
    public boolean escape()
    {
        return false;
    }


    private ActiveData checkBlock(Vector2 position, int layer)
    {
        Map map = Map.Get(dimension);

        if (map == null)
        {
            return null;
        }

        BlockData b = map.getBlock((int)position.x, (int)position.y, layer);

        if (b != null)
        {
            SpriteBlockComponentData cp = b.getComponent(SpriteBlockComponentData.class);

            if (cp != null)
            {
                return cp.getSprite(map);
            }
        }

        return null;
    }

    private ActiveData checkBlock(Vector2 position)
    {
        ActiveData activeData = checkBlock(position, Constants.Layers.BLOCK_LAYER_FOREGROUND);

        if (activeData != null)
        {
            return activeData;
        }

        return checkBlock(position, Constants.Layers.BLOCK_LAYER_BACKGROUND);
    }

    @Override
    public boolean mouseMove(Vector2 position)
    {
        if (getMenu().getDragAndDrop().isDragging())
            return false;

        ActiveData activeData = checkBlock(position);

        if (activeData != null)
        {
            hoverOneItem(activeData);
            return true;
        }
        else
        {
            clearHoveredItems();
            return false;
        }
    }

    @Override
    public boolean mouseDrag(Vector2 position, int button)
    {
        return false;
    }

    @Override
    public boolean mouseUp(Vector2 position, int button)
    {
        if (getMenu().getDragAndDrop().isDragging())
            return false;

        return false;
    }

    @Override
    public boolean mouseDown(Vector2 position, int button)
    {
        if (button == Input.Buttons.LEFT)
        {
            ActiveData hoveredItem = checkBlock(position);

            if (hoveredItem != null)
            {
                RealEstateItemComponentData rsi = hoveredItem.getComponent(RealEstateItemComponentData.class);

                if (rsi != null)
                {
                    int x_ = (int) hoveredItem.getX(), y_ = (int) hoveredItem.getY();

                    JSONObject args = new JSONObject();
                    args.put("map", dimension);
                    args.put("id", hoveredItem.getId());
                    args.put("x", (int)x_);
                    args.put("y", (int)y_);
                    args.put("key", rsi.getKey());
                    args.put("c", hoveredItem.getCreator().getID());

                    BrainOutClient.SocialController.sendRequest("real_estate_remove_object", args,
                    new SocialController.RequestCallback()
                    {
                        @Override
                        public void success(JSONObject response)
                        {
                            refresh();
                        }

                        @Override
                        public void error(String reason)
                        {
                            Menu.playSound(Menu.MenuSound.denied);
                        }
                    });
                }

                clearHoveredItems();
            }

            return true;
        }
        else if (button == Input.Buttons.RIGHT)
        {
            ActiveData hoveredItem = checkBlock(position);

            if (hoveredItem != null)
            {
                SpriteWithBlocksComponentData spi = hoveredItem.getComponent(SpriteWithBlocksComponentData.class);

                if (spi != null && Map.Get(dimension, FreePlayMap.class) != null)
                {
                    RealEstateItemContainerComponent rsnic = hoveredItem.getCreator().getComponent(RealEstateItemContainerComponent.class);
                    if (rsnic != null)
                    {
                        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);
                        if (csGame != null)
                        {
                            PlayerData playerData = csGame.getPlayerData();
                            if (playerData != null)
                            {
                                ActiveProgressVisualComponentData progress = playerData.getComponent(ActiveProgressVisualComponentData.class);

                                if (progress != null && progress.isActive())
                                {
                                    return false;
                                }
                            }
                        }

                        BrainOutClient.getInstance().topState().topMenu().popMeAndPushMenu(new RealEstateItemExchangeInventoryMenu(
                            BrainOutClient.ClientController.getState(CSGame.class).getPlayerData(),
                            hoveredItem
                        ));
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, Vector2 position, int pointer)
    {
        Object o = payload.getObject();

        if (o instanceof SpriteWithBlocksComponent)
        {
            if (!(payload instanceof AvailableRealEstateItemsWidget.RealEstateItemPayload))
                return false;

            float scale = Map.GetWatcher().getScale();

            AvailableRealEstateItemsWidget.RealEstateItemPayload p = ((AvailableRealEstateItemsWidget.RealEstateItemPayload) payload);

            float x = position.x + (p.getOffsetX() * scale) / Constants.Graphics.BLOCK_SIZE,
                    y = position.y + (p.getOffsetY() * scale) / Constants.Graphics.BLOCK_SIZE;

            SpriteWithBlocksComponent sp = ((SpriteWithBlocksComponent) o);
            Map map = Map.Get(dimension);

            boolean valid = sp.validateBlocksForAdding(map, (int) x, (int) y);

            float x_ = (x % 1) * Constants.Graphics.BLOCK_SIZE,
                    y_ = (y % 1) * Constants.Graphics.BLOCK_SIZE;

            p.updateOffset(-x_, -y_);

            return valid;
        }

        return false;
    }

    @Override
    public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, Vector2 position, int pointer)
    {
        Object o = payload.getObject();

        if (o instanceof SpriteWithBlocksComponent)
        {
            if (!(payload instanceof AvailableRealEstateItemsWidget.RealEstateItemPayload))
                return;

            AvailableRealEstateItemsWidget.RealEstateItemPayload p = ((AvailableRealEstateItemsWidget.RealEstateItemPayload) payload);

            float scale = Map.GetWatcher().getScale();

            float x = position.x + (p.getOffsetX() * scale) / Constants.Graphics.BLOCK_SIZE,
                y = position.y + (p.getOffsetY() * scale) / Constants.Graphics.BLOCK_SIZE;

            SpriteWithBlocksComponent sp = ((SpriteWithBlocksComponent) o);
            FreePlayMap map = Map.Get(dimension, FreePlayMap.class);
            if (map == null)
                return;

            if (!sp.validateBlocksForAdding(map, (int) x, (int) y))
                return;

            JSONObject args = new JSONObject();
            args.put("map", dimension);
            args.put("x", (int)x);
            args.put("y", (int)y);
            args.put("name", p.rsItem);
            args.put("payload", p.rsPayload);

            BrainOutClient.SocialController.sendRequest("real_estate_place_object", args,
                new SocialController.RequestCallback()
            {
                @Override
                public void success(JSONObject response)
                {
                    refresh();
                }

                @Override
                public void error(String reason)
                {
                    Menu.playSound(Menu.MenuSound.denied);
                }
            });
        }
    }

    private void refresh()
    {
        boom();
        Gdx.app.postRunnable(() -> getSpritesWidget().refresh());
    }

    private void updatedActiveData(ActiveData activeData)
    {
        updatedActiveDataList.add(activeData);
    }

    @Override
    public ID getID()
    {
        return ID.actives;
    }
}
