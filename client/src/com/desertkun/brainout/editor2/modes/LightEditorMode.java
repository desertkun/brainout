package com.desertkun.brainout.editor2.modes;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.common.msg.client.editor2.Editor2ActiveAddMsg;
import com.desertkun.brainout.common.msg.client.editor2.Editor2ActiveRemoveMsg;
import com.desertkun.brainout.content.active.Light;
import com.desertkun.brainout.content.components.Editor2EnabledComponent;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.LightData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.editor2.Editor2Menu;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Tooltip;

public class LightEditorMode extends EditorMode
{
    private boolean cutterEnabled;
    private Light light;
    private ImageButton cutterButton;

    private final float ClosestDistance = 16.0f;

    private Color cannotAddColor = new Color(1f, 0f, 0f, 1f);
    private Color addLightColor = new Color(1f, 1f, 1f, 0.5f);
    private Color remoteLightColor = new Color(1f, 0f, 0f, 0.75f);
    private Vector2 position;
    private ActiveData closestActive;

    public LightEditorMode(Editor2Menu menu)
    {
        super(menu);

        position = new Vector2();
    }

    @Override
    public void init()
    {
        super.init();

        light = BrainOutClient.ContentMgr.queryOneContentTpl(Light.class,
            check -> check.hasComponent(Editor2EnabledComponent.class));
    }

    @Override
    public boolean mouseMove(Vector2 position)
    {
        this.position.set(position);
        updateActiveDatas();

        return true;
    }

    private void updateActiveDatas()
    {
        Map map = Map.Get(getMenu().getDimension());

        closestActive = map.getClosestActive(8, position.x, position.y, LightData.class, activeData -> true);
    }

    @Override
    public boolean mouseDown(Vector2 position, int button)
    {
        this.position.set(position);
        updateActiveDatas();

        if (isCutterEnabled())
        {
            if (closestActive != null)
            {
                BrainOutClient.ClientController.sendTCP(new Editor2ActiveRemoveMsg(closestActive));
            }
        }
        else
        {
            if (closestActive == null)
            {
                BrainOutClient.ClientController.sendTCP(new Editor2ActiveAddMsg(light, position.x, position.y, getMenu().getDimension()));
            }
        }

        return true;
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        renderLights(batch);

        batch.end();

        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        if (isCutterEnabled())
        {
            shapeRenderer.setColor(remoteLightColor);

            if (closestActive != null)
            {
                shapeRenderer.circle(closestActive.getX(), closestActive.getY(), 1, 16);
            }
            else
            {
                shapeRenderer.circle(position.x, position.y, 1, 16);
            }
        }
        else
        {
            if (closestActive != null)
            {
                shapeRenderer.setColor(cannotAddColor);
                shapeRenderer.circle(position.x, position.y, ClosestDistance / 3.0f, 32);
            }
            else
            {
                shapeRenderer.setColor(addLightColor);
                shapeRenderer.circle(position.x, position.y, ClosestDistance / 3.0f, 32);
            }
        }

        shapeRenderer.end();

        batch.begin();
    }

    private void renderLights(Batch batch)
    {
        Map map = Map.Get(getMenu().getDimension());

        if (map == null)
            return;

        for (ObjectMap.Entry<Integer, ActiveData> entry : map.getActives())
        {
            ActiveData activeData = entry.value;

            if (!(activeData instanceof LightData))
                continue;

            float x = activeData.getX(), y = activeData.getY();
            float size = 2;

            if (activeData.getContent().hasComponent(IconComponent.class))
            {
                IconComponent iconComponent = activeData.getContent().getComponent(IconComponent.class);
                TextureAtlas.AtlasRegion icon = iconComponent.getIcon();
                batch.draw(icon, x - size / 2.0f, y - size / 2.0f, size, size);
            }

        }

    }

    @Override
    public void selected()
    {
        cutterEnabled = false;
    }

    public boolean isCutterEnabled()
    {
        return cutterEnabled;
    }

    public void setCutterEnabled(boolean cutterEnabled)
    {
        this.cutterEnabled = cutterEnabled;
    }

    @Override
    public void renderPanels(Table panels)
    {
        {
            Table panel = new Table(BrainOutClient.Skin);
            panel.setBackground("buttons-group");
            renderLightsPanel(panel);
            panels.add(panel).padRight(8);
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

    private void renderLightsPanel(Table panel)
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
    }

    @Override
    public ID getID()
    {
        return ID.light;
    }
}
