package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.ConfirmationPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;

import java.util.Comparator;

public class SelectMapMenu extends Menu
{
    private final SelectCallback callback;
    private final FileHandle directory;
    private final String defaultName;
    private Mode mode;
    private Table content;
    private TextField selectFileEdit;
    private TextButton selectButton;

    public enum Mode
    {
        open,
        save
    }

    public interface SelectCallback
    {
        void selected(FileHandle map);
        void cancelled();
    }

    public SelectMapMenu(Mode mode, FileHandle directory, SelectCallback callback)
    {
        this(mode, directory, callback, "");
    }

    public SelectMapMenu(Mode mode, FileHandle directory, SelectCallback callback, String name)
    {
        this.mode = mode;
        this.callback = callback;
        this.directory = directory;
        this.defaultName = name;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();


        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            String titleValue;

            switch (mode)
            {
                case open:
                {
                    titleValue = "Open a Map";
                    break;
                }
                case save:
                default:
                {
                    titleValue = "Save a Map";
                    break;
                }
            }

            Label title = new Label(titleValue, BrainOutClient.Skin, "title-yellow");

            header.add(title).expandX().row();
            data.add(header).expandX().fillX().row();
        }

        Table body = new Table(BrainOutClient.Skin);
        body.setBackground("form-default");

        {
            Label here = new Label("Maps are stored here:", BrainOutClient.Skin, "title-gray");
            body.add(here).expandX().center().pad(8).row();


            Label path = new Label(directory.file().getAbsolutePath(), BrainOutClient.Skin, "title-gray");
            path.setWrap(true);
            path.setAlignment(Align.center);
            body.add(path).expandX().fillX().pad(8).padTop(0).row();
        }

        content = new Table(BrainOutClient.Skin);
        refreshContent();
        ScrollPane scrollPane = new ScrollPane(content, BrainOutClient.Skin, "scroll-default");
        setScrollFocus(scrollPane);
        body.add(scrollPane).size(600, 400).pad(8).row();

        {
            Table buttons = new Table();

            {
                selectFileEdit = new TextField(defaultName, BrainOutClient.Skin, "edit-default");

                selectFileEdit.addListener(new ChangeListener()
                {
                    @Override
                    public void changed(ChangeEvent event, Actor actor)
                    {
                        if (selectButton == null)
                            return;

                        selectButton.setDisabled(!validateSelection());
                    }
                });

                buttons.add(selectFileEdit).expandX().fillX().padRight(8);

                setKeyboardFocus(selectFileEdit);
            }

            {
                String title;
                String style;

                switch (mode)
                {
                    case open:
                    {
                        title = L.get("MENU_OPEN");
                        style = "button-green";
                        break;
                    }
                    case save:
                    default:
                    {
                        title = L.get("MENU_SAVE");
                        style = "button-yellow";
                        break;
                    }
                }

                selectButton = new TextButton(title, BrainOutClient.Skin, style);
                selectButton.setDisabled(!validateSelection());

                selectButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        selected();
                    }
                });

                buttons.add(selectButton).size(128, 32).padRight(8);
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

                buttons.add(cancel).size(128, 32).row();
            }

            body.add(buttons).expandX().fillX().pad(8).padTop(0).row();
        }

        data.add(body).expand().fill().row();


        return data;
    }

    private boolean validateSelection()
    {
        String text = getCurrentSelectionFileName();

        if (text.isEmpty())
            return false;

        switch (mode)
        {
            case open:
            {
                FileHandle child = directory.child(text);

                if (!child.exists())
                    return false;

                break;
            }
        }

        return true;
    }

    private void canceled()
    {
        pop();

        callback.cancelled();
    }

    private String getCurrentSelectionFileName()
    {
        return selectFileEdit.getText() + ".map";
    }

    private void selected()
    {
        if (!validateSelection())
            return;

        String text = getCurrentSelectionFileName();

        switch (mode)
        {
            case save:
            {
                FileHandle child = directory.child(text);

                if (child.exists())
                {
                    pushMenu(new ConfirmationPopup("File exists. Overwrite?")
                    {
                        @Override
                        public void yes()
                        {
                            confirmSelected();
                        }
                    });

                    return;
                }

                break;
            }
        }

        confirmSelected();
    }

    private void confirmSelected()
    {
        pop();
        callback.selected(directory.child(getCurrentSelectionFileName()));
    }

    private void refreshContent()
    {
        content.clearChildren();

        if (!directory.isDirectory())
            return;

        int i = 0;

        Array<FileHandle> handles = new Array<>(directory.list());

        handles.sort(Comparator.comparing(FileHandle::name));

        for (FileHandle handle : handles)
        {
            String fileName = handle.name();

            if (!fileName.endsWith(".map"))
                continue;

            String nameWithoutExtension = handle.nameWithoutExtension();

            Button row = new Button(BrainOutClient.Skin,
                i % 2 == 0 ? "button-row-dark-blue" : "button-row-border-blue");

            {
                Image icon = new Image(BrainOutClient.Skin, "shootmode-single");
                row.add(icon).padLeft(8);
            }

            {
                Label title = new Label(nameWithoutExtension, BrainOutClient.Skin, "title-small");
                row.add(title).expandX().left().padLeft(8);
            }

            row.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    selectFile(nameWithoutExtension);
                }
            });

            row.addListener(new ActorGestureListener()
            {
                @Override
                public void tap(InputEvent event, float x, float y, int count, int button)
                {
                    if (count == 2)
                    {
                        selected();
                    }
                }
            });

            content.add(row).expandX().fillX().height(32).row();

            i++;
        }

        if (i == 0)
        {
            Label noFiles = new Label("No maps so far", BrainOutClient.Skin, "title-gray");

            content.add(noFiles).expandX().center().row();
        }
    }

    public void selectFile(String fileName)
    {
        selectFileEdit.setText(fileName);
        selectButton.setDisabled(!validateSelection());
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
