package com.desertkun.brainout;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.data.interfaces.ActiveLayer;
import com.desertkun.brainout.mode.GameMode;
import org.anthillplatform.runtime.services.*;

import java.util.regex.Pattern;

public class Constants
{
    public static class Version
    {
        public static final String API = "0.2";
        public static final int BUILD = 17;
    }

    public static class Files
    {
        public static final String USER_PROFILE_NAME = "user-profile";
    }

    public static class Matchmaking
    {
        public static final String[] APPROVED_MAPS = {
            "factory",
            "station",
            "mall",
            "hospital",
            "hospital_new",
            "siber",
            "forest",
            "warehouse",
            "bridge",
            "tower",
            "bunker",
            "construction",
            "hangars",
            "deposite",
            "cargo",
            "canyon"
        };

        public static final String[] APPROVED_COMPETITIVE_MAPS = {
            "factory",
            "station",
            "mall",
            "hospital",
            "hospital_new",
            "siber",
            "forest",
            "bridge",
            "tower",
            "bunker",
            "canyon"
        };

        public static final GameMode.ID[] APPROVED_MODES = {
            GameMode.ID.normal,
            GameMode.ID.deathmatch,
            GameMode.ID.domination,
            GameMode.ID.assault,
            GameMode.ID.foxhunt,
            GameMode.ID.gungame
        };

        public static final GameMode.ID[] APPROVED_COMPETITIVE_MODES = {
            GameMode.ID.normal,
            GameMode.ID.domination,
            GameMode.ID.assault
        };
    }

    public static class Menu
    {
        public static final float LOADINGBAR_WIDTH = 256;
        public static final float LOADINGBAR_HEIGHT = 8;

        public static final float MENU_BACKGROUND_FADE = 0.25f;
        public static final float MENU_BACKGROUND_FADE_DOUBLE = 0.75f;
        public static final float MENU_BACKGROUND_FADE_TRIPLE = 0.95f;
    }

    public static class Voice
    {
        public static final int DISTANCE = 32;
        public static final int DISTANCE_SQR = DISTANCE * DISTANCE;
    }

    public static class Connection
    {
        public static final int DEFAULT_TCP_PORT = 36555;
        public static final int DEFAULT_UDP_PORT = 36556;
        public static final int DEFAULT_HTTP_PORT = 36557;
        public static final int TIME_OUT = 2500;

        public static final int RECONNECT_TIME_OUT = 15;

        public static final String[] DISCOVER = new String[]
        {
            LoginService.ID, ProfileService.ID, PromoService.ID, EventService.ID,
            GameService.ID, LeaderboardService.ID, MessageService.ID, StoreService.ID,
            SocialService.ID, StaticService.ID, ReportService.ID, BlogService.ID,
            MarketService.ID
        };
    }

    public static class Maps
    {
        public static final int MAP_SIGNATURE_SIZE = 32;
        public static final String MAGIC = "/BRAIN/OUT/L9a1E5mLKjh93Z40T7lk/";
        public static final String V = "1";
    }

    public static class Graphics
    {
        public static final int BLOCK_SIZE = 16;
        public static final float BLOCK_SCALE = 1;
        public static final float RES_SIZE = BLOCK_SIZE * BLOCK_SCALE;

        public static final int DISPLAY_ADDITINAL_BLOCKS = 16;
    }

    public static class Physics
    {
        public static final float CORRECTION_DISTANCE = 8.0f;
        public static final float REPOSITION_DISTANCE = 8.0f;

        public static final float FIXED_TIME_STEP = 1.0f / 60.0f;
        public static final int MAX_STEPS = 5;

        public static final int RAY_CAST_STEPS = 4;

        public static final int VELOCITY_ITERATIONS = 16;
        public static final int POSITION_ITERATIONS = 16;

        public static final float SCALE = 1.0f;
        public static final float SCALE_OF = 1.0f / SCALE;
        public static final float MASS_COEF = 1.0f;

        public static final int PHYSIC_BLOCK_SIZE = 32;

        public static final int PHYSIC_BLOCKS_PER_CHUNK = Core.CHUNK_SIZE / PHYSIC_BLOCK_SIZE;
        public static final int PHYSIC_BLOCKS_PER_CHUNK_SQR = PHYSIC_BLOCKS_PER_CHUNK * PHYSIC_BLOCKS_PER_CHUNK;

        public static final int CATEGORY_BLOCKS = 1;
        public static final int CATEGORY_RAGDOLL = 2;
        public static final int CATEGORY_BELT = 3;
        public static final int CATEGORY_SHIELDS = 4;
        public static final int CATEGORY_OBJECT = 5;
        public static final int CATEGORY_LIGHT = 6;

    }

    public static class Moves
    {
        public static final float CHANGE_MOVE_DIST = 0.5f;
        public static final float CHANGE_SPEED_DIST = 3.0f;
        public static final float CHANGE_ANGLE_DIST = 10f;

        public static final float CHANGE_TIME_DEFAULT = 0.1f;
        public static final float CHANGE_TIME_MOUSE = 0.25f;
        public static final float CHANGE_TIME_ANGLE = 0.2f;
        public static final float CHANGE_TIME_ANYWAY = 0.5f;
        public static final float CHANGE_TIME_ANYWAY_SLOW = 5f;
    }

    public static class Inventory
    {
        public static final Array<String> SLOTS = new Array<String>(new String[]
        {
            "slot-primary",
            "slot-secondary",
            "slot-special",
            "slot-melee",
            "slot-binoculars",
            "slot-flashlight"
        });
    }

    public static class Core
    {
        public static final boolean MULTIPLE_WINDOWS_DISABLED = false;

        public static final String SHOP_ITEM = "shop";
        public static final String UNLOCK_TREE = "unlock-tree";

        public static final float EDIT_MOVE_SPEED = 20f;

        public static final float BULLET_TIME_TO_LIVE = 2f;
        public static final int BULLET_UPDATE_STEPS = 16;
        public static final int CHUNK_SIZE = 64;

        public static final int CHUNK_SIZE_SQR = CHUNK_SIZE * CHUNK_SIZE;
        public static final int CHUNK_SIZE_PIX = (int)(CHUNK_SIZE * Graphics.RES_SIZE);

        public static final float GRAVITY = 50f;

        public static final int SERVER_ACTIVE_START = 0x00FFFFFF;
        public static final int SERVER_ACTIVE_AMOUNT = 0x0F000000;
        public static final float PHY_COLLISION_REDUCING = 0.2f;

        public static final int PING_GOOD = 100;
        public static final int PING_NORMAL = 200;
        public static final float MAX_SPEED = 60f;

        public static final float GAME_END_THRESHOLD = 0.75f;

        public static final int SECONDS_IN_A_DAY = 1800;

    }

    public static class Weapons
    {
        public static final float SHOOT_SPEED_COEF = 1.25f;
        public static final float SHOOT_IN_AIR_OFFSET_COEF = 2.0f;
        public static final float ACCURACY_STAY_MIN_VALUE = 0.2f;

        public static class StuckCoefficients
        {
            public static final float BAD_MIN = 0.05f;
            public static final float BAD_MAX = 0.1f;
            public static final float GOOD_MIN = 0.3f;
            public static final float GOOD_MAX = 0.4f;
        }
    }

    public static class Clans
    {
        public static final String CURRENCY_CREATE_CLAN = User.SKILLPOINTS;
        public static final String CURRENCY_UPDATE_CLAN = User.SKILLPOINTS;
        public static final String CURRENCY_JOIN_CLAN = User.NUCLEAR_MATERIAL;
        public static final String CURRENCY_CLAN_PARTICIPATE = User.NUCLEAR_MATERIAL;

        public static final int ROLE_LIEUTENANT = 500;
    }

    public static class ActiveTags
    {
        public static final int COLLIDER = 0;
        public static final int SPAWNABLE = 1;
        public static final int PLAYERS = 2;
        public static final int WITH_HEALTH = 3;
        public static final int INSTANCE_LIMIT = 4;
        public static final int RESOURCE_RECEIVER = 5;
        public static final int DETECTORS = 6;
        public static final int CHIP = 7;
        public static final int CHIP_SPAWNER = 8;
        public static final int CHIP_RECEIVER = 9;
        public static final int DETECTABLE = 10;
        public static final int FLAG = 11;
        public static final int THROWABLE = 12;
        public static final int EXIT_DOOR = 13;
        public static final int ITEM = 14;
        public static final int PORTAL = 15;
        public static final int TARGET_SPAWNER = 16;
        public static final int SHOOTING_RANGE = 17;
        public static final int RADIOACTIVE = 18;
        public static final int USER_IMAGE = 19;
        public static final int POINT_OF_INTEREST = 20;
        public static final int MARKER = 21;
        public static final int CAMP_FIRE = 22;
        public static final int WIND = 23;
        public static final int BACKGROUND_MUSIC = 24;
        public static final int ENTER_DOOR = 25;
        public static final int MARKET_CONTAINER = 26;
    }

    public static class Layers
    {
        public static final int ACTIVE_LAYERS_COUNT = ActiveLayer.values().length;
        public static final int EFFECT_LAYERS_COUNT = 3;
        public static final int BLOCK_LAYERS_COUNT = 3;

        public static final int BLOCK_LAYER_BACKGROUND = 0;
        public static final int BLOCK_LAYER_FOREGROUND = 1;
        public static final int BLOCK_LAYER_UPPER = 2;

        public static final int ACTIVE_LAYER_1      = 0;
        public static final int ACTIVE_LAYER_1TOP   = 3;
        public static final int ACTIVE_LAYER_2      = 1;
        public static final int ACTIVE_LAYER_3      = 2;

        public static final int EFFECT_LAYER_1 = 0;
        public static final int EFFECT_LAYER_2 = 1;
        public static final int EFFECT_LAYER_3 = 2;
    }

    public static class Analytics
    {
        public static final String GAME = "98cfdc39bb73ea7ee941b788c553854a";
        public static final String SECRET = "1c9f643192c6bbae72bc1d868f022c05e2a653f5";
    }

    public static class Drop
    {
        public static final String DEFAULT_DROP_ITEM = "def-drop-item";
    }

    public static class User
    {
        public static final String SCORE = "score";
        public static final String TECH_SCORE = "tech-score";

        public static final String SKILLPOINTS = "skillpts";
        public static final String GEARS = "gears";
        public static final String NUCLEAR_MATERIAL = "nuclear-material";
        public static final String LEVEL = "level";
        public static final String TECH_LEVEL = "tech-level";

        public static final String STORE_SLOT = "slot-store";
        public static final String PLAYER_SLOT = "slot-player";
        public static final String DAILY_CONTAINER = "case-daily";

        public static final String PROFILE_BADGE = "profile-badge";
        public static final String PROFILE_BADGE_DEFAULT = "profile-badge-none";
    }

    public static class Collecting
    {
        public static final float TIMEOUT = 30;
    }

    public static class Sound
    {
        public static final float SOUND_HEAR_DIST = 192;
        public static final float SOUND_DELAY_DIST = 0.75f;
    }

    public static class Damage
    {
        public static final String DAMAGE_HIT = "hit";
        public static final String DAMAGE_PROTECT = "protect";
        public static final String DAMAGE_FRACTURE = "fracture";
    }

    public static class UdpMessages
    {
        public static final int TIMEOUT = 500;
        public static final int RETRIES = 3;
    }

    public class Stats
    {
        public static final String KILLS = "kills";
        public static final String DONATED = "donated";
        public static final String RESOURCES_RECEIVED = "resources-received";
        public static final String SILENT_KILLS = "silent-kills";
        public static final String DEATHS = "deaths";
        public static final String EFFICIENCY = "kpd";
        public static final String GAMES_WON = "games-won";
        public static final String TOURNAMENTS_WON = "tournaments-won";
        public static final String GAMES_LOST = "games-lost";
        public static final String TIME_SPENT = "time-spent";
        public static final String RATING = "rating";
    }

    public static class Properties
    {
        public static final String SLOT_PRIMARY = "primary";
        public static final String SLOT_SECONDARY = "secondary";

        public static final String FIRE_BONE_OFFSET = "fire-bone-offset";
        public static final String DAMAGE = "damage";
        public static final String BULLET = "bullet";
        public static final String ACCURACY = "accuracy";
        public static final String RECOIL = "recoil";
        public static final String RELOAD_TIME = "reload-time";
        public static final String FETCH_TIME = "fetch-time";
        public static final String RELOAD_TIME_BOTH = "reload-time-both";
        public static final String COCK_TIME = "cock-time";
        public static final String CLIP_SIZE = "clip-size";
        public static final String AIM_MARKER = "aim-marker";
        public static final String AIM_DISTANCE = "aim-distance";
        public static final String SILENT = "silent";
        public static final String FIRE_RATE = "fire-rate";
        public static final String FIRE_RATE_B2 = "fire-rate-b2";
        public static final String WEAR_RESISTANCE = "wear-resistance";
        public static final String SPEED_COEF = "speed-coef";
        public static final String SHOOT_MODES = "shoot-modes";
        public static final String NUMBER_OF_MAGAZINES = "number-of-magazines";
        public static final String MAG_ADD_ROUND_TIME = "mag-add-round-time";
    }

    public static class Weapon
    {
        public static final float ACCURACY_MIN_MIN = 0.5f;
        public static final float ACCURACY_MIN_MAX = 5.0f;

        public static final float ACCURACY_MAX_MIN = 2.0f;
        public static final float ACCURACY_MAX_MAX = 15.0f;

        public static final float RECOIL_LAUNCH_ADD_MIN = 0.05f;
        public static final float RECOIL_LAUNCH_ADD_MAX = 1.0f;

        public static final float RECOIL_LAUNCH_ANGLE_ADD_MIN = 0.0f;
        public static final float RECOIL_LAUNCH_ANGLE_ADD_MAX = 20.0f;
        public static final float RECOIL_LAUNCH_ANGLE_LIMIT = 45f;

        public static final float RECOIL_BREAKDOWN_MIN = 2.0f;
        public static final float RECOIL_BREAKDOWN_MAX = 0.025f;
    }

    public class Effects
    {
        public static final String LAST_ROUND_EFFECT = "last-round";
        public static final String EMPTY_EFFECT = "empty";
        public static final String STUCK_EFFECT = "stuck";
    }

    public class TimeSync
    {
        public static final float PERIOD = 10.0f;
    }

    public class DailyReward
    {
        public static final int MAX_DAILY_CONTAINERS = 20;
    }

    public static class Other
    {
        public static final String MAP_EDITOR_PASS = "map-editor-pass";
        public static final String CLAN_PASS = "clan-pass";
        public static final String FAVORITES_TAG = "favorites";
        public static final String SHOOTING_RANGE_ACTION = "shooting-range";
        public static final String VALUABLES_ACTION = "fp-valuables";
        public static final int LEVEL_PROTECT = 15;
        public static final float TEAM_LANDING_TIMER = 10.0f;
        public static final String TEAM_LANDING_ACTION_NAME = "team-landing-message";
    }

    public static class Editor
    {
        public static Array<MapSize> SIZES;

        public static Pattern NAME_PATTERN = Pattern.compile("^([a-z0-9_]{3,20})$");

        public static class MapSize
        {
            private int w;
            private int h;

            public MapSize(int w, int h)
            {
                this.w = w;
                this.h = h;
            }

            @Override
            public String toString()
            {
                return String.valueOf(w) + "x" + h;
            }

            public String getID()
            {
                return toString();
            }

            public int getW()
            {
                return w;
            }

            public int getH()
            {
                return h;
            }
        }

        static
        {
            SIZES = new Array<>(new MapSize[]{
                new MapSize(5, 2),
                new MapSize(3, 2),
                new MapSize(2, 2),
                new MapSize(1, 2)
            });
        }
    }
}
