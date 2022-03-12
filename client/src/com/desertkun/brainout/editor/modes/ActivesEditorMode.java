package com.desertkun.brainout.editor.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.*;
import com.desertkun.brainout.common.editor.EditorProperty;
import com.desertkun.brainout.common.editor.props.get.EditorGetActivePropertiesMsg;
import com.desertkun.brainout.common.editor.props.set.EditorSetActivePropertyMsg;
import com.desertkun.brainout.common.msg.client.editor.*;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.Light;
import com.desertkun.brainout.content.active.Sprite;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.data.EditorMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.SpriteData;
import com.desertkun.brainout.data.components.BoundingBoxComponentData;
import com.desertkun.brainout.data.components.SpriteComponentData;
import com.desertkun.brainout.data.interfaces.ActiveLayer;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.editor.menu.EditorMenu;
import com.desertkun.brainout.editor.menu.SelectContentMenu;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Tooltip;

public class ActivesEditorMode extends EditorMode
{
    private final Vector2 currentPosition, originalPosition;
    public static int currentLayer = 0;

    private static Vector2 tmp = new Vector2();
    private static Vector2 tmp2 = new Vector2();

    private ObjectMap<ActiveData, Selection> selection;
    private ActiveData currentSelection;

    public class Selection
    {
        public Vector2 orig;

        public Selection(ActiveData activeData)
        {
            this.orig = new Vector2(activeData.getX(), activeData.getY());
        }
    }

    public enum DragState
    {
        node,
        moveActive,
        selection
    }

    public enum Filter
    {
        other,
        sprites,
        lights
    }

    private Active currentActive;

    private TextureAtlas.AtlasRegion selectionNormal, selectionSelected;
    private final BitmapFont drawFont;
    private TextButton currentActiveButton;
    private boolean cloneMode;
    private Button cutterEnabled;
    private DragState dragState;
    private Filter filter;

    public ActivesEditorMode(EditorMenu menu, EditorMap map, Filter filter)
    {
        super(menu, map);

        this.filter = filter;

        currentPosition = new Vector2();
        originalPosition = new Vector2();
        selection = new ObjectMap<>();
        currentSelection = null;

        selectionNormal = BrainOutClient.getRegion("active-normal");
        selectionSelected = BrainOutClient.getRegion("active-selected");

        drawFont = BrainOutClient.Skin.getFont("ingame");
        dragState = DragState.node;

        Array<Content> contentArray = BrainOutClient.ContentMgr.queryContent(Active.class,
            this::filterContent);

        currentActive = null;

        if (contentArray.size > 0)
        {
            currentActive = ((Active) contentArray.get(0));
        }
    }

    @Override
    public void initContextMenu(RegisterButton callback)
    {
        cutterEnabled = (Button)callback.registerButton("button-editor-cutter", () -> {});
        Tooltip.RegisterToolTip(cutterEnabled, L.get("EDITOR_BLOCKS_CUTTER"), getMenu());

        callback.registerSpace();

        RegisterButton subMenuRoot = callback.registerSubMenu();

        this.currentActiveButton = new TextButton("", BrainOutClient.Skin, "button-editor-text");

        currentActiveButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                SelectContentMenu m = new SelectContentMenu(currentActive,
                    Active.class,
                    new SelectContentMenu.ContentSelected()
                    {
                        @Override
                        public void selected(Content content)
                        {
                            currentActive = ((Active) content);
                            updateActiveButton();
                        }

                        @Override
                        public void canceled()
                        {

                        }

                        @Override
                        public boolean filter(Content content)
                        {
                            return filterContent(content);
                        }
                    });

                getMenu().pushMenu(m);
            }
        });

        subMenuRoot.registerActor(currentActiveButton);

        updateActiveButton();
    }

    private void updateActiveButton()
    {
        currentActiveButton.setText(currentActive == null ? "[SELECT]" :
                currentActive.getTitle().get());

    }

    @Override
    public void mouseMove(Vector2 pos)
    {
        currentPosition.set(pos);

        getMenu().getStats().setText(getMap().getName() + " x:" + (int)pos.x + " y:" + (int)pos.y);
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        switch (keyCode)
        {
            case Input.Keys.E:
            {
                cutterEnabled.setChecked(!cutterEnabled.isChecked());

                break;
            }
            case Input.Keys.F1:
            {
                moveActivesLayer(ActiveLayer.layer1);
                break;
            }
            case Input.Keys.F2:
            {
                moveActivesLayer(ActiveLayer.layer1top);
                break;
            }
            case Input.Keys.F3:
            {
                moveActivesLayer(ActiveLayer.layer4);
                break;
            }
            case Input.Keys.F4:
            {
                moveActivesLayer(ActiveLayer.layer5);
                break;
            }
            case Input.Keys.DEL:
            {
                for (ObjectMap.Entry<ActiveData, Selection> entry : selection)
                {
                    removeObject(entry.key);
                }

                selection.clear();

                break;
            }
        }

        return super.keyDown(keyCode);
    }

    private void moveActivesLayer(ActiveLayer layer)
    {
        ActiveData closest = getClosestActive();
        if (closest != null)
        {
            moveLayer1top(closest, layer);
        }

        for (ObjectMap.Entry<ActiveData, Selection> entry : selection)
        {
            moveLayer1top(entry.key, layer);
        }

        selection.clear();
    }

    private void moveLayer1top(ActiveData key, ActiveLayer layer)
    {
        EditorProperty pt = new EditorProperty();
        pt.value = PropertyValue.vEnum;
        pt.data = layer.toString();
        pt.name = "layer";
        pt.kind = PropertyKind.string;
        pt.clazz = "data.ActiveLayer";

        BrainOutClient.ClientController.sendTCP(new EditorSetActivePropertyMsg(key, pt));
    }

    @Override
    public void touchUp(Vector2 pos)
    {
        currentPosition.set(pos);

        switch (dragState)
        {
            case selection:
            {
                if (currentPosition.dst(originalPosition) < 0.1f)
                {
                    if (cutterEnabled.isChecked())
                    {
                        ActiveData activeData = getClosestActive();

                        if (activeData != null)
                        {
                            removeObject(activeData);
                            selection.remove(activeData);
                        }

                        if (selection.size > 0)
                        {
                            for (ObjectMap.Entry<ActiveData, Selection> entry : selection)
                            {
                                removeObject(entry.key);
                            }

                            selection.clear();
                        }
                    }
                    else
                    {
                        if (selection.size > 0)
                        {
                            clearSelection();
                        }
                        else
                        {
                            addObject(currentPosition);
                        }
                    }

                    dragState = DragState.node;
                }
                else
                {
                    if (!isMultiSelection())
                    {
                        clearSelection();
                    }

                    float minX = Math.min(originalPosition.x, currentPosition.x),
                            minY = Math.min(originalPosition.y, currentPosition.y),
                            maxX = Math.max(originalPosition.x, currentPosition.x),
                            maxY = Math.max(originalPosition.y, currentPosition.y);

                    for (ObjectMap.Entry<Integer, ActiveData> entry : getMap().getActives())
                    {
                        ActiveData activeData = entry.value;

                        if (validateActive(activeData))
                        {
                            float x = activeData.getX(), y = activeData.getY();

                            if (x >= minX && x <= maxX &&
                                    y >= minY && y <= maxY)
                            {
                                selectItem(activeData);
                            }
                        }
                    }

                    dragState = DragState.node;
                }

                break;
            }
            case moveActive:
            {
                if (selection.size > 0 && currentSelection != null)
                {
                    // calculate the offset
                    tmp.set(currentPosition);
                    tmp.sub(originalPosition);

                    for (ObjectMap.Entry<ActiveData, Selection> entry : selection)
                    {
                        Selection selected = entry.value;

                        // get the original position of the object
                        tmp2.set(selected.orig);
                        // add the position we've changed
                        tmp2.add(tmp);

                        if (isAttachToGrid())
                        {
                            tmp2.x = (int)(tmp2.x + 0.5f);
                            tmp2.y = (int)(tmp2.y + 0.5f);
                        }

                        if (cloneMode)
                        {
                            cloneObject(entry.key, tmp2);
                        }
                        else
                        {

                            moveObject(entry.key, tmp2);
                        }

                        entry.key.updated();
                    }
                }

                if (!isMultiSelection() && selection.size == 1)
                {
                    clearSelection();
                }

                dragState = DragState.node;

                break;
            }
        }

    }

    private boolean isAttachToGrid()
    {
        return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
    }

    private void cloneObject(ActiveData activeData, Vector2 position)
    {
        if (activeData != null)
        {
            BrainOutClient.ClientController.sendTCP(new EditorActiveCloneMsg(activeData, position));
        }
    }

    @Override
    public void touchMove(Vector2 pos)
    {
        super.touchMove(pos);

        switch (dragState)
        {
            case selection:
            {
                currentPosition.set(pos);

                break;
            }
            case moveActive:
            {
                if (!cloneMode)
                {
                    // calculate the offset
                    tmp.set(pos);
                    tmp.sub(originalPosition);

                    for (ObjectMap.Entry<ActiveData, Selection> entry : selection)
                    {
                        Selection selected = entry.value;

                        tmp2.set(selected.orig);
                        tmp2.add(tmp);

                        if (isAttachToGrid())
                        {
                            tmp2.x = (int)(tmp2.x + 0.5f);
                            tmp2.y = (int)(tmp2.y + 0.5f);
                        }

                        entry.key.setPosition(tmp2.x, tmp2.y);
                    }
                }

                currentPosition.set(pos);

                break;
            }
        }
    }

    @Override
    public void properties(Vector2 pos)
    {
        currentPosition.set(pos);

        final ActiveData activeData = getClosestActive();

        if (activeData != null)
        {
            BrainOutClient.ClientController.sendTCP(new EditorGetActivePropertiesMsg(activeData));
        }

    }

    public void selectItem(ActiveData data)
    {
        selection.put(data, new Selection(data));
    }

    @Override
    public void touchDown(Vector2 pos)
    {
        currentPosition.set(pos);

        ActiveData activeData = getClosestActive();

        if (cutterEnabled.isChecked())
        {
            originalPosition.set(currentPosition);
            dragState = DragState.selection;
        }
        else
        {
            if (isMultiSelection())
            {
                if (activeData != null)
                {
                    if (isItemSelected(activeData))
                    {
                        unselectItem(activeData);
                    }
                    else
                    {
                        selectItem(activeData);
                    }
                }
            }
            else
            {
                if (activeData != null)
                {
                    if (!isItemSelected(activeData))
                    {
                        clearSelection();
                    }

                    cloneMode = Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT);

                    selectItem(activeData);

                    for (ObjectMap.Entry<ActiveData, Selection> entry : selection)
                    {
                        entry.value.orig.set(entry.key.getX(), entry.key.getY());
                    }

                    originalPosition.set(activeData.getX(), activeData.getY());
                    currentSelection = activeData;
                    dragState = DragState.moveActive;
                }
                else
                {
                    originalPosition.set(currentPosition);
                    dragState = DragState.selection;
                }
            }
        }
    }

    private void clearSelection()
    {
        selection.clear();
    }

    public boolean isMultiSelection()
    {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
    }

    private void unselectItem(ActiveData data)
    {
        selection.remove(data);
    }

    private boolean isItemSelected(ActiveData data)
    {
        return selection.containsKey(data);
    }

    private ActiveData getClosestActive()
    {
        return getMap().getClosestActive(1, currentPosition.x, currentPosition.y,
            ActiveData.class, this::validateActive);
    }

    private boolean validateActive(ActiveData activeData)
    {
        return isLayerEnabled(activeData.getLayer()) &&
                filterContent(activeData.getCreator()) &&
                activeData.getCreator() != null &&
                activeData.getCreator().isEditorSelectable();
    }

    private void removeObject(ActiveData activeData)
    {
        if (activeData != null)
        {
            BrainOutClient.ClientController.sendTCP(new EditorActiveRemoveMsg(activeData));
        }
    }

    private void moveObject(ActiveData activeData, Vector2 position)
    {
        if (activeData != null)
        {
            BrainOutClient.ClientController.sendTCP(new EditorActiveMoveMsg(activeData, position));
        }
    }

    public boolean isLayerEnabled(int layer)
    {
        return ((EditorMap) getMap()).getActives().getRenderLayer(layer).isEnabled();
    }

    private void addObject(Vector2 position)
    {
        if (currentActive != null)
        {
            BrainOutClient.ClientController.sendTCP(new EditorActiveAddMsg(currentActive, getCurrentLayer(),
                null, position.x, position.y, getMap().getDimension()));
        }
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);

        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl20.glLineWidth(2);

        switch (dragState)
        {
            case selection:
            {

                float minX = Math.min(originalPosition.x, currentPosition.x),
                      minY = Math.min(originalPosition.y, currentPosition.y),
                      maxX = Math.max(originalPosition.x, currentPosition.x),
                      maxY = Math.max(originalPosition.y, currentPosition.y);

                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.rect(minX, minY, maxX - minX, maxY - minY);

                break;
            }
            case moveActive:
            {
                if (currentSelection != null)
                {
                    shapeRenderer.setColor(cloneMode ? Color.RED : Color.BLUE);

                    tmp2.set(currentSelection.getX(), currentSelection.getY());

                    if (isAttachToGrid())
                    {
                        tmp2.x = (int)(tmp2.x + 0.5f);
                        tmp2.y = (int)(tmp2.y + 0.5f);
                    }

                    shapeRenderer.line(currentPosition.x, currentPosition.y, tmp2.x, tmp2.y);
                }

                break;
            }
            case node:
            {
                renderArrow(shapeRenderer, currentPosition.x, currentPosition.y);

                break;
            }
        }

        EditorMap map = getMap();

        if (filter == Filter.other)
        {
            shapeRenderer.setColor(Color.RED);
            Gdx.gl20.glLineWidth(1);

            for (ObjectMap.Entry<Integer, ActiveData> entry :
                    map.getActives().getItemsForTag(Constants.ActiveTags.SPAWNABLE))
            {
                ActiveData activeData = entry.value;

                shapeRenderer.rect(activeData.getX() - 1.0f, activeData.getY() - 1.5f, 2.0f, 3.0f);
            }
        }

        shapeRenderer.setColor(Color.PURPLE);
        Gdx.gl20.glLineWidth(1);

        for (ObjectMap.Entry<Integer, ActiveData> entry: map.getActives().entries())
        {
            ActiveData activeData = entry.value;

            BoundingBoxComponentData bb = activeData.getComponent(BoundingBoxComponentData.class);

            if (bb == null)
                continue;

            float w = bb.getWidth() / 2.0f, h = bb.getHeight() / 2.0f;

            shapeRenderer.rect(
                activeData.getX() - w, activeData.getY() - h, bb.getWidth(), bb.getHeight()
            );
        }

        shapeRenderer.end();

        batch.begin();

        for (ObjectMap.Entry<Integer, ActiveData> entry: map.getActives().entries())
        {
            ActiveData activeData = entry.value;

            if (activeData.getCreator() == null) continue;

            if (activeData.getCreator().isEditorSelectable())
            {
                renderActive(batch, activeData, context);
            }
        }
    }

    public boolean filterContent(Content content)
    {
        if (content == null)
        {
            return false;
        }

        if (!content.isEditorSelectable())
        {
            return false;
        }

        switch (filter)
        {
            case sprites:
            {
                return content instanceof Sprite;
            }
            case lights:
            {
                return content instanceof Light;
            }
            case other:
            {
                if (content instanceof Sprite ||
                    content instanceof Light)
                {
                    return false;
                }

                break;
            }
            default:
            {
                return true;
            }
        }

        return true;
    }

    private void renderActive(Batch batch, ActiveData activeData, RenderContext context)
    {
        if (!filterContent(activeData.getCreator()))
        {
            return;
        }

        if (!isLayerEnabled(activeData.getLayer()))
        {
            return;
        }

        float x = activeData.getX(), y = activeData.getY();

        Active creator = activeData.getCreator();
        if (creator == null) return;

        Team team = activeData.getTeam();

        Color color;
        if (team == null)
        {
            color = Color.WHITE;
        }
        else
        {
            color = team.getColor();
        }

        boolean selected = currentPosition.dst(x, y) < 1f || isItemSelected(activeData);

        float size = 2.0f;

        if (selected)
        {
            if (activeData instanceof SpriteData)
            {
                SpriteData asSprite = ((SpriteData) activeData);
                SpriteComponentData sc = asSprite.getComponent(SpriteComponentData.class);
                if (sc != null)
                {
                    Color or = batch.getColor().cpy();
                    batch.setColor(0.8f, 0.8f, 0.8f, 1.0f);
                    asSprite.render(batch, context);
                    batch.setColor(or);
                }

            }

            if (cutterEnabled.isChecked())
            {
                color = Color.RED;
            }

            String info = activeData.getEditorTitle();

            if (info == null)
            {
                info = "[" + creator.getID() + "]" +
                    (activeData.getNameId() != null ? (" " + activeData.getNameId()) : "")
                    + "\nteam: " + (team == null ? "NO" : team.getID())
                    + "\nz: " + activeData.getZIndex();
                drawFont.setColor(Color.WHITE);
            }
            else
            {
                drawFont.setColor(Color.YELLOW);
            }

            drawFont.draw(batch, info, x + 1, y + 1);
            drawFont.setColor(Color.WHITE);

            size = 2.4f;
        }

        batch.setColor(color);

        TextureRegion icon = selected ? selectionSelected : selectionNormal;

        if (activeData.getContent().hasComponent(IconComponent.class))
        {
            IconComponent iconComponent = activeData.getContent().getComponent(IconComponent.class);
            icon = iconComponent.getIcon();
        }

        batch.draw(icon, x - size / 2.0f, y - size / 2.0f, size, size);
        batch.setColor(Color.WHITE);
    }

    private void renderArrow(ShapeRenderer shapeRenderer, float x, float y)
    {
        shapeRenderer.setColor(Color.GREEN);

        shapeRenderer.line(x - 1f, y, x - 0.1f, y);
        shapeRenderer.line(x + 1f, y, x + 0.1f, y);
        shapeRenderer.line(x, y - 1f, x, y - 0.1f);
        shapeRenderer.line(x, y + 1f, x, y + 0.1f);
    }

    public static int getCurrentLayer()
    {
        return currentLayer;
    }

    public static void setCurrentLayer(int currentLayer)
    {
        ActivesEditorMode.currentLayer = currentLayer;
    }
}
