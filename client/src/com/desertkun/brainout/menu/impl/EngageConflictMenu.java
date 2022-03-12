package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.RoomSettings;

public class EngageConflictMenu extends QuickPlayOptionsMenu
{
    private int conflictSize;

    public EngageConflictMenu(Callback callback,
                              RoomSettings roomSettings)
    {
        super(L.get("MENU_CLAN_CHALLENGE"), callback, roomSettings);

        this.conflictSize = 8;
    }

    @Override
    protected String headerBorderStyle()
    {
        return "form-red";
    }

    @Override
    protected String formBorderStyle()
    {
        return "form-border-red";
    }

    @Override
    protected boolean enableRegionOption()
    {
        return false;
    }

    @Override
    protected boolean enableMyLevelOnlyOption()
    {
        return false;
    }

    @Override
    protected boolean enableKeepSameModeOption()
    {
        return false;
    }

    @Override
    protected GameMode.ID[] getGameModes()
    {
        return Constants.Matchmaking.APPROVED_COMPETITIVE_MODES;
    }

    @Override
    protected String[] getMaps()
    {
        return Constants.Matchmaking.APPROVED_COMPETITIVE_MAPS;
    }

    protected class ConflictSizeOption extends DropdownOption<ConflictSizeOption.ConflictSize>
    {
        private ConflictSize[] conflictSizes;

        public ConflictSizeOption()
        {
            super("MENU_CLAN_CHALLENGE_SIZE");

            conflictSizes = new ConflictSize[]
            {
                new ConflictSize(8),
                new ConflictSize(6),
                new ConflictSize(4)
            };
        }

        @Override
        protected void getItems(Array<ConflictSize> items)
        {
            items.addAll(conflictSizes);
        }

        @Override
        protected ConflictSize getCurrentItem()
        {
            for (ConflictSize size : conflictSizes)
            {
                if (size.size == conflictSize)
                    return size;
            }

            return null;
        }

        @Override
        protected void setItem(ConflictSize item)
        {
            conflictSize = item.getSize();
        }

        public class ConflictSize
        {
            private int size;

            public ConflictSize(int size)
            {
                this.size = size;
            }

            @Override
            public String toString()
            {
                int half = size / 2;
                return String.valueOf(half) + "x" + String.valueOf(half);
            }

            public int getSize()
            {
                return size;
            }
        }
    }

    @Override
    protected void renderOptions(Array<SettingsOption> options)
    {
        super.renderOptions(options);

        options.add(new ConflictSizeOption());
    }

    public int getConflictSize()
    {
        return conflictSize;
    }
}
