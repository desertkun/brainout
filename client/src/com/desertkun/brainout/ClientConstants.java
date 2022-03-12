package com.desertkun.brainout;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

public class ClientConstants extends Constants
{
    public static class Client
    {
        public final static String MAINMENU_PACKAGE = "mainmenu";

        public final static boolean DEBUG = false;
        public final static boolean MOUSE_LOCK = !DEBUG;

        public final static float MOUSE_MIN_DIST = 5f;

        public final static float HIT_TIMER = 0.5f;
        public final static float DAMAGE_TIMER = 0.5f;
    }

    public static class Presets
    {
        public final static OrderedMap<String, String> PRESETS = new OrderedMap<>();

        static
        {
            PRESETS.put("no-primary", "MENU_PRESET_NO_PRIMARY");
            PRESETS.put("knife-only", "MENU_PRESET_KNIFES_ONLY");
            PRESETS.put("classic-only", "MENU_PRESET_CLASSIC_ONLY");
            PRESETS.put("toz34-only", "MENU_PRESET_TOZ34");
            PRESETS.put("hard", "MENU_PRESET_HARD");
            PRESETS.put("super-hard", "MENU_PRESET_SUPER_HARD");
        }
    }

    public static class Security
    {
        public final static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxU5ijLJZY/EPnE54zm7uQaRtj" +
                "D3J7FQsVjUBjI9B5/Q5EHGFOkXmpbscEYflO3CtrXKanadzlyoMXUTxEPYXbpE6e" +
                "2z/LZRdC0cZuScKJ6v0Qryllkboo4LoK0vZ4Aw5MoycOorwUVYu4P+CUCItyTeW9" +
                "6M39y5szQ0lrb378FQIDAQAB";
    }

    public static class ScreenResolution
    {
        public final static int MIN_X = 1024;
        public final static int MIN_Y = 768;
    }

    public static class Name
    {
        public final static String APP_NAME = "brainout";
        public final static String GAMESPACE = "brainout:desktop";
    }

    public static class Scopes
    {
        public final static String SCOPES = "profile,profile_write,game_root,game_mod,game_editor," +
            "message_listen,game,store,group,static_upload,party,party_create,report_upload,blog,game_ban,market";
        public final static String SHOULD_HAVE = "profile,profile_write,game,store,party,party_create," +
            "report_upload,blog,market";
    }

    public static class Items
    {
        public final static float WAVING = 5f;
        public final static float PICK_DISTANCE = 3f;
        public final static float FOUND_DISTANCE = 16f;
        public final static float PICK_TIMEOUT = 0.5f;
        public final static float START_PICK = 0.5f;
        public final static float MAX_SPEED = 10f;
    }

    public static class Sync
    {
        public static final int TIME_SYNC_COUNT = 10;
        public static final int TIMEOUT = 2;
        public static final int TIMEOUT_DISCONNECT = 10;
    }

    public static class Components
    {
        public static class Laser
        {
            public static final int DISTANCE = 50;
            public static final float UPDATE_TIME = 0.0125f;
            public static final float ALPHA = 0.8f;
            public static final float SEE_ANGLE = 20f;
        }
    }

    public static class Aiming
    {
        public static final float AIM_FOLLOWING_SPEED = 8.0f;
        public static final float AIM_ZOOMING_IN_COEFFICIENT = 5f;
        public static final float AIM_ZOOMING_IN_MIN_DISTANCE = 20f;
        public static final float AIM_ZOOMING_BLOCKS_SMOOTH = 5f;
    }

    public static class Menu
    {
        public static class Tooltip
        {
            public static final float TOOLTIP_TIME = 0.25f;
            public static final float HIDE_TIME = 2f;
        }

        public static class Flash
        {
            public static final float FLASH = 0.5f;
        }

        public static class Layers
        {
            public static final int WIDTH = 300;
            public static final int HEIGHT = 200;
            public static final int OFFSET_X = 10;
            public static final int OFFSET_Y = 10;
        }

        public static class Chat
        {
            public static final int WIDTH = 400;
            public static final int HEIGHT = 300;
            public static final int OFFSET_X = 10;
            public static final int OFFSET_Y = 200;

            public static final int SEND_HEIGHT = 30;
            public static final int SEND_X = 10;
            public static final int SEND_Y = 10;
            public static final float SEND_APPEARING_TIME = 0.125f;

            public static final float APPEARING_TIME = 0.25f;
            public static final float SHOW_DELAY = 5f;
        }

        public static class FreePlay
        {
            public static final String[] MAGAZINE_IMAGES = {
                "magazine-empty",
                "magazine-1",
                "magazine-2",
                "magazine-3",
                "magazine-4",
                "magazine-5",
                "magazine-6",
                "magazine-7",
                "magazine-8",
                "magazine-9",
                "magazine-10",
                "magazine-full",
            };

            public static String GetMagazineImage(float percent)
            {
                return MAGAZINE_IMAGES[Math.min((int)(percent * (float)(MAGAZINE_IMAGES.length - 1)),
                        MAGAZINE_IMAGES.length - 1)];
            }
        }

        public static class PartyFriends
        {
            public static final int WIDTH = 68;
            public static final int HEIGHT = 210;
            public static final int OFFSET_X = 20;
            public static final int OFFSET_Y = 130;
        }

        public static class KillList
        {
            public static final int WIDTH = 400;
            public static final int HEIGHT = 200;
            public static final int X = 12;
            public static final int Y = 16;
            public static final float APPEARANCE = 4f;
            public static final float ALPHA_TIME = 0.5f;

            public static final Color MY_COLOR = Color.GREEN;
            public static final Color FRIEND_COLOR = new Color(0x0094FFFF);
            public static final Color ENEMY_COLOR = new Color(0xFF6A00FF);
            public static final Color ADMIN_COLOR = new Color(0x00F8FCFF);
            public static final Color SPECIAL_COLOR = new Color(0xCB43A7FF);
            public static final Color BRAIN_PASS_COLOR = new Color(0xf8b800FF);
            public static final Color CLAN_COLOR = Color.GREEN;

            public static final Color MINIMAP_MY_COLOR = new Color(0xFFFFFF7F);
            public static final Color MINIMAP_ENEMY_COLOR = new Color(0xF8B8007F);

            public static final Color KARMA_VERY_BAD = new Color(0xfc3800FF);
            public static final Color KARMA_BAD = new Color(0xfcb800FF);
            public static final Color KARMA_OK = new Color(0xFFFFFFFF);
            public static final Color KARMA_GOOD = new Color(0x00f8fcFF);
            public static final Color KARMA_VERY_GOOD = new Color(0x31a2f2FF);
        }

        public static class PlayerInfo
        {
            public static final int WIDTH = 352;
            public static final int HEIGHT = 400;
            public static final int X = 8;
            public static final int Y = 8;

            public static final int LABEL_WIDTH = 150;
            public static final int LABEL_HEIGHT = 20;

            public static final int LABEL_OFFSET_X = 0;
            public static final int LABEL_OFFSET_Y = 2;

            public static final int INSTRUMENT_ICON_WIDTH = 160;
            public static final int INSTRUMENT_ICON_HEIGHT = 52;
        }

        public static class SendChat
        {
            public static final float APPEARING_TIME = 0.125f;
        }

        public static class Console
        {
            public static final int SEND_HEIGHT = 30;
            public static final int TERMINAL_HEIGHT = 256;
        }

        public static class Notify
        {
            public static final float APPEARANCE = 2f;
        }

        public static class PlayerStats
        {
            public static final float WIDTH = 5f;
            public static final float HEIGHT = 2f;
        }

        public static class PlayerChat
        {
            public static final float WIDTH = 12f;
            public static final float HEIGHT = 8f;
        }
    }

    public static class Keys
    {
        public static final int KEY_MOVE_LEFT = Input.Keys.A;
        public static final int KEY_MOVE_RIGHT = Input.Keys.D;
        public static final int KEY_MOVE_UP = Input.Keys.W;
        public static final int KEY_MOVE_DOWN = Input.Keys.S;

        public static final int KEY_SIT = Input.Keys.CONTROL_LEFT;
        public static final int KEY_RUN = Input.Keys.SHIFT_LEFT;

        public static final int KEY_CHAT = Input.Keys.Y;
        public static final int KEY_TEAM_CHAT = Input.Keys.U;
        public static final int KEY_DEBUG = Input.Keys.GRAVE;

        public static final int KEY_EDIT_MOVE_LEFT = Input.Keys.LEFT;
        public static final int KEY_EDIT_MOVE_RIGHT = Input.Keys.RIGHT;
        public static final int KEY_EDIT_MOVE_UP = Input.Keys.UP;
        public static final int KEY_EDIT_MOVE_DOWN = Input.Keys.DOWN;
    }

    public static class Editor
    {
        public static final int MAX_MODE_SPEED = 8;
    }

    public static class WeaponState
    {
        public static final int NORMAL = 1;
        public static final int PULL = 2;
        public static final int PULLING = 3;
        public static final int RELOADING = 4;
        public static final int STUCK = 5;
        public static final int EMPTY = 6;
        public static final int COCKING = 7;
        public static final int COCKED = 8;
        public static final int DETACHED = 9;
        public static final int ADDING_ROUNDS = 10;
        public static final int MISFIRE = 11;
    }

    public static class Flag
    {
        public static final int OFFSET_Y = 4;
        public static final float WIDTH = 4;
        public static final float HEIGHT = 0.5f;
    }

    public static class Player
    {
        public static final float SENSITIVITY_MULTIPLIER = 0.5f;
    }

    public static class Workshop
    {
        public static final int WORKSHOP_MINIMUM_SUBSCRIPTIONS = 100;
    }
}
