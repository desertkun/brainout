package com.desertkun.brainout.editor.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.common.msg.client.editor.EditorBlockMsg;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.data.EditorMap;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.editor.menu.EditorMenu;
import com.desertkun.brainout.editor.menu.SelectContentMenu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Tooltip;

public class BlocksEditorMode extends EditorMode
{
    private static Block currentBlock = null;
    private static int shapeSize = 0;
    private static int currentLayer = Constants.Layers.BLOCK_LAYER_FOREGROUND;

    private int lastPosX, lastPosY, currentPosX, currentPosY;
    private SubMode subMode;
    private State state;
    private RegisterButton subMenuRoot;

    private int selectionX1, selectionY1, selectionX2, selectionY2;
    private int movingOffsetX, movingOffsetY;;
    private float counter;
    private TextButton currentBlockButton;
    private Button cutterEnabled;

    public enum SubMode
    {
        circle,
        rectangle,
        line,
        select
    }

    public enum State
    {
        normal,
        placed,
        selected,
        moving
    }

    public enum SubMenu
    {
        blocks,
        selection
    }

    public BlocksEditorMode(EditorMenu menu, EditorMap map)
    {
        super(menu, map);

        subMode = SubMode.circle;
        state = State.normal;
        counter = 0;
        currentLayer = Constants.Layers.BLOCK_LAYER_FOREGROUND;

        Array<Block> contentArray = BrainOutClient.ContentMgr.queryContent(Block.class);

        if (currentBlock == null)
        {
            if (contentArray.size > 0)
            {
                currentBlock = contentArray.get(0);
            }
        }
    }

    @Override
    public void initContextMenu(RegisterButton callback)
    {
        ButtonGroup shapeGroup = new ButtonGroup();

        Tooltip.RegisterToolTip(callback.registerGroupButton("button-editor-shape-1", () ->
        {
            setSubMode(SubMode.circle);
            initSubMenu(SubMenu.blocks);
            shapeSize = 0;
        }, shapeGroup),  L.get("EDITOR_BLOCKS_BRUSH_CIRCLE"), getMenu());

        Tooltip.RegisterToolTip(callback.registerGroupButton("button-editor-shape-2", () ->
        {
            setSubMode(SubMode.circle);
            initSubMenu(SubMenu.blocks);
            shapeSize = 1;
        }, shapeGroup),  L.get("EDITOR_BLOCKS_BRUSH_CIRCLE"), getMenu());

        Tooltip.RegisterToolTip(callback.registerGroupButton("button-editor-shape-3", () ->
        {
            setSubMode(SubMode.circle);
            initSubMenu(SubMenu.blocks);
            shapeSize = 3;
        }, shapeGroup),  L.get("EDITOR_BLOCKS_BRUSH_CIRCLE"), getMenu());

        Tooltip.RegisterToolTip(callback.registerGroupButton("button-editor-shape-rect", () ->
        {
            setSubMode(SubMode.rectangle);
            initSubMenu(SubMenu.blocks);
        }, shapeGroup),  L.get("EDITOR_BLOCKS_BRUSH_RECTANGLE"), getMenu());

        Tooltip.RegisterToolTip(callback.registerGroupButton("button-editor-shape-line", () ->
        {
            setSubMode(SubMode.line);
            initSubMenu(SubMenu.blocks);
        }, shapeGroup),  L.get("EDITOR_BLOCKS_BRUSH_LINE"), getMenu());

        Tooltip.RegisterToolTip(callback.registerGroupButton("button-editor-shape-select", () ->
        {
            setSubMode(SubMode.select);
            initSubMenu(SubMenu.selection);
        }, shapeGroup),  L.get("EDITOR_BLOCKS_BRUSH_SELECT"), getMenu());

        callback.registerSpace();

        cutterEnabled = (Button)callback.registerButton("button-editor-cutter", () -> {});
        Tooltip.RegisterToolTip(cutterEnabled, L.get("EDITOR_BLOCKS_CUTTER"), getMenu());

        callback.registerSpace();

        this.subMenuRoot = callback.registerSubMenu();
        initSubMenu(SubMenu.blocks);
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
        }

        return super.keyDown(keyCode);
    }

    private void initSubMenu(SubMenu subMenu)
    {
        subMenuRoot.clear();

        switch (subMenu)
        {
            case blocks:
            {
                this.currentBlockButton = new TextButton("", BrainOutClient.Skin, "button-editor-text");

                currentBlockButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        selectBlock();
                    }
                });

                subMenuRoot.registerActor(currentBlockButton);

                updateBlockButton();

                if (currentBlock == null)
                {
                    selectBlock();
                }

                break;
            }
            case selection:
            {

                Tooltip.RegisterToolTip(subMenuRoot.registerButton("button-editor-shape-select-cancel",
                        () -> state = State.normal),
                    L.get("EDITOR_BLOCKS_BRUSH_SELECT_CANCEL"), getMenu());

                Tooltip.RegisterToolTip(subMenuRoot.registerButton("button-editor-shape-select-remove", () ->
                {
                    removeBlocks(selectionX1, selectionY1, selectionX2, selectionY2);

                    state = State.normal;
                }), L.get("EDITOR_BLOCKS_BRUSH_SELECT_REMOVE"), getMenu());

                break;
            }
        }
    }

    private void selectBlock()
    {
        SelectContentMenu m = new SelectContentMenu(currentBlock,
            Block.class,
            new SelectContentMenu.ContentSelected()
            {
                @Override
                public void selected(Content content)
                {
                    currentBlock = ((Block) content);
                    updateBlockButton();
                }

                @Override
                public void canceled()
                {

                }

                @Override
                public boolean filter(Content content)
                {
                    return true;
                }
            });

        getMenu().pushMenu(m);
    }

    private void updateBlockButton()
    {
        currentBlockButton.setText(currentBlock == null ?
            L.get("EDITOR_BLOCKS_SELECT") :
            currentBlock.getTitle().get());

    }

    public boolean isLayerEnabled()
    {
        return ((EditorMap) getMap()).getBlocksLayer(currentLayer).enabled;
    }

    @Override
    public void touchUp(Vector2 pos)
    {
        int posX = (int)pos.x;
        int posY = (int)pos.y;

        switch (subMode)
        {
            case rectangle:
            {
                if (state == State.placed)
                {
                    placeBlockRectangle(lastPosX, lastPosY, posX, posY);

                    state = State.normal;
                }

                break;
            }

            case line:
            {
                if (state == State.placed)
                {
                    placeBlockLine(lastPosX, lastPosY, posX, posY);

                    state = State.normal;
                }

                break;
            }

            case select:
            {
                switch (state)
                {
                    case placed:
                    {
                        selectionX1 = Math.min(lastPosX, currentPosX);
                        selectionY1 = Math.min(lastPosY, currentPosY);
                        selectionX2 = Math.max(lastPosX, currentPosX);
                        selectionY2 = Math.max(lastPosY, currentPosY);

                        state = State.selected;

                        break;
                    }
                    case moving:
                    {
                        copyBlock(
                            selectionX1, selectionY1,
                            selectionX2, selectionY2,
                            posX - movingOffsetX, posY - movingOffsetY,
                            isCopy()
                        );

                        int selectionWidth = selectionX2 - selectionX1,
                            selectionHeight = selectionY2 - selectionY1;

                        selectionX1 = posX - movingOffsetX;
                        selectionY1 = posY - movingOffsetY;
                        selectionX2 = selectionX1 + selectionWidth;
                        selectionY2 = selectionY1 + selectionHeight;

                        state = State.selected;

                        break;
                    }
                }

                break;
            }
        }
    }

    @Override
    public void touchDown(Vector2 pos)
    {
        lastPosX = (int)pos.x;
        lastPosY = (int)pos.y;

        switch (subMode)
        {
            case circle:
            {
                placeBlockCircle(lastPosX, lastPosY);
                break;
            }
            case rectangle:
            case line:
            {
                state = State.placed;

                break;
            }
            case select:
            {
                if (state == State.selected &&
                    lastPosX >= selectionX1 && lastPosY >= selectionY1 &&
                    lastPosX <= selectionX2 && lastPosY <= selectionY2)
                {
                    movingOffsetX = lastPosX - selectionX1;
                    movingOffsetY = lastPosY - selectionY1;
                    state = State.moving;

                    return;
                }

                state = State.placed;

                break;
            }
        }
    }


    private void removeBlocks(int x1, int y1, int x2, int y2)
    {
        if (!isLayerEnabled()) return;

        for (int i = x1; i <= x2; i++)
        {
            for (int j = y1; j <= y2; j++)
            {
                BlockData blockData = getMap().getBlock(i, j, currentLayer);
                if (blockData != null && !blockData.isRemovable()) continue;

                BrainOutClient.ClientController.sendTCP(new EditorBlockMsg(i, j, currentLayer, null,
                    getMap().getDimension()));
            }
        }
    }

    private Block getCurrentBlock()
    {
        if (cutterEnabled.isChecked())
        {
            return null;
        }

        return currentBlock;
    }

    private void placeBlockCircle(int x, int y)
    {
        if (!isLayerEnabled()) return;

        Block currentBlock = getCurrentBlock();

        for (int i = -shapeSize; i <= shapeSize; i++)
        {
            for (int j = -shapeSize; j <= shapeSize; j++)
            {
                int pX = x + i, pY = y + j;

                if (Vector2.dst(pX, pY, x, y) > shapeSize) continue;

                BrainOutClient.ClientController.sendTCP(new EditorBlockMsg(
                    pX, pY, currentLayer, currentBlock, getMap().getDimension()));
            }
        }
    }

    private void copyBlock(int x1, int y1, int x2, int y2, int toX, int toY, boolean copy)
    {
        if (!isLayerEnabled()) return;

        if (toX == x1 && toY == y1) return;

        for (int i = x1; i <= x2; i++)
        {
            for (int j = y1; j <= y2; j++)
            {
                int tX = toX + i - x1;
                int tY = toY + j - y1;

                BlockData blockData = getMap().getBlock(i, j, currentLayer);

                if (blockData != null && !blockData.isCopyable()) continue;

                Block blockAt = blockData != null ? blockData.getCreator() : null;
                BrainOutClient.ClientController.sendTCP(new EditorBlockMsg(
                    tX, tY, currentLayer, blockAt, getMap().getDimension()));
            }
        }

        if (!copy)
        {
            removeBlocks(x1, y1, x2, y2);
        }
    }

    private void placeBlockLine(int x1, int y1, int x2, int y2)
    {
        if (!isLayerEnabled()) return;

        Block currentBlock = getCurrentBlock();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;

        int err = dx - dy;

        while (true)
        {
            BrainOutClient.ClientController.sendTCP(new EditorBlockMsg(
                x1, y1, currentLayer, currentBlock, getMap().getDimension()));

            if (x1 == x2 && y1 == y2) {
                break;
            }

            int e2 = 2 * err;

            if (e2 > -dy) {
                err = err - dy;
                x1 = x1 + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                y1 = y1 + sy;
            }
        }
    }

    private void placeBlockRectangle(int aX, int aY, int bX, int bY)
    {
        if (!isLayerEnabled()) return;

        Block currentBlock = getCurrentBlock();

        int x1 = Math.min(aX, bX),
            y1 = Math.min(aY, bY),
            x2 = Math.max(aX, bX),
            y2 = Math.max(aY, bY);

        for (int i = x1; i <= x2; i++)
        {
            for (int j = y1; j <= y2; j++)
            {
                BrainOutClient.ClientController.sendTCP(new EditorBlockMsg(
                    i, j, currentLayer, currentBlock, getMap().getDimension()));
            }
        }
    }

    @Override
    public void mouseMove(Vector2 pos)
    {
        int newPosX = (int)pos.x;
        int newPosY = (int)pos.y;

        if (newPosX != currentPosX || newPosY != currentPosY)
        {
            currentPosX = newPosX;
            currentPosY = newPosY;

            int chunkX = currentPosX / Constants.Core.CHUNK_SIZE;
            int chunkY = currentPosY / Constants.Core.CHUNK_SIZE;

            getMenu().getStats().setText(getMap().getName() + " x:" + currentPosX + " y:" + currentPosY +
                    " chunk: x " + chunkX + " y" + chunkY);
        }
    }

    @Override
    public void touchMove(Vector2 pos)
    {
        currentPosX = (int)pos.x;
        currentPosY = (int)pos.y;

        if (currentPosX != lastPosX || currentPosY != lastPosY)
        {
            switch (subMode)
            {
                case circle:
                {
                    placeBlockCircle(currentPosX, currentPosY);

                    lastPosX = currentPosX;
                    lastPosY = currentPosY;
                    break;
                }
            }
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

        switch (subMode)
        {
            case circle:
            {
                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.circle(currentPosX + 0.5f, currentPosY + 0.5f, shapeSize + 0.5f, 20);
                break;
            }
            case rectangle:
            {
                switch (state)
                {
                    case placed:
                    {
                        int x1 = Math.min(lastPosX, currentPosX),
                            y1 = Math.min(lastPosY, currentPosY),
                            x2 = Math.max(lastPosX, currentPosX),
                            y2 = Math.max(lastPosY, currentPosY);

                        shapeRenderer.setColor(Color.YELLOW);
                        shapeRenderer.rect(x1, y1,
                            x2 - x1 + 1f, y2 - y1 + 1f);

                        break;
                    }
                }
                break;
            }
            case line:
            {
                switch (state)
                {
                    case placed:
                    {
                        shapeRenderer.setColor(Color.RED);
                        shapeRenderer.circle(currentPosX + 0.5f, currentPosY + 0.5f, 0.25f, 10);
                        shapeRenderer.circle(lastPosX + 0.5f, lastPosY + 0.5f, 0.25f, 10);

                        shapeRenderer.setColor(Color.WHITE);
                        shapeRenderer.line(lastPosX + 0.5f, lastPosY + 0.5f,
                            currentPosX + 0.5f, currentPosY + 0.5f);

                        break;
                    }
                }
                break;
            }
            case select:
            {
                switch (state)
                {
                    case placed:
                    {
                        int x1 = Math.min(lastPosX, currentPosX),
                                y1 = Math.min(lastPosY, currentPosY),
                                x2 = Math.max(lastPosX, currentPosX),
                                y2 = Math.max(lastPosY, currentPosY);

                        shapeRenderer.setColor(Color.GREEN);
                        shapeRenderer.rect(x1, y1,
                                x2 - x1 + 1f, y2 - y1 + 1f);

                        break;
                    }
                    case moving:
                    {
                        int selectionWidth = selectionX2 - selectionX1,
                            selectionHeight = selectionY2 - selectionY1;

                        if (isCopy())
                        {
                            shapeRenderer.setColor(Color.CYAN);
                        }
                        else
                        {
                            shapeRenderer.setColor(Color.YELLOW);
                        }

                        shapeRenderer.rect(currentPosX - movingOffsetX, currentPosY - movingOffsetY,
                                selectionWidth + 1, selectionHeight + 1);
                    }
                    case selected:
                    {
                        float coef = ((float)Math.sin(counter * 10f) + 1f) / 8f;

                        shapeRenderer.setColor(Color.GREEN);
                        shapeRenderer.rect(selectionX1 - coef, selectionY1 - coef,
                                selectionX2 - selectionX1 + 1f + coef * 2f, selectionY2 - selectionY1 + 1f + coef * 2f);

                        break;
                    }
                }

                break;
            }
        }

        shapeRenderer.end();

        batch.begin();
    }

    public static int getCurrentLayer()
    {
        return BlocksEditorMode.currentLayer;
    }

    public static void setCurrentLayer(int currentLayer)
    {
        BlocksEditorMode.currentLayer = currentLayer;
    }

    private boolean isCopy()
    {
        return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
    }

    @Override
    public void update(float dt)
    {
        counter += dt;
    }

    public void setSubMode(SubMode subMode)
    {
        this.subMode = subMode;
    }
}
