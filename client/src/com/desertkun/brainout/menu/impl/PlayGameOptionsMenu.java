package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.RoomSettings;
import org.anthillplatform.runtime.services.GameService;

import java.util.Objects;

public abstract class PlayGameOptionsMenu extends FormMenu
{
    private final RoomSettings settings;

    public PlayGameOptionsMenu(RoomSettings roomSettings)
    {
        this.settings = roomSettings;
    }

    protected abstract static class SettingsOption
    {
        private final String title;

        public SettingsOption(String title)
        {
            this.title = title;
        }

        protected abstract void render(Table row);

        public void renderRow(Table to)
        {
            Label title = new Label(L.get(this.title),
                    BrainOutClient.Skin, "title-yellow");

            title.setAlignment(Align.right);

            Table row = new Table();

            row.add(title).width(240).pad(8).uniformX().fillX();

            Table value = new Table();
            render(value);

            row.add(value).width(240).pad(8).uniformX().left().fillX().row();

            to.add(row).expandX().fillX().row();
        }
    }

    protected abstract static class CheckboxOption extends SettingsOption
    {
        public CheckboxOption(String title)
        {
            super(title);
        }

        protected abstract boolean isChecked();
        protected abstract void setChecked(boolean checked);

        private static String getTitle(boolean checked)
        {
            return checked ? L.get("MENU_YES") : L.get("MENU_NO");
        }

        @Override
        protected void render(Table row)
        {
            CheckBox checkBox = new CheckBox(getTitle(isChecked()),
                BrainOutClient.Skin, "checkbox-default");

            checkBox.setChecked(isChecked());
            checkBox.addListener(new ChangeListener()
            {
                @Override
                public void changed(ChangeEvent event, Actor actor)
                {
                    setChecked(checkBox.isChecked());
                    checkBox.setText(getTitle(checkBox.isChecked()));
                }
            });

            row.add(checkBox).height(32).padLeft(-4).expandX().left().row();
        }
    }

    protected abstract static class DropdownOption <T> extends SettingsOption
    {
        public DropdownOption(String title)
        {
            super(title);
        }

        protected abstract void getItems(Array<T> items);
        protected abstract T getCurrentItem();
        protected abstract void setItem(T item);

        @Override
        protected void render(Table row)
        {
            SelectBox<T> selectBox = new SelectBox<>(BrainOutClient.Skin, "select-badged");

            Array<T> items = new Array<>();
            getItems(items);

            selectBox.setItems(items);
            selectBox.setSelected(getCurrentItem());
            selectBox.setAlignment(Align.center);
            selectBox.getList().setAlignment(Align.center);
            selectBox.addListener(new ChangeListener()
            {
                @Override
                public void changed(ChangeEvent event, Actor actor)
                {
                    setItem(selectBox.getSelected());
                }
            });

            row.add(selectBox).height(32).expandX().fillX().row();
        }
    }

    public interface MapOptionNameChangedCallback
    {
        void changed(String name);
    }

    private static OrderedMap<String, MapOptionMap> MapOptionItems = new OrderedMap<>();
    private static MapOptionMap Any = new MapOptionMap(null);

    static
    {
        MapOptionItems.put("", Any);
    }

    protected class MapOption extends SettingsOption
    {

        private final MapOptionNameChangedCallback callback;

        public MapOption(MapOptionNameChangedCallback callback)
        {
            super("MENU_MAP");

            this.callback = callback;

            for (String map: getMaps())
            {
                MapOptionItems.put(map, new MapOptionMap(map));
            }
        }

        protected void getItems(Array<MapOptionMap> items)
        {
            items.addAll(MapOptionItems.values().toArray());
        }

        protected MapOptionMap getCurrentItem()
        {
            if (settings.getMap().isDefined())
                return MapOptionItems.get(settings.getMap().getValue());

            return Any;
        }

        protected void setItem(MapOptionMap item)
        {
            if (item == Any)
            {
                settings.getMap().undefine();
                return;
            }

            settings.getMap().define(item.map);
        }

        @Override
        protected void render(Table row)
        {
            TextButton button = new TextButton(getCurrentItem().toString(),
                    BrainOutClient.Skin, "button-badge");

            button.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    pushMenu(new MapSelectionMenu(new MapSelectionMenu.SelectionCallback()
                    {
                        @Override
                        public void selectedAny()
                        {
                            settings.getMap().undefine();
                            callback.changed("main");
                            button.setText(L.get("MENU_ANY_MAP"));
                        }

                        @Override
                        public void selectedStandard(String name)
                        {
                            settings.getMap().define(name);
                            callback.changed("main");
                            button.setText(L.get("MAP_" + name.toUpperCase()));
                        }

                        @Override
                        public void selectedWorkshop(String id, String title)
                        {
                            settings.getMap().define(id);
                            callback.changed("custom");
                            button.setText(title);

                            // cache
                            MapOptionItems.put(id, new MapOptionWorkshopMap(id, title));
                        }
                    }, getMaps()));
                }
            });

            row.add(button).height(32).expandX().fillX().row();
        }
    }

    public static class MapOptionMap
    {
        public String map;

        public MapOptionMap(String map)
        {
            this.map = map;
        }

        @Override
        public String toString()
        {
            if (map == null)
                return L.get("MENU_ANY_MAP");

            return map;
        }
    }

    public static class MapOptionWorkshopMap extends MapOptionMap
    {
        private final String name;

        public MapOptionWorkshopMap(String map, String name)
        {
            super(map);

            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    protected class ModeOption extends DropdownOption<ModeOption.Mode>
    {
        private OrderedMap<String, Mode> items = new OrderedMap<>();
        private Mode any;

        public ModeOption()
        {
            super("MENU_MODE");

            any = new Mode(null);

            items.put("", any);

            for (GameMode.ID mode: getGameModes())
            {
                items.put(mode.toString(), new Mode(mode));
            }
        }

        @Override
        protected void getItems(Array<Mode> items)
        {
            items.addAll(this.items.values().toArray());
        }

        @Override
        protected Mode getCurrentItem()
        {
            if (settings.getMode().isDefined())
                return items.get(settings.getMode().getValue());

            return any;
        }

        @Override
        protected void setItem(Mode item)
        {
            if (item == any)
            {
                settings.getMode().undefine();
                return;
            }

            settings.getMode().define(item.mode.toString());
        }

        public class Mode
        {
            public GameMode.ID mode;

            public Mode(GameMode.ID mode)
            {
                this.mode = mode;
            }

            @Override
            public String toString()
            {
                if (mode == null)
                    return L.get("MENU_ANY_MODE");

                return L.get("MODE_" + mode.toString().toUpperCase());
            }
        }
    }

    protected class PresetOption extends DropdownOption<PresetOption.WrapperPreset>
    {
        private Array<WrapperPreset> presets;
        private WrapperPreset unset;

        public class WrapperPreset
        {
            public String id;
            public String title;

            @Override
            public String toString()
            {
                return L.get(title);
            }
        }

        public PresetOption()
        {
            super(L.get("MENU_PRESET"));

            presets = new Array<>();

            unset = new WrapperPreset();
            unset.id = null;
            unset.title = "MENU_UNSET";

            for (ObjectMap.Entry<String, String> entry : ClientConstants.Presets.PRESETS)
            {
                WrapperPreset preset = new WrapperPreset();

                preset.id = entry.key;
                preset.title = entry.value;

                presets.add(preset);
            }

            presets.add(unset);
        }

        @Override
        protected void getItems(Array<PresetOption.WrapperPreset> items)
        {
            items.addAll(presets);
        }


        @Override
        protected PresetOption.WrapperPreset getCurrentItem()
        {
            for (WrapperPreset preset : presets)
            {
                if (Objects.equals(preset.id, settings.getPreset()))
                {
                    return preset;
                }
            }

            return unset;
        }

        @Override
        protected void setItem(PresetOption.WrapperPreset item)
        {
            settings.setPreset(item.id);
        }
    }

    protected class RegionOption extends DropdownOption<ClientController.RegionWrapper>
    {
        private ClientController.RegionWrapper any;

        public RegionOption()
        {
            super("MENU_REGION");

            any = new ClientController.RegionWrapper(new GameService.Region(L.get("MENU_ANY_REGION"), null));
        }

        @Override
        protected void getItems(Array<ClientController.RegionWrapper> items)
        {
            items.add(any);

            BrainOutClient.ClientController.getRegions().forEach(items::add);
        }

        @Override
        protected ClientController.RegionWrapper getCurrentItem()
        {
            if (settings.getRegion() != null)
            {
                for (ClientController.RegionWrapper region : BrainOutClient.ClientController.getRegions())
                {
                    if (region.region.name.equals(settings.getRegion()))
                    {
                        return region;
                    }
                }
            }

            return any;
        }

        @Override
        protected void setItem(ClientController.RegionWrapper item)
        {
            if (item == any)
            {
                settings.setRegion(null);
                return;
            }

            settings.setRegion(item.region.name);
        }
    }

    public static class YesNoOption extends DropdownOption<RoomSettings.BooleanOption>
    {
        private final RoomSettings.BooleanOption option;

        public YesNoOption(String title, RoomSettings.BooleanOption option)
        {
            super(title);

            this.option = option;
        }

        @Override
        protected void getItems(Array<RoomSettings.BooleanOption> items)
        {
            items.add(RoomSettings.BooleanOption.UNSET);
            items.add(RoomSettings.BooleanOption.YES);
            items.add(RoomSettings.BooleanOption.NO);
        }

        @Override
        protected RoomSettings.BooleanOption getCurrentItem()
        {
            if (!option.isDefined())
                return RoomSettings.BooleanOption.UNSET;

            return option.getValue() ? RoomSettings.BooleanOption.YES : RoomSettings.BooleanOption.NO;
        }

        @Override
        protected void setItem(RoomSettings.BooleanOption item)
        {
            if (item.isDefined())
            {
                option.define(item.getValue());
            }
            else
            {
                option.undefine();
            }
        }

    }

    protected GameMode.ID[] getGameModes()
    {
        return Constants.Matchmaking.APPROVED_MODES;
    }

    protected String[] getMaps()
    {
        return Constants.Matchmaking.APPROVED_MAPS;
    }

    protected void renderContent(Table content)
    {
        Array<SettingsOption> options = new Array<>();
        renderOptions(options);

        for (SettingsOption option: options)
        {
            option.renderRow(content);
        }
    }

    protected abstract void renderOptions(Array<SettingsOption> options);

    public RoomSettings getSettings()
    {
        return settings;
    }
}
