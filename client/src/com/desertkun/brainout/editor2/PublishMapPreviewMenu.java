package com.desertkun.brainout.editor2;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.data.Editor2Map;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.mode.GameMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PublishMapPreviewMenu extends Menu
{
    private final File previewFile;
    private final File mapFile;
    private Editor2Map map;
    private Button saveButton;
    private TextField mapName;
    private TextArea mapDescription;
    private ObjectMap<String, CheckBox> gameModes;
    private boolean hasPreview;
    private Texture previewTexture;

    public PublishMapPreviewMenu(File previewFile, File mapFile, Editor2Map map)
    {
        this.previewFile = previewFile;
        this.mapFile = mapFile;
        this.map = map;

        try
        {
            generatePreview();
        }
        catch (IOException e)
        {
            hasPreview = false;
        }
    }

    private void generatePreview() throws IOException
    {
        byte[] data = Files.readAllBytes(previewFile.toPath());

        previewTexture = new Texture(new Pixmap(data, 0, data.length), true);
        previewTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear,
                                 Texture.TextureFilter.MipMapLinearLinear);

        hasPreview = true;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(L.get("EDITOR2_PUBLISH_WORKSHOP"), BrainOutClient.Skin, "title-yellow");
            header.add(title).row();

            data.add(header).size(562, 32).row();
        }

        {
            Table contents = new Table(BrainOutClient.Skin);
            contents.setBackground("form-default");
            contents.align(Align.center);

            renderContents(contents);

            data.add(contents).width(564).row();
        }

        {
            Table buttons = new Table();

            {
                TextButton cancel = new TextButton(L.get("MENU_CANCEL"), BrainOutClient.Skin, "button-default");

                cancel.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        close();
                    }
                });

                buttons.add(cancel).expandX().fillX().uniformX().height(64);
            }

            {
                saveButton = new Button(BrainOutClient.Skin, "button-green");
                saveButton.setDisabled(!validate());

                {
                    Image steam = new Image(BrainOutClient.Skin, "steam-icon");
                    steam.setScaling(Scaling.none);
                    saveButton.add(steam).size(32, 32).padRight(16);

                    Label publish = new Label(L.get("EDITOR2_PUBLISH"), BrainOutClient.Skin, "title-small");
                    saveButton.add(publish);
                }


                saveButton.addListener(new ClickOverListener()
                {

                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        if (!validate())
                            return;

                        Menu.playSound(MenuSound.select);

                        save();
                    }
                });

                buttons.add(saveButton).expandX().fillX().uniformX().height(64);
            }

            data.add(buttons).expandX().fillX().row();
        }

        if (BrainOutClient.Env.getGameUser().hasWorkshopLegalTerms())
        {
            Table legal = new Table(BrainOutClient.Skin);
            legal.align(Align.center);

            renderLegal(legal);

            data.add(legal).width(564).padTop(16).row();
        }

        return data;
    }

    private void renderLegal(Table legal)
    {
        {
            Label part1 = new Label(L.get("EDITOR2_SUBMIT_LEGAL_1"), BrainOutClient.Skin, "title-small");
            legal.add(part1).row();
        }

        {
            TextButton part2 = new TextButton(L.get("EDITOR2_SUBMIT_LEGAL_2"), BrainOutClient.Skin,
                "button-text-clear");

            part2.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    BrainOutClient.Env.openURI(BrainOutClient.Env.getGameUser().getWorkshopLegalTermsLink());
                }
            });

            legal.add(part2);
        }
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        map = null;

        if (previewTexture != null)
        {
            previewTexture.dispose();
            previewTexture = null;
        }
    }

    private boolean validate()
    {
        String name = mapName.getText();
        if (!Constants.Editor.NAME_PATTERN.matcher(name).matches())
            return false;

        if (mapDescription.getText().length() > 300)
            return false;

        return true;
    }

    private void save()
    {
        ObjectSet<String> modes = new ObjectSet<>();

        for (ObjectMap.Entry<String, CheckBox> entry : gameModes)
        {
            if (entry.value.isChecked())
                modes.add(entry.key);
        }

        popMeAndPushMenu(new PublishMapMenu(
            previewFile, mapFile, mapName.getText(), mapDescription.getText(), modes));
    }

    private void close()
    {
        pop();
    }

    private void renderContents(Table contents)
    {
        if (hasPreview)
        {
            Image preview = new Image(previewTexture);
            preview.setScaling(Scaling.fit);
            contents.add(preview).expandX().fillX().height(180).pad(8).row();
        }

        {
            Label title = new Label(L.get("MENU_MAP_NAME"), BrainOutClient.Skin, "title-small");
            contents.add(title).pad(8).padBottom(0).row();

            mapName = new TextField("", BrainOutClient.Skin, "edit-default");
            contents.add(mapName).expandX().fillX().pad(16).row();

            setKeyboardFocus(mapName);

            mapName.addListener(new ChangeListener()
            {
                @Override
                public void changed(ChangeEvent event, Actor actor)
                {
                    saveButton.setDisabled(!validate());
                }
            });
        }

        {
            Label title = new Label(L.get("MENU_MAP_DESCRIPTION"), BrainOutClient.Skin, "title-small");
            contents.add(title).row();

            mapDescription = new TextArea("", BrainOutClient.Skin, "edit-default");
            mapDescription.setPrefRows(4);
            contents.add(mapDescription).expandX().fillX().pad(16).row();

            mapDescription.addListener(new ChangeListener()
            {
                @Override
                public void changed(ChangeEvent event, Actor actor)
                {
                    saveButton.setDisabled(!validate());
                }
            });
        }

        {
            Label title = new Label(L.get("MENU_MAP_IS_SUITABLE_FOR"), BrainOutClient.Skin, "title-small");
            contents.add(title).row();

            gameModes = new ObjectMap<>();

            Table modes = new Table();
            contents.add(modes).expandX().fillX().pad(16).row();

            int i = 0;

            for (GameMode.ID mode : Constants.Matchmaking.APPROVED_MODES)
            {
                String modeName = mode.toString();

                CheckBox modeCheckbox = new CheckBox(
                    L.get("MODE_" + mode.toString().toUpperCase()),
                    BrainOutClient.Skin, "checkbox-default");
                modeCheckbox.setChecked(true);

                gameModes.put(modeName, modeCheckbox);
                modes.add(modeCheckbox).expandX().pad(2).left();

                i++;

                if (i % 3 == 0)
                {
                    modes.row();
                }
            }
        }
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(0.75f, getBatch());
        super.render();
    }
}
