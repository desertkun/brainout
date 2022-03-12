package com.desertkun.brainout.editor.widgets;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.data.EditorMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.editor.menu.EditorMenu;
import com.desertkun.brainout.editor.modes.ActivesEditorMode;
import com.desertkun.brainout.editor.modes.BlocksEditorMode;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.widgets.Widget;

public class LayersWidget extends Widget
{
    private final EditorMap map;
    private final EditorMenu menu;
    private Table layers;
    private ImageButton switchButton;
    private boolean opened;
    private Array<EditorLayer> editorLayers;

    public abstract class EditorLayer
    {
        protected final int layer;
        private String name;

        public EditorLayer(int layer, String name)
        {
            this.name = name;

            this.layer = layer;
        }

        public abstract boolean isVisible();
        public abstract boolean setVisible(boolean visible);

        public abstract boolean isActive();
        public abstract boolean setActive(boolean enabled);

        public abstract boolean isEnabled();
        public abstract boolean setEnabled(boolean active);
    }

    public class BlocksLayer extends EditorLayer
    {
        public BlocksLayer(int layer, String name)
        {
            super(layer, name);
        }

        public BlocksEditorMode getMode()
        {
            EditorMenu editorMenu = ((EditorMenu) getMenu());

            if (editorMenu.getCurrentMode() instanceof BlocksEditorMode)
            {
                return ((BlocksEditorMode) editorMenu.getCurrentMode());
            }

            return null;
        }

        @Override
        public boolean isActive()
        {
            if (!(getMenu().getCurrentMode() instanceof BlocksEditorMode))
            {
                return false;
            }

            return BlocksEditorMode.getCurrentLayer() == layer;
        }

        @Override
        public boolean setActive(boolean active)
        {
            if (!(getMenu().getCurrentMode() instanceof BlocksEditorMode))
            {
                return false;
            }

            if (active)
            {
                BlocksEditorMode.setCurrentLayer(layer);
            }

            return true;
        }

        @Override
        public boolean isEnabled()
        {
            if (!(getMenu().getCurrentMode() instanceof BlocksEditorMode))
            {
                return false;
            }

            return map.getBlocksLayer(layer).enabled;
        }

        @Override
        public boolean setEnabled(boolean enabled)
        {
            if (!(getMenu().getCurrentMode() instanceof BlocksEditorMode))
            {
                return false;
            }

            map.getBlocksLayer(layer).enabled = enabled;

            return true;
        }

        @Override
        public boolean isVisible()
        {
            return map.getBlocksLayer(layer).visible;
        }

        @Override
        public boolean setVisible(boolean visible)
        {
            map.getBlocksLayer(layer).visible = visible;

            return true;
        }
    }

    public class ActivesLayer extends EditorLayer
    {
        public ActivesLayer(int layer, String name)
        {
            super(layer, name);
        }

        @Override
        public boolean isVisible()
        {
            return map.getActives().getRenderLayer(layer).isVisible();
        }

        @Override
        public boolean setVisible(boolean visible)
        {
            map.getActives().getRenderLayer(layer).setVisible(visible);

            return true;
        }

        @Override
        public boolean isActive()
        {
            if (!(getMenu().getCurrentMode() instanceof ActivesEditorMode))
            {
                return false;
            }

            return ActivesEditorMode.getCurrentLayer() == layer;
        }

        @Override
        public boolean setActive(boolean active)
        {
            if (!(getMenu().getCurrentMode() instanceof ActivesEditorMode))
            {
                return false;
            }

            if (active)
            {
                ActivesEditorMode.setCurrentLayer(layer);
            }

            return true;
        }

        @Override
        public boolean isEnabled()
        {
            if (!(getMenu().getCurrentMode() instanceof ActivesEditorMode))
            {
                return false;
            }

            return map.getActives().getRenderLayer(layer).isEnabled();
        }

        @Override
        public boolean setEnabled(boolean active)
        {
            if (!(getMenu().getCurrentMode() instanceof ActivesEditorMode))
            {
                return false;
            }

            map.getActives().getRenderLayer(layer).setEnabled(active);

            return true;
        }
    }

    public class LightsLayer extends EditorLayer
    {
        public LightsLayer(int layer, String name)
        {
            super(layer, name);
        }

        @Override
        public boolean isVisible()
        {
            return map.isLightsVisible();
        }

        @Override
        public boolean setVisible(boolean visible)
        {
            map.setLightsVisible(visible);
            return true;
        }

        @Override
        public boolean isActive()
        {
            return false;
        }

        @Override
        public boolean setActive(boolean enabled)
        {
            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return false;
        }

        @Override
        public boolean setEnabled(boolean active)
        {
            return false;
        }
    }

    public LayersWidget(Map map, EditorMenu menu, float x, float y, float w, float h)
    {
        super(x, y, w, h);

        this.map = (EditorMap)map;
        this.menu = menu;
        this.opened = false;
        this.editorLayers = new Array<>();

        initLayers();
    }

    private void addLayer(EditorLayer editorLayer)
    {
        editorLayers.add(editorLayer);
    }

    private void initLayers()
    {
        addLayer(new LightsLayer(0, "EDITOR_ACTIVES_LIGHTS"));
        addLayer(new ActivesLayer(2, "EDITOR_ACTIVES_LAYER_2"));
        addLayer(new ActivesLayer(1, "EDITOR_ACTIVES_LAYER_1"));
        addLayer(new BlocksLayer(1, "EDITOR_BLOCKS_LAYER_FOREGROUND"));
        addLayer(new ActivesLayer(3, "EDITOR_ACTIVES_LAYER_1TOP"));
        addLayer(new BlocksLayer(0, "EDITOR_BLOCKS_LAYER_BACKGROUND"));
        addLayer(new ActivesLayer(0, "EDITOR_ACTIVES_LAYER_0"));
    }

    @Override
    public void init()
    {
        align(Align.bottom);

        this.layers = new Table();

        this.switchButton = new ImageButton(BrainOutClient.Skin, "button-editor-widgets-show");

        switchButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(Menu.MenuSound.select);

                opened = !opened;

                switchButton.setStyle(BrainOutClient.Skin.get(
                    opened ? "button-editor-widgets-hide" : "button-editor-widgets-show",
                    ImageButton.ImageButtonStyle.class));

                updateLayers();
            }
        });

        add(switchButton).expandX().right().row();

        add(layers).expandX().fillX().row();

        updateLayers();
    }

    public void updateLayers()
    {
        layers.clear();

        if (opened)
        {
            layers.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));

            for (EditorLayer editorLayer : editorLayers)
            {
                Table line = new Table();

                final EditorLayer layer = editorLayer;

                ImageButton enabled = new ImageButton(BrainOutClient.Skin, "button-editor-layer-lock");

                enabled.setChecked(layer.isEnabled());

                enabled.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {

                        if (layer.setEnabled(enabled.isChecked()))
                        {
                            Menu.playSound(Menu.MenuSound.select);
                        }
                        else
                        {
                            Menu.playSound(Menu.MenuSound.denied);

                            enabled.setChecked(!enabled.isChecked());
                        }
                    }
                });

                ImageButton visibility = new ImageButton(BrainOutClient.Skin, "button-editor-layer-visibility");

                visibility.setChecked(layer.isVisible());

                visibility.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        if (layer.setVisible(visibility.isChecked()))
                        {
                            Menu.playSound(Menu.MenuSound.select);
                        }
                        else
                        {
                            Menu.playSound(Menu.MenuSound.denied);

                            visibility.setChecked(!visibility.isChecked());
                        }
                    }
                });

                line.add(enabled);
                line.add(visibility);

                TextButton title = new TextButton(L.get(layer.name), BrainOutClient.Skin,
                        "button-checkable");
                title.setChecked(layer.isActive());

                title.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {

                        if (layer.setActive(title.isChecked()))
                        {
                            Menu.playSound(Menu.MenuSound.select);
                        }
                        else
                        {
                            Menu.playSound(Menu.MenuSound.denied);

                            title.setChecked(!title.isChecked());
                        }

                        updateLayers();
                    }
                });

                line.add(title).expandX().fillX().pad(2).height(30).row();

                layers.add(line).expandX().fillX().row();
            }
        }
        else
        {
            layers.setBackground((Drawable)null);
        }
    }

    public EditorMenu getMenu()
    {
        return menu;
    }
}
