package com.desertkun.brainout.editor.menu;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.data.EditorMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.ConfirmationPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class MapDimensionsMenu extends Menu
{
    private final EditorMap map;
    private final MapNewDimensionMenu.NewDimensionCallback newDimensionCallback;
    private final DeleteDimensionCallback deleteDimensionCallback;
    private final SwitchDimensionCallback switchDimensionCallback;

    public interface DeleteDimensionCallback
    {
        void delete(String name);
    }

    public interface SwitchDimensionCallback
    {
        void switch_(EditorMap map);
    }

    public MapDimensionsMenu(EditorMap map,
                             MapNewDimensionMenu.NewDimensionCallback newDimensionCallback,
                             DeleteDimensionCallback deleteDimensionCallback,
                             SwitchDimensionCallback switchDimensionCallback)
    {
        this.map = map;
        this.newDimensionCallback = newDimensionCallback;
        this.deleteDimensionCallback = deleteDimensionCallback;
        this.switchDimensionCallback = switchDimensionCallback;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(L.get("EDITOR_MAP_DIMENSIONS"), BrainOutClient.Skin, "title-yellow");

            header.add(title).expandX().row();
            data.add(header).expandX().fillX().row();
        }

        Table body = new Table(BrainOutClient.Skin);
        body.setBackground("form-default");

        Table content = new Table(BrainOutClient.Skin);
        renderDimensions(content);
        ScrollPane scrollPane = new ScrollPane(content, BrainOutClient.Skin, "scroll-default");
        setScrollFocus(scrollPane);
        body.add(scrollPane).size(600, 300).pad(8).row();

        {
            Table buttons = new Table();

            {
                TextButton create = new TextButton(L.get("MENU_CREATE"), BrainOutClient.Skin, "button-green");

                create.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        doCreate();
                    }
                });

                buttons.add(create).size(128, 32).padRight(8);
            }

            {
                TextButton close = new TextButton(L.get("MENU_CLOSE"), BrainOutClient.Skin, "button-default");

                close.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        canceled();
                    }
                });

                buttons.add(close).size(128, 32).padLeft(8).row();
            }

            body.add(buttons).expandX().fillX().pad(8).padTop(0).row();
        }

        data.add(body).expand().fill().row();

        return data;
    }

    private void renderDimensions(Table content)
    {
        int i = 0;

        for (final EditorMap editorMap : Map.All(EditorMap.class))
        {
            Button row = new Button(BrainOutClient.Skin,
                i % 2 == 0 ? "button-row-dark-blue" : "button-row-border-blue");

            {
                Image icon = new Image(BrainOutClient.Skin, "editor-dimensions");
                icon.setScaling(Scaling.none);
                row.add(icon).padLeft(8);
            }

            {
                Label title = new Label(editorMap.getDimension(), BrainOutClient.Skin, "title-small");
                row.add(title).expandX().left().padLeft(8);
            }

            if (editorMap != map)
            {
                row.addListener(new ActorGestureListener()
                {
                    @Override
                    public void tap(InputEvent event, float x, float y, int count, int button)
                    {
                        if (count == 2)
                        {
                            switchTo(editorMap);
                        }
                    }
                });

                if (!editorMap.getDimension().equals("default"))
                {
                    TextButton deleteButton = new TextButton(L.get("MENU_DELETE"), BrainOutClient.Skin, "button-danger");

                    deleteButton.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);

                            pushMenu(new ConfirmationPopup("Are you sure?")
                            {
                                @Override
                                public void yes()
                                {
                                    canceled();
                                    deleteDimensionCallback.delete(editorMap.getDimension());
                                }
                            });
                        }
                    });

                    row.add(deleteButton).size(128, 32).padRight(-6).padLeft(10);
                }
            }

            content.add(row).expandX().fillX().height(32).row();

            i++;
        }
    }

    private void switchTo(EditorMap editorMap)
    {
        canceled();

        switchDimensionCallback.switch_(editorMap);
    }

    private void doCreate()
    {
        pushMenu(new MapNewDimensionMenu(map, newDimensionCallback));
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    private void canceled()
    {
        pop();
    }
}
