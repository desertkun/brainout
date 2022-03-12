package com.desertkun.brainout.editor2.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.common.msg.client.editor2.BlockRectMsg;
import com.desertkun.brainout.common.msg.client.editor2.MultipleBlocksMsg;
import com.desertkun.brainout.common.msg.client.editor2.SingleBlockMsg;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.components.Editor2EnabledComponent;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.editor2.Editor2Menu;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Tooltip;

import java.util.Comparator;

public class BlocksEditorMode extends EditorMode
{
    private final Array<Block> content;
    private Shape currentShape;
    private Block currentBlock;

    private boolean cutterEnabled;

    private Color selectedBlockColor = new Color(1f, 1f, 1f, 0.5f);
    private Color selectedCutterBlockColor = new Color(1f, 0f, 0f, 0.75f);

    private int mouseBlockX, mouseBlockY, mouseBlockStartX, mouseBlockStartY;
    private ImageButton cutterButton;
    private ObjectSet<PlacementBlock> placementBlocks;

    private class PlacementBlock
    {
        private int x, y;

        public PlacementBlock(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode()
        {
            Map map = Map.Get(getMenu().getDimension());
            return y * map.getWidth() + x;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof PlacementBlock)
            {
                return ((PlacementBlock) obj).x == x && ((PlacementBlock) obj).y == y;
            }

            return false;
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }
    }

    public BlocksEditorMode(Editor2Menu menu)
    {
        super(menu);

        currentShape = Shape.circle0;
        placementBlocks = new ObjectSet<>();

        content = BrainOutClient.ContentMgr.queryContent(Block.class, this::validateBlock);
        content.sort(Comparator.comparing(Content::getID));
    }

    public enum Shape
    {
        circle3,
        circle0,
        rect
    }

    private boolean validateBlock(Block block)
    {
        return block.hasComponent(IconComponent.class) && block.hasComponent(Editor2EnabledComponent.class);
    }

    @Override
    public void selected()
    {
        cutterEnabled = false;

        if (currentBlock == null)
        {
            currentBlock = BrainOutClient.ContentMgr.get("core", Block.class);
        }
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

    public String getDimension()
    {
        return getMenu().getDimension();
    }

    @Override
    public boolean mouseUp(Vector2 position, int button)
    {
        switch (getCurrentShape())
        {
            case circle0:
            case circle3:
            {
                if (placementBlocks.size == 0)
                    return true;

                if (placementBlocks.size == 1)
                {
                    BrainOutClient.ClientController.sendTCP(
                        new SingleBlockMsg(getDimension(), getCurrentBlock(), placementBlocks.first().getX(),
                            placementBlocks.first().getY()));
                }
                else
                {
                    Queue<MultipleBlocksMsg.Point> points = new Queue<>();

                    for (PlacementBlock block : placementBlocks)
                    {
                        points.addLast(new MultipleBlocksMsg.Point(block.getX(), block.getY()));
                    }

                    BrainOutClient.ClientController.sendTCP(
                        new MultipleBlocksMsg(getDimension(), getCurrentBlock(), points));
                }

                placementBlocks.clear();

                break;
            }
            case rect:
            {
                int x = Math.min(mouseBlockStartX, mouseBlockX),
                    y = Math.min(mouseBlockStartY, mouseBlockY),
                    w = Math.max(mouseBlockStartX, mouseBlockX) - x + 1,
                    h = Math.max(mouseBlockStartY, mouseBlockY) - y + 1;

                BrainOutClient.ClientController.sendTCP(
                    new BlockRectMsg(getDimension(), getCurrentBlock(), x, y, w, h));

                return true;
            }
        }

        return false;
    }

    private Block getCurrentBlock()
    {
        return cutterEnabled ? null : currentBlock;
    }

    @Override
    public boolean mouseDown(Vector2 position, int button)
    {
        switch (getCurrentShape())
        {
            case circle0:
            case circle3:
            {
                placementBlocks.clear();
                addPlacement(position);

                return true;
            }
            case rect:
            {
                mouseBlockStartX = (int)(position.x);
                mouseBlockStartY = (int)(position.y);

                return true;
            }
        }

        return false;
    }

    private void addPlacement(Vector2 position)
    {
        int x = (int)(position.x), y = (int)(position.y);
        switch (getCurrentShape())
        {
            case circle0:
            {
                placementBlocks.add(new PlacementBlock(x, y));

                break;
            }
            case circle3:
            {
                for (int j = y - 3; j <= y + 3; j++)
                {
                    for (int i = x - 3; i <= x + 3; i++)
                    {
                        if (Vector2.dst(x, y, i, j) > 3)
                            continue;

                        placementBlocks.add(new PlacementBlock(i, j));
                    }
                }

                break;
            }
        }
    }

    @Override
    public boolean mouseMove(Vector2 position)
    {
        mouseBlockX = (int)(position.x);
        mouseBlockY = (int)(position.y);

        processPlacement(position);

        return true;
    }

    @Override
    public boolean mouseDrag(Vector2 position, int button)
    {
        mouseBlockX = (int)(position.x);
        mouseBlockY = (int)(position.y);

        processPlacement(position);

        return true;
    }

    private void processPlacement(Vector2 position)
    {
        switch (getDragState())
        {
            case dragging:
            {
                switch (getCurrentShape())
                {
                    case circle0:
                    case circle3:
                    {
                        addPlacement(position);

                        break;
                    }
                }

                break;
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
        shapeRenderer.setColor(isCutterEnabled() ? selectedCutterBlockColor : selectedBlockColor);
        renderCurrentShape();

        batch.begin();
    }

    public boolean isCutterEnabled()
    {
        return cutterEnabled;
    }

    private void renderCurrentShape()
    {
        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;

        switch (getCurrentShape())
        {
            case circle0:
            {
                if (placementBlocks.size > 0)
                {
                    renderPlacements();
                }

                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.rect(mouseBlockX, mouseBlockY, 1, 1);
                shapeRenderer.end();

                break;
            }
            case circle3:
            {
                if (placementBlocks.size > 0)
                {
                    renderPlacements();
                }

                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.circle(mouseBlockX + 0.5f, mouseBlockY + 0.5f, 3, 16);
                shapeRenderer.end();

                break;
            }
            case rect:
            {
                switch (getDragState())
                {
                    case dragging:
                    {
                        int x = Math.min(mouseBlockStartX, mouseBlockX),
                            y = Math.min(mouseBlockStartY, mouseBlockY),
                            w = Math.max(mouseBlockStartX, mouseBlockX) - x + 1,
                            h = Math.max(mouseBlockStartY, mouseBlockY) - y + 1;

                        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                        shapeRenderer.rect(x, y, w, h);
                        shapeRenderer.end();
                        break;
                    }
                    default:
                    {
                        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                        shapeRenderer.rect(mouseBlockX, mouseBlockY, 1, 1);
                        shapeRenderer.end();
                        break;
                    }
                }

                break;
            }
        }
    }

    private void renderPlacements()
    {
        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (PlacementBlock block : placementBlocks)
        {
            shapeRenderer.rect(block.getX(), block.getY(), 1, 1);
        }

        shapeRenderer.end();
    }

    @Override
    public void renderPanels(Table toolbar)
    {
        {
            Table panel = new Table(BrainOutClient.Skin);
            panel.setBackground("buttons-group");
            renderShapesPanel(panel);
            toolbar.add(panel).padRight(8);
        }
        {
            Table panel = new Table(BrainOutClient.Skin);
            panel.setBackground("buttons-group");
            renderBlocksPanel(panel);
            toolbar.add(panel).padRight(8);
        }
    }

    private void renderBlocksPanel(Table panel)
    {
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

        ButtonGroup<Button> blocksGroup = new ButtonGroup<>();
        blocksGroup.setMaxCheckCount(1);
        blocksGroup.setMinCheckCount(1);

        for (Block block : content)
        {
            IconComponent icon = block.getComponent(IconComponent.class);

            if (icon == null || icon.getIcon() == null)
                return;

            final Button button = new Button(BrainOutClient.Skin, "button-notext-checkable");
            Tooltip.RegisterToolTip(button, block.getTitle().get(), getMenu());

            Image image = new Image(icon.getIcon());
            image.setTouchable(Touchable.disabled);
            image.setScaling(Scaling.none);
            button.add(image);

            if (currentBlock == block)
                button.setChecked(true);

            button.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(Menu.MenuSound.select);
                    currentBlock = block;
                }
            });

            panel.add(button).size(30, 30).padRight(2);
            blocksGroup.add(button);
        }
    }

    private void setCutterEnabled(boolean enabled)
    {
        cutterEnabled = enabled;
    }

    private void renderShapesPanel(Table panel)
    {
        ButtonGroup<ImageButton> shapeGroup = new ButtonGroup<>();

        renderShape(panel, shapeGroup, Shape.circle0, "button-editor-shape-1", "EDITOR_BLOCKS_BRUSH_CIRCLE");
        renderShape(panel, shapeGroup, Shape.circle3, "button-editor-shape-3", "EDITOR_BLOCKS_BRUSH_CIRCLE");
        renderShape(panel, shapeGroup, Shape.rect, "button-editor-shape-rect", "EDITOR_BLOCKS_BRUSH_RECTANGLE");
    }

    private void renderShape(Table panel, ButtonGroup<ImageButton> shapeGroup, Shape shape, String icon, String title)
    {
        final ImageButton button = new ImageButton(BrainOutClient.Skin, icon);
        Tooltip.RegisterToolTip(button, L.get(title), getMenu());

        if (getCurrentShape() == shape)
            button.setChecked(true);

        button.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(Menu.MenuSound.select);

                setCurrentShape(shape);
            }
        });

        panel.add(button);
        shapeGroup.add(button);
    }

    public Shape getCurrentShape()
    {
        return currentShape;
    }

    private void setCurrentShape(Shape shape)
    {
        this.currentShape = shape;
    }

    @Override
    public ID getID()
    {
        return ID.blocks;
    }
}
