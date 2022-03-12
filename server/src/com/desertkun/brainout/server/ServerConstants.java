package com.desertkun.brainout.server;

import com.badlogic.gdx.graphics.Color;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;

public class ServerConstants extends Constants
{
    public static final int CLIENTS_MAX = 64;

    public static class Chat
    {
        public static final Color COLOR_INFO = Color.WHITE;
        public static final Color COLOR_CONSOLE = Color.GREEN;
        public static final Color COLOR_IMPORTANT = Color.YELLOW;
    }

    public static class Blocks
    {
        public static final int PLACE_MIN_DISTANCE = 3;
    }

    public static class Drop
    {
        public static final float DROP_SPEED_THROW = 20f;
        public static final float DROP_SPEED_DEATH = 5f;
    }

    public static class Clients
    {
        public static final float PING_TIME = 8f;
        public static final float UPDATE_INFO_TIME = 16f;
        public static final float MAX_UPDATE_MOVEMENT_DISTANCE = 64;
        public static final float UPDATE_TIME_ANYWAY = 1.0f;

        public static final float MAX_UPDATE_MOVEMENT_DISTANCE_SQR =
                MAX_UPDATE_MOVEMENT_DISTANCE * MAX_UPDATE_MOVEMENT_DISTANCE;
    }

    public static class GameMode
    {
        public static final float CHECK_TIME = 2f;
    }

    public static class Maps
    {
        public static final String MAP_KEY = "9zS9kBxM6QoVMgrabMsE6XwUUQgLLwBDO0RqN8QK";
    }

    public static class Spawn
    {
        public static final float DELAY = 1f;
        public static final float AUTO_KICK = BrainOut.OnlineEnabled() ? 120f : 120000f;
    }

    public static class Name
    {
        public static final String APP_NAME = "brainout";
        public static final String GAMESPACE = "brainout:desktop";
    }

    public static class Controller
    {
        public static final float DEATHS_PERIOD = 30;
    }

    public static class Online
    {
        public static final String EXTEND_SCOPES = "profile_private,promo,event_profile_write," +
            "message_listen,store_order,group_create,group_write,lb_arbitrary_account,group," +
            "message_authoritative,event_write,event_join,party_create,profile,profile_multi," +
            "market,market_post_order,market_update_item,market_delete_order";

        public static class ProfileFields
        {
            public static final String SCORE = "score";
            public static final String TECH_SCORE = "tech-score";
        }

        public static final float PROFILE_UPLOAD_PERIOD = BrainOut.OnlineEnabled() ? 30.0f : 1.0f;

        public static final int GROUP_MAX_MEMBERS = 20;
    }

    public static class Rating
    {
        public static final int WON_ADD_RATING = 10;
        public static final int LOST_REMOVE_RATING = 3;
        public static final int PUNISHMENT = LOST_REMOVE_RATING;
    }

    public static class Farming
    {
        public static final int MAX_KILLS_IN_A_ROW = 25;
        public static final int PLAYERS_TO_STORE = 2;
    }
}
