package com.desertkun.brainout.editor.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.data.EditorMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;

import java.util.regex.Pattern;

public class MapNewDimensionMenu extends Menu
{
    private final EditorMap map;
    private TextField name;
    private TextField widthInput;
    private TextField heightInput;
    private NewDimensionCallback callback;

    private static Pattern DIMENSION_PATTERN = Pattern.compile("([a-z0-9-]{3,})");
    private TextButton create;

    public interface NewDimensionCallback
    {
        void create(String name, int w, int h);
    }

    public MapNewDimensionMenu(EditorMap map, NewDimensionCallback callback)
    {
        this.map = map;
        this.callback = callback;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(L.get("EDITOR_NEW_DIMENSION"), BrainOutClient.Skin, "title-yellow");

            header.add(title).expandX().row();
            data.add(header).expandX().fillX().row();
        }

        Table body = new Table(BrainOutClient.Skin);
        body.setBackground("form-default");

        {
            Table sizes = new Table();

            Label nameLabel = new Label("Name", BrainOutClient.Skin, "title-small");
            Label widthLabel = new Label("Width", BrainOutClient.Skin, "title-small");
            Label heightLabel = new Label("Height", BrainOutClient.Skin, "title-small");

            name = new TextField(nextDimensionName(),
                BrainOutClient.Skin, "edit-default");

            name.addListener(new ChangeListener()
            {
                @Override
                public void changed(ChangeEvent event, Actor actor)
                {
                    if (validateName())
                    {
                        create.setDisabled(false);
                    }
                    else
                    {
                        create.setDisabled(true);
                    }
                }
            });

            widthInput = new TextField("1", BrainOutClient.Skin, "edit-default");

            heightInput = new TextField("1", BrainOutClient.Skin, "edit-default");

            sizes.add(nameLabel).width(100).pad(8);
            sizes.add(name).expandX().fillX().pad(8).row();
            sizes.add(widthLabel).width(100).pad(8);
            sizes.add(widthInput).expandX().fillX().pad(8).row();
            sizes.add(heightLabel).width(100).pad(8);
            sizes.add(heightInput).expandX().fillX().pad(8).row();

            body.add(sizes).width(400).pad(16).fillX().row();
        }

        {
            Table buttons = new Table();

            {
                create = new TextButton(L.get("MENU_CREATE"), BrainOutClient.Skin, "button-green");

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
                TextButton cancel = new TextButton(L.get("MENU_CANCEL"), BrainOutClient.Skin, "button-default");

                cancel.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        canceled();
                    }
                });

                buttons.add(cancel).size(128, 32).padLeft(8).row();
            }

            body.add(buttons).expandX().fillX().pad(8).padTop(0).row();
        }

        data.add(body).expand().fill().row();

        return data;
    }

    private boolean validateName()
    {
        String dimensionName = name.getText();
        return DIMENSION_PATTERN.matcher(dimensionName).matches() && Map.Get(dimensionName) == null;
    }

    private String nextDimensionName()
    {
        for (int i = 1; i < 1000; i++)
        {
            String test = map.getName() + "-" + i;

            if (Map.Get(test) != null)
                continue;

            return test;
        }

        return "default-new";
    }

    private void doCreate()
    {
        if (create.isDisabled())
            return;

        int resizeW, resizeH;

        try
        {
            resizeW = Integer.valueOf(widthInput.getText());
            resizeH = Integer.valueOf(heightInput.getText());
        }
        catch (NumberFormatException ignored)
        {
            canceled();
            return;
        }

        pop();

        callback.create(name.getText(), resizeW, resizeH);
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
