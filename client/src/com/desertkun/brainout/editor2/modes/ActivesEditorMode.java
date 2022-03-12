package com.desertkun.brainout.editor2.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientEnvironment;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.common.msg.client.editor2.*;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.Sound;
import com.desertkun.brainout.content.components.SpriteWithBlocksComponent;
import com.desertkun.brainout.content.components.UserSpriteWithBlocksComponent;
import com.desertkun.brainout.data.ClientActiveDataMap;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.components.SpriteWithBlocksComponentData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.SpriteBlockComponentData;
import com.desertkun.brainout.data.components.UserSpriteWithBlocksComponentData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.editor2.Editor2Menu;
import com.desertkun.brainout.editor2.widgets.SpritesWidget;
import com.desertkun.brainout.events.ActiveActionEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Tooltip;

import java.util.TimerTask;

public class ActivesEditorMode extends EditorMode implements EventReceiver
{
    private ImageButton cutterButton;
    private boolean cutterEnabled;
    private float counter;
    private SpritesWidget spritesWidget;

    private ObjectSet<ActiveData> selected, originals;
    private ObjectSet<ActiveData> hoveredItems;
    private boolean selectionMode, dragMode, dragValid, duplicationMode;
    private Vector2 selectionStart, selectionEnd, dragStart, tmp;
    private ObjectMap<ActiveData, Vector2> originalPositions, originalPositions2;
    private ObjectSet<ActiveData> updatedActiveDataList;
    private ObjectSet<Integer> tmp2 = new ObjectSet<>();
    private boolean dirty;

    public ActivesEditorMode(Editor2Menu menu)
    {
        super(menu);

        cutterEnabled = false;
        selected = new ObjectSet<>();
        originals = new ObjectSet<>();
        hoveredItems = new ObjectSet<>();
        selectionStart = new Vector2();
        selectionEnd = new Vector2();
        dragStart = new Vector2();
        tmp = new Vector2();
        originalPositions = new ObjectMap<>();
        originalPositions2 = new ObjectMap<>();
        updatedActiveDataList = new ObjectSet<>();
    }

    private void clearSelection()
    {
        selected.clear();
    }

    private void addSelection(ActiveData activeData)
    {
        selected.add(activeData);
    }

    private void flipSelection(ActiveData activeData)
    {
        if (isSelected(activeData))
        {
            selected.remove(activeData);
        }
        else
        {
            selected.add(activeData);
        }
    }

    private boolean isSelected(ActiveData activeData)
    {
        return selected.contains(activeData);
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
        spritesWidget = new SpritesWidget(getMenu().getDragAndDrop(), getMenu());
        getMenu().addActor(spritesWidget);
        getMenu().setScrollFocus(spritesWidget.getPane());

        spritesWidget.setBounds(
            BrainOutClient.getWidth() - SpritesWidget.TOTAL_WIDTH,
            0,
            SpritesWidget.TOTAL_WIDTH,
            BrainOutClient.getHeight()
        );

        BrainOutClient.EventMgr.subscribeAt(Event.ID.activeAction, this, true);
    }

    public SpritesWidget getSpritesWidget()
    {
        return spritesWidget;
    }

    @Override
    public void release()
    {
        spritesWidget.exit(() -> spritesWidget.remove());

        BrainOutClient.EventMgr.unsubscribe(Event.ID.activeAction, this);
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public void update(float dt)
    {
        counter += dt * 360.0f;

        if (counter >= 360)
        {
            counter -= 360;
        }

        if (dirty)
        {
            dirty = false;

            boom();

            getSpritesWidget().refresh();
        }
    }

    private void boom()
    {
        Sound s = BrainOutClient.ContentMgr.get("editor-place", Sound.class);
        s.play();
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        batch.end();

        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        if (selected.size > 0)
        {
            renderSelection(batch, context);
        }

        if (hoveredItems.size > 0)
        {
            renderCurrentSelection(batch, context);
        }

        if (selectionMode)
        {
            renderSelectionMode();
        }

        renderFlags();

        shapeRenderer.end();
        batch.begin();
    }

    private void renderFlags()
    {
        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;
        shapeRenderer.setColor(Color.GRAY);

        Map map = Map.Get(getMenu().getDimension());

        for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.FLAG, true))
        {
            SpriteWithBlocksComponentData b = activeData.getComponent(SpriteWithBlocksComponentData.class);

            if (b == null)
                continue;

            FlagData flag = ((FlagData) activeData);

            shapeRenderer.circle(activeData.getX(), activeData.getY(), flag.getSpawnRange() / 2.0f, 64);
        }
    }

    private void renderSelectionMode()
    {
        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;
        shapeRenderer.setColor(Color.WHITE);

        float x = Math.min(selectionStart.x, selectionEnd.x),
              y = Math.min(selectionStart.y, selectionEnd.y),
              w = Math.max(selectionStart.x, selectionEnd.x) - x,
              h = Math.max(selectionStart.y, selectionEnd.y) - y;

        shapeRenderer.rect(x, y, w, h);
    }

    private void renderSelection(Batch batch, RenderContext context)
    {
        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;

        if (dragMode)
        {
            shapeRenderer.setColor(dragValid ? Color.GREEN : Color.RED);
        }
        else
        {
            shapeRenderer.setColor(Color.WHITE);
        }

        for (ActiveData activeData : selected)
        {
            SpriteWithBlocksComponentData spi = activeData.getComponent(SpriteWithBlocksComponentData.class);

            if (spi != null)
            {
                SpriteWithBlocksComponent sp = spi.getContentComponent();
                shapeRenderer.rect(activeData.getX(), activeData.getY(), sp.getWidth(), sp.getHeight());
            }
            else
            {
                UserSpriteWithBlocksComponentData us = activeData.getComponent(UserSpriteWithBlocksComponentData.class);

                if (us != null)
                {
                    shapeRenderer.rect(activeData.getX(), activeData.getY(), us.getWidth(), us.getHeight());
                }
            }
        }
    }

    private void renderCurrentSelection(Batch batch, RenderContext context)
    {
        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;
        shapeRenderer.setColor(cutterEnabled ? Color.RED : Color.YELLOW);

        for (ActiveData hoveredItem : hoveredItems)
        {
            SpriteWithBlocksComponentData spi = hoveredItem.getComponent(SpriteWithBlocksComponentData.class);

            if (spi != null)
            {
                SpriteWithBlocksComponent sp = spi.getContentComponent();
                shapeRenderer.rect(hoveredItem.getX(), hoveredItem.getY(), sp.getWidth(), sp.getHeight());
            }
            else
            {
                UserSpriteWithBlocksComponentData us = hoveredItem.getComponent(UserSpriteWithBlocksComponentData.class);

                if (us != null)
                {
                    shapeRenderer.rect(hoveredItem.getX(), hoveredItem.getY(), us.getWidth(), us.getHeight());
                }
            }
        }
    }

    @Override
    public void selected()
    {
        cutterEnabled = false;
        duplicationMode = false;

        // mode = Mode.moveObject;
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        switch (keyCode)
        {
            case Input.Keys.E:
            {
                Menu.playSound(Menu.MenuSound.select);
                cutterButton.setChecked(!cutterButton.isChecked());
                setCutterEnabled(cutterButton.isChecked());

                return true;
            }
        }

        return false;
    }

    private void renderModesPanel(Table panel)
    {
        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        group.setMaxCheckCount(1);
        group.setMinCheckCount(1);

        {
            cutterButton = new ImageButton(BrainOutClient.Skin, "button-editor-cutter");
            Tooltip.RegisterToolTip(cutterButton, L.get("EDITOR_BLOCKS_CUTTER") + " [E]", getMenu());

            cutterButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(Menu.MenuSound.select);
                    setCutterEnabled(cutterButton.isChecked());
                }
            });

            panel.add(cutterButton);
        }
    }

    @Override
    public boolean escape()
    {
        return false;
    }

    private boolean isMultiSelection()
    {
        return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT);
    }

    private boolean isDuplicationMode()
    {
        if (ClientEnvironment.isMac())
        {
            return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        }

        return Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT);
    }

    private ActiveData checkBlock(Vector2 position, int layer)
    {
        Map map = Map.Get(getMenu().getDimension());

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
        if (button != Input.Buttons.LEFT)
            return false;

        if (selectionMode)
        {
            selectionEnd.set(position);

            float x1 = Math.min(selectionStart.x, selectionEnd.x),
                  y1 = Math.min(selectionStart.y, selectionEnd.y),
                  x2 = Math.max(selectionStart.x, selectionEnd.x),
                  y2 = Math.max(selectionStart.y, selectionEnd.y);

            clearHoveredItems();

            Map map = Map.Get(getMenu().getDimension());

            for (ObjectMap.Entry<Integer, ActiveData> entry : map.getActives())
            {
                ActiveData activeData = entry.value;

                SpriteWithBlocksComponentData spi = activeData.getComponent(SpriteWithBlocksComponentData.class);

                if (spi != null)
                {
                    SpriteWithBlocksComponent sp = spi.getContentComponent();

                    if (x1 <= activeData.getX() && y1 <= activeData.getY() &&
                        x2 >= activeData.getX() + sp.getWidth() && y2 >= activeData.getY() + sp.getHeight())
                    {
                        hoverItem(activeData);
                    }
                }
                else
                {
                    UserSpriteWithBlocksComponentData us =
                        activeData.getComponent(UserSpriteWithBlocksComponentData.class);

                    if (us != null)
                    {
                        if (x1 <= activeData.getX() && y1 <= activeData.getY() &&
                                x2 >= activeData.getX() + us.getWidth() && y2 >= activeData.getY() + us.getHeight())
                        {
                            hoverItem(activeData);
                        }
                    }
                }
            }

            return true;
        }
        else
        {
            if (selected.size > 0 || hoveredItems.size > 0)
            {
                if (!dragMode)
                {
                    duplicationMode = isDuplicationMode();
                    originalPositions.clear();

                    boolean clear = false;

                    for (ActiveData hoveredItem : hoveredItems)
                    {
                        if (!isSelected(hoveredItem))
                        {
                            clear = true;
                            break;
                        }
                    }

                    if (clear)
                    {
                        if (!isMultiSelection())
                        {
                            clearSelection();
                        }

                        for (ActiveData hoveredItem : hoveredItems)
                        {
                            addSelection(hoveredItem);
                        }
                    }

                    clearHoveredItems();

                    if (duplicationMode)
                    {
                        originals.clear();
                        originals.addAll(selected);
                        selected.clear();
                        Map map = Map.Get(getMenu().getDimension());

                        for (ActiveData original : originals)
                        {
                            String d = Data.ComponentSerializer.toJson(original, Data.ComponentWriter.TRUE, -1);
                            ActiveData copy = map.newActiveData(map.generateClientId(), d, true);
                            selected.add(copy);
                        }
                    }

                    for (ActiveData activeData : selected)
                    {
                        originalPositions.put(activeData, new Vector2(activeData.getX(), activeData.getY()));
                    }

                    dragMode = true;
                }

                dragValid = true;

                Map map = Map.Get(getMenu().getDimension());

                tmp.set(position).sub(dragStart);
                tmp2.clear();

                for (ObjectMap.Entry<ActiveData, Vector2> entry : originalPositions)
                {
                    ActiveData activeData = entry.key;
                    Vector2 originalPosition = entry.value;

                    int x_ = (int)(originalPosition.x + (int)tmp.x), y_ = (int)(originalPosition.y + (int)tmp.y);

                    activeData.setPosition(x_, y_);

                    SpriteWithBlocksComponentData spi = activeData.getComponent(SpriteWithBlocksComponentData.class);

                    if (spi != null)
                    {
                        SpriteWithBlocksComponent sp = spi.getContentComponent();

                        if (!sp.validateBlocksForAdding(map, x_, y_, selected))
                        {
                            dragValid = false;
                        }
                    }
                    else
                    {
                        UserSpriteWithBlocksComponentData us =
                            activeData.getComponent(UserSpriteWithBlocksComponentData.class);

                        if (us != null)
                        {
                            if (!us.validateBlocksForAdding(map, x_, y_, selected))
                            {
                                dragValid = false;
                            }
                        }
                    }

                    tmp2.add(activeData.getLayer());
                }

                for (Integer layer : tmp2)
                {
                    ClientActiveDataMap.ClientRenderMap renderMap =
                        ((ClientActiveDataMap.ClientRenderMap) map.getActives().getRenderLayer(layer));

                    renderMap.getCache().updateCache();
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseUp(Vector2 position, int button)
    {
        if (getMenu().getDragAndDrop().isDragging())
            return false;

        if (button == Input.Buttons.LEFT)
        {
            if (selectionMode)
            {
                selectionMode = false;

                if (!isMultiSelection())
                {
                    clearSelection();
                }

                for (ActiveData hoveredItem : hoveredItems)
                {
                    addSelection(hoveredItem);
                }

                clearHoveredItems();

                return true;
            }
            else
            {
                if (dragMode)
                {
                    dragMode = false;

                    Map map = Map.Get(getMenu().getDimension());

                    if (dragValid)
                    {
                        tmp.set(position).sub(dragStart);

                        int x_ = (int)(tmp.x), y_ = (int)(tmp.y);

                        if (duplicationMode)
                        {
                            BrainOutClient.ClientController.sendTCP(new CopyObjectsMsg(
                                getMenu().getDimension(), originals, x_, y_
                            ));
                        }
                        else
                        {
                            if (x_ != 0 || y_ != 0)
                            {
                                BrainOutClient.ClientController.sendTCP(new MoveObjectsMsg(
                                        getMenu().getDimension(), selected, x_, y_
                                ));

                                boom();
                            }
                        }
                    }
                    else
                    {
                        Menu.playSound(Menu.MenuSound.denied);

                        // rollback immediately

                        for (ObjectMap.Entry<ActiveData, Vector2> entry : originalPositions)
                        {
                            entry.key.setPosition(entry.value.x, entry.value.y);

                            tmp2.add(entry.key.getLayer());
                        }

                        originalPositions.clear();

                        for (Integer layer : tmp2)
                        {
                            ClientActiveDataMap.ClientRenderMap renderMap =
                                ((ClientActiveDataMap.ClientRenderMap) map.getActives().getRenderLayer(layer));

                            renderMap.getCache().updateCache();
                        }
                    }

                    if (duplicationMode)
                    {
                        for (ActiveData activeData : selected)
                        {
                            map.removeActive(activeData, true);
                        }

                        selected.clear();
                        selected.addAll(originals);
                        originals.clear();
                    }
                    else
                    {
                        originalPositions2.clear();
                        originalPositions2.putAll(originalPositions);

                        updatedActiveDataList.clear();

                        // a timed that automatically roll back changes if they are not changed themselves
                        BrainOutClient.Timer.schedule(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                Gdx.app.postRunnable(() ->
                                {
                                    tmp2.clear();

                                    for (ObjectMap.Entry<ActiveData, Vector2> entry : originalPositions2)
                                    {
                                        if (updatedActiveDataList.contains(entry.key))
                                        {
                                            // do not revert those who's been updated
                                            continue;
                                        }

                                        entry.key.setPosition(entry.value.x, entry.value.y);

                                        tmp2.add(entry.key.getLayer());
                                    }

                                    updatedActiveDataList.clear();

                                    for (Integer layer : tmp2)
                                    {
                                        ClientActiveDataMap.ClientRenderMap renderMap =
                                                ((ClientActiveDataMap.ClientRenderMap) map.getActives().getRenderLayer(layer));

                                        renderMap.getCache().updateCache();
                                    }

                                    originalPositions2.clear();
                                });
                            }
                        }, 500);
                    }

                    originalPositions.clear();
                }
                else
                {
                    ActiveData hoveredItem = checkBlock(position);

                    if (!isMultiSelection())
                    {
                        clearSelection();
                    }

                    if (hoveredItem != null)
                    {
                        flipSelection(hoveredItem);
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseDown(Vector2 position, int button)
    {
        if (button == Input.Buttons.LEFT)
        {
            ActiveData hoveredItem = checkBlock(position);

            if (hoveredItem == null)
            {
                if (cutterEnabled)
                {
                    //
                }
                else
                {
                    // nothing is hovered while clicking, enter the selection mode
                    selectionMode = true;
                    selectionStart.set(position);
                    selectionEnd.set(position);
                    clearHoveredItems();
                }
            }
            else
            {
                if (cutterEnabled)
                {
                    BrainOutClient.ClientController.sendTCP(new RemoveObjectMsg(hoveredItem));
                    clearHoveredItems();
                    clearSelection();
                }
                else
                {
                    hoverOneItem(hoveredItem);
                    dragStart.set((int)position.x, (int)position.y);
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, Vector2 position, int pointer)
    {
        Object o = payload.getObject();

        if (o instanceof SpriteWithBlocksComponent)
        {
            if (!(payload instanceof SpritesWidget.SpritePayload))
                return false;

            SpritesWidget.SpritePayload p = ((SpritesWidget.SpritePayload) payload);

            float x = position.x + p.getOffsetX() / Constants.Graphics.BLOCK_SIZE,
                    y = position.y + p.getOffsetY() / Constants.Graphics.BLOCK_SIZE;

            SpriteWithBlocksComponent sp = ((SpriteWithBlocksComponent) o);
            Map map = Map.Get(getMenu().getDimension());

            boolean valid = sp.validateBlocksForAdding(map, (int) x, (int) y);

            float x_ = (x % 1) * Constants.Graphics.BLOCK_SIZE,
                    y_ = (y % 1) * Constants.Graphics.BLOCK_SIZE;

            p.updateOffset(-x_, -y_);

            return valid;
        }

        if (o instanceof UserSpriteWithBlocksComponent)
        {
            if (!(payload instanceof SpritesWidget.UserSpritePayload))
                return false;

            SpritesWidget.UserSpritePayload p = ((SpritesWidget.UserSpritePayload) payload);

            float x = position.x + p.getOffsetX() / Constants.Graphics.BLOCK_SIZE,
                    y = position.y + p.getOffsetY() / Constants.Graphics.BLOCK_SIZE;

            UserSpriteWithBlocksComponent us = ((UserSpriteWithBlocksComponent) o);
            Map map = Map.Get(getMenu().getDimension());

            boolean valid = us.validateBlocksForAdding(map, (int) x, (int) y, p.getWidth(), p.getHeight());

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
            if (!(payload instanceof SpritesWidget.SpritePayload))
                return;

            SpritesWidget.SpritePayload p = ((SpritesWidget.SpritePayload) payload);

            float x = position.x + p.getOffsetX() / Constants.Graphics.BLOCK_SIZE,
                    y = position.y + p.getOffsetY() / Constants.Graphics.BLOCK_SIZE;

            SpriteWithBlocksComponent sp = ((SpriteWithBlocksComponent) o);
            Map map = Map.Get(getMenu().getDimension());

            if (!sp.validateBlocksForAdding(map, (int) x, (int) y))
                return;

            Content content = sp.getContent();

            BrainOutClient.ClientController.sendTCP(
                    new CreateObjectMsg(getMenu().getDimension(), content, (int) x, (int) y));
        }

        if (o instanceof UserSpriteWithBlocksComponent)
        {
            if (!(payload instanceof SpritesWidget.UserSpritePayload))
                return;

            SpritesWidget.UserSpritePayload p = ((SpritesWidget.UserSpritePayload) payload);

            float x = position.x + p.getOffsetX() / Constants.Graphics.BLOCK_SIZE,
                  y = position.y + p.getOffsetY() / Constants.Graphics.BLOCK_SIZE;

            UserSpriteWithBlocksComponent sp = ((UserSpriteWithBlocksComponent) o);
            Map map = Map.Get(getMenu().getDimension());

            if (!sp.validateBlocksForAdding(map, (int) x, (int) y, p.getWidth(), p.getHeight()))
                return;

            BrainOutClient.ClientController.sendTCP(
                new CreateUserImageMsg(getMenu().getDimension(), sp.getContent(), p.getSprite(), (int) x, (int) y,
                    p.getWidth(), p.getHeight()));
        }
    }

    private void setCutterEnabled(boolean enabled)
    {
        this.cutterEnabled = enabled;
    }

    public boolean isCutterEnabled()
    {
        return cutterEnabled;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case activeAction:
            {
                ActiveActionEvent ev = ((ActiveActionEvent) event);

                if (ev.action == ActiveActionEvent.Action.updated)
                {
                    updatedActiveData(ev.activeData);
                }
                else if (ev.action == ActiveActionEvent.Action.removed)
                {
                    refresh();
                }
                else if (ev.action == ActiveActionEvent.Action.added)
                {
                    refresh();
                }

                break;
            }
        }

        return false;
    }

    private void refresh()
    {
        dirty = true;
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
