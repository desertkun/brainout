package com.desertkun.brainout.editor.menu;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.data.EditorMap;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class ResizeMapMenu extends Menu
{
    private final EditorMap map;
    private TextField widthInput;
    private TextField heightInput;
    private ResizeCallback callback;

    private int anchorX, anchorY;

    public interface ResizeCallback
    {
        void resize(int w, int h, int aX, int aY);
    }

    public ResizeMapMenu(EditorMap map, ResizeCallback callback)
    {
        this.map = map;

        anchorX = 0;
        anchorY = 0;

        this.callback = callback;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(L.get("EDITOR_RESIZE_MAP"), BrainOutClient.Skin, "title-yellow");

            header.add(title).expandX().row();
            data.add(header).expandX().fillX().row();
        }

        Table body = new Table(BrainOutClient.Skin);
        body.setBackground("form-default");

        {
            Table sizes = new Table();

            Label widthLabel = new Label("Width:", BrainOutClient.Skin, "title-small");
            Label heightLabel = new Label("Height:", BrainOutClient.Skin, "title-small");

            widthInput = new TextField(String.valueOf(map.getBlocks().getBlockWidth()),
                BrainOutClient.Skin, "edit-default");

            heightInput = new TextField(String.valueOf(map.getBlocks().getBlockHeight()),
                BrainOutClient.Skin, "edit-default");

            sizes.add(widthLabel).pad(8);
            sizes.add(widthInput).pad(8).row();
            sizes.add(heightLabel).pad(8);
            sizes.add(heightInput).pad(8).row();

            body.add(sizes).row();
        }

        {
            Table anchor = new Table();

            ButtonGroup<Button> buttons = new ButtonGroup<>();
            buttons.setMinCheckCount(1);
            buttons.setMaxCheckCount(1);

            for (int j = 1; j >= -1; j--)
            {
                for (int i = -1; i <= 1; i++)
                {
                    final int _x = i, _y = j;
                    Button checkBox = new Button(BrainOutClient.Skin, "button-checkbox");
                    anchor.add(checkBox).pad(2, -2, 2, -2);

                    checkBox.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);

                            anchorX = _x;
                            anchorY = _y;
                        }
                    });

                    buttons.add(checkBox);

                    if (i == 0 && j == 0)
                    {
                        checkBox.setChecked(true);
                    }
                }

                anchor.row();
            }

            body.add(anchor).pad(16).expandX().center().row();
        }

        {
            Table buttons = new Table();

            {
                TextButton resize = new TextButton(L.get("MENU_APPLY"), BrainOutClient.Skin, "button-green");

                resize.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        doResize();
                    }
                });

                buttons.add(resize).size(128, 32).padRight(8);
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

    private void doResize()
    {
        int resizeW = map.getBlocks().getBlockWidth(),
            resizeH = map.getBlocks().getBlockHeight();

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

        callback.resize(resizeW, resizeH, anchorX, anchorY);
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }

    private void canceled()
    {
        pop();
    }
}
