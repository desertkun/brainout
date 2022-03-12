package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.online.RoomSettings;

public class QuickPlayOptionsMenu extends PlayGameOptionsMenu
{
    public interface Callback
    {
        void selected(String name, RoomSettings settings, QuickPlayOptionsMenu menu);
        void cancelled();
    }

    private final String title;
    private final Callback callback;
    private boolean extended;
    private static String Name = "main";

    public QuickPlayOptionsMenu(String title, Callback callback, RoomSettings roomSettings)
    {
        super(roomSettings);

        this.extended = false;
        this.callback = callback;
        this.title = title;
    }

    public QuickPlayOptionsMenu(Callback callback, RoomSettings roomSettings)
    {
        this(L.get("MENU_QUICK_PLAY"), callback, roomSettings);
    }

    protected String headerTitleStyle()
    {
        return "title-yellow";
    }

    protected String headerBorderStyle()
    {
        return "form-gray";
    }

    public void setName(String name)
    {
        Name = name;
    }

    private String getName()
    {
        return Name;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        Table header = new Table();
        Table content = super.createUI();

        Label skinTitle = new Label(title, BrainOutClient.Skin, headerTitleStyle());

        skinTitle.setAlignment(Align.center);
        header.add(new BorderActor(skinTitle, headerBorderStyle())).minWidth(540).expandX().fillX().row();

        renderContent(content);

        if (!extended)
        {
            Table row = new Table();

            Label description = new Label(L.get("MENU_SELECT_FIND_OPTIONS"),
                    BrainOutClient.Skin, "title-small");
            description.setAlignment(Align.center);
            row.add(description).pad(8, 0, 8, 0).expandX().fillX();

            TextButton additional = new TextButton("...", BrainOutClient.Skin, "button-default");

            additional.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    extended = true;
                    reset();
                }
            });

            row.add(additional).size(32).pad(8, 0, 8, 0).row();

            content.add(row).width(440).expandX().fillX().row();
        }

        Table buttons = new Table();
        renderButtons(buttons);

        content.add(buttons).pad(8).expandX().fillX().row();

        data.add(header).expandX().fillX().row();
        data.add(content).expandX().fillX().row();

        return data;
    }

    private void renderButtons(Table buttons)
    {
        TextButton cancel = new TextButton(
            L.get("MENU_CANCEL"),
            BrainOutClient.Skin, "button-default");

        cancel.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.back);
                pop();
                callback.cancelled();
            }
        });

        buttons.add(cancel).size(192, 32).pad(8);

        TextButton start = new TextButton(
            L.get("MENU_START"),
            BrainOutClient.Skin, "button-yellow");

        start.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);
                pop();
                callback.selected(getName(), getSettings(), QuickPlayOptionsMenu.this);
            }
        });

        buttons.add(start).size(192, 32).pad(8);
    }

    protected boolean enableRegionOption()
    {
        return true;
    }

    protected boolean enablePresets()
    {
        return true;
    }

    protected boolean enableMyLevelOnlyOption()
    {
        return true;
    }

    protected boolean enableKeepSameModeOption()
    {
        return true;
    }

    @Override
    protected void renderOptions(Array<PlayGameOptionsMenu.SettingsOption> options)
    {
        options.add(new MapOption(this::setName));
        options.add(new ModeOption());

        if (extended)
        {
            if (enablePresets())
            {
                options.add(new PresetOption());
            }

            if (enableRegionOption())
            {
                options.add(new RegionOption());
            }

            if (enableMyLevelOnlyOption())
            {
                options.add(new CheckboxOption("MENU_MY_LEVEL_ONLY")
                {
                    @Override
                    protected boolean isChecked()
                    {
                        return getSettings().isMyLevelOnly();
                    }

                    @Override
                    protected void setChecked(boolean checked)
                    {
                        getSettings().setMyLevelOnly(checked);
                    }
                });
            }

            if (enableKeepSameModeOption())
            {
                options.add(new YesNoOption("MENU_KEEP_SAME_MODE", getSettings().getKeepMode()));
            }

            options.add(new YesNoOption("MENU_DISABLE_BUILDING", getSettings().getDisableBuilding()));
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
}
