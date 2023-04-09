package com.desertkun.brainout.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.online.Preset;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateGame;
import com.desertkun.brainout.plugins.Plugin;
import com.desertkun.brainout.utils.CoreSettings;

public class ServerSettings extends CoreSettings
{
    private BaseConditions baseConditions;
    private ObjectMap<String, Integer> prices;
    private String name;
    private Array<String> teamNames;

    private ObjectMap<String, String> levels;
    private ObjectMap<String, Preset> presets;
    private String dailyContainer;

    private String greetings;
    private String mapsFilter;
    private boolean developerMode;
    private boolean ratingEnabled;
    private int deathsRequired;
    private String fullDrop;
    private String zone;
    private long lastConflict;

    private boolean autoBalanceEnabled;
    private int autoBalanceValue;
    private float restartIn;
    private long roundTime;
    private float modeDelay;
    private boolean weaponAutoLoad;

    private ServerController.RespawnKind respawnKind;
    private float respawnWaveTime;
    private float respawnWaveMinTime;
    private float respawnWaveRate;

    public String getGreetings()
    {
        return greetings;
    }

    public static class BaseConditions implements Json.Serializable
    {
        public ObjectMap<String, String> defines;
        public Array<String> packages;
        public String name;

        public BaseConditions()
        {
            defines = new ObjectMap<>();
            packages = new Array<>();
        }

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            name = jsonData.name();

            if (jsonData.has("defines"))
            {
                JsonValue def = jsonData.get("defines");

                if (def.isObject())
                {
                    for (JsonValue v: def)
                    {
                        defines.put(v.name(), v.asString());
                    }
                }
            }

            if (jsonData.has("packages"))
            {
                JsonValue def = jsonData.get("packages");

                if (def.isArray())
                {
                    for (JsonValue v: def)
                    {
                        packages.add(v.asString());
                    }
                }
            }
        }

        public void getDefines(ObjectMap<String, String> defines)
        {
            for (ObjectMap.Entry<String, String> entry: this.defines)
            {
                defines.put(entry.key, entry.value);
            }
        }
    }

    public static class MapConditions extends BaseConditions
    {
        public String map;

        public MapConditions()
        {
        }

        public MapConditions(String map)
        {
            this.map = map;
        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            super.read(json, jsonData);

            this.map = jsonData.getString("map");
        }

        @Override
        public void getDefines(ObjectMap<String, String> defines)
        {
            super.getDefines(defines);

            defines.put("currentMap", map);
        }

        public String getMapName()
        {
            if (name != null)
                return name;

            return map;
        }
    }

    public static class GameModeConditions extends BaseConditions
    {
        public GameMode.ID mode;
        public Array<String> teams;
        public JsonValue settings;
        public long roundTime;

        public GameModeConditions()
        {
            this.teams = null;

            this.roundTime = -1;
        }

        public GameModeConditions(GameMode.ID mode)
        {
            this.mode = mode;

            this.roundTime = -1;
        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            super.read(json, jsonData);

            this.mode = GameMode.ID.valueOf(jsonData.getString("mode"));
            if (jsonData.has("teams"))
            {
                this.teams = new Array<>();

                for (JsonValue team : jsonData.get("teams"))
                {
                    this.teams.add(team.asString());
                }
            }

            this.settings = jsonData.get("settings");

            this.roundTime = jsonData.getLong("roundTime", -1);
        }

        public long getRoundTime()
        {
            return roundTime;
        }

        @Override
        public void getDefines(ObjectMap<String, String> defines)
        {
            super.getDefines(defines);

            defines.put("currentMode", mode.toString());
            defines.put("rating", BrainOutServer.Settings.isRatingEnabled() ? "enabled" : "disabled");
        }

        public JsonValue getSettings()
        {
            return settings;
        }
    }

    public ServerSettings()
    {
        this.prices = new ObjectMap<>();
        this.levels = new ObjectMap<>();
        this.baseConditions = new BaseConditions();
        this.presets = new ObjectMap<>();
    }

    @Override
    public void write(Json json)
    {
        super.write(json);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.modeDelay = jsonData.getFloat("modeDelay", 0);

        this.developerMode = jsonData.getBoolean("developerMode", false);
        this.ratingEnabled = jsonData.getBoolean("ratingEnabled", true);
        this.weaponAutoLoad = jsonData.getBoolean("weaponAutoLoad", true);
        this.fullDrop = jsonData.getString("fullDrop", null);
        this.deathsRequired = jsonData.getInt("deathsRequired", 3);

        if (jsonData.has("plugins"))
        {
            for (JsonValue pluginData : jsonData.get("plugins"))
            {
                Plugin plugin = json.readValue(Plugin.class, pluginData);

                BrainOutServer.Controller.installPlugin(plugin);
            }
        }


        if (jsonData.has("levels"))
        {
            for (JsonValue value : jsonData.get("levels"))
            {
                levels.put(value.name(), value.asString());
            }
        }

        if (jsonData.has("greetings"))
        {
            String greetingsFile = jsonData.getString("greetings");

            try
            {
                this.greetings = Gdx.files.local(greetingsFile).readString();
            }
            catch (GdxRuntimeException ignored)
            {

            }
        }

        this.baseConditions.read(json, jsonData);

        this.name = json.readValue("name", String.class, jsonData);
        this.dailyContainer = json.readValue("daily-container", String.class, "", jsonData);

        if (jsonData.has("respawn"))
        {
            JsonValue respawn = jsonData.get("respawn");

            this.respawnKind = ServerController.RespawnKind.valueOf(respawn.getString("kind", ServerController.RespawnKind.waves.toString()));
            this.respawnWaveTime = respawn.getFloat("waveTime", 30);
            this.respawnWaveMinTime = respawn.getFloat("waveMinTime", 10);
            this.respawnWaveRate = respawn.getFloat("rate", 0);
        }
        else
        {
            this.respawnKind = ServerController.RespawnKind.waves;
        }

        this.mapsFilter = jsonData.getString("maps-filter", "");

        if (jsonData.has("prices"))
        {
            for (JsonValue port: jsonData.get("prices"))
            {
                prices.put(port.name, port.asInt());
            }
        }

        if (jsonData.has("autoBalance"))
        {
            JsonValue autoBalance = jsonData.get("autoBalance");

            this.autoBalanceEnabled = json.readValue("enabled", Boolean.class, true, autoBalance);
            this.autoBalanceValue = json.readValue("balance", Integer.class, 2, autoBalance);
        }

        this.teamNames = json.readValue(Array.class, String.class, jsonData.get("teams"));

        this.restartIn = jsonData.getFloat("restartIn", 0);
        this.roundTime = jsonData.getLong("roundTime", 1200);

        if (jsonData.has("presets"))
        {
            JsonValue presets = jsonData.get("presets");
            for (JsonValue preset : presets)
            {
                Preset preset_ = new Preset();
                preset_.read(json, preset);
                this.presets.put(preset.name(), preset_);
            }
        }
    }

    public int getPrice(String name)
    {
        Integer award = prices.get(name);
        if (award != null)
        {
            return award;
        }

        return 0;
    }

    public String getName()
    {
        return name;
    }

    public String getZone() {
        return zone;
    }

    public long getLastConflict()
    {
        return lastConflict;
    }

    public void setLastConflict(long lastConflict)
    {
        this.lastConflict = lastConflict;
    }

    public void setZone(String zone)
    {
        this.zone = zone;
    }

    public Array<String> getTeamNames() {
        return teamNames;
    }

    public boolean checkTeamFire(ActiveData launching, ActiveData victim)
    {
        if (BrainOutServer.PackageMgr.getDefine("friendlyFire", "false").equals("true"))
        {
            return true;
        }

        PlayState ps = BrainOutServer.Controller.getPlayState();
        if (ps instanceof PlayStateGame)
        {
            PlayStateGame playStateGame = ((PlayStateGame) ps);

            GameMode gameMode = playStateGame.getMode();

            return gameMode.isEnemiesActive(launching, victim);
        }

        return launching != victim;
    }

    public boolean checkTeamFire(Team launching, Team victim)
    {
        if (BrainOutServer.PackageMgr.getDefine("friendlyFire", "false").equals("true"))
        {
            return true;
        }

        PlayState ps = BrainOutServer.Controller.getPlayState();
        if (ps instanceof PlayStateGame)
        {
            PlayStateGame playStateGame = ((PlayStateGame) ps);

            GameMode gameMode = playStateGame.getMode();

            return gameMode.isEnemies(launching, victim);
        }

        return launching != victim;
    }

    public boolean checkTeamFire(Client launching, Client victim)
    {
        if (launching == null || victim == null)
            return false;

        if (BrainOutServer.PackageMgr.getDefine("friendlyFire", "false").equals("true"))
        {
            return true;
        }

        PlayState ps = BrainOutServer.Controller.getPlayState();
        if (ps instanceof PlayStateGame)
        {
            PlayStateGame playStateGame = ((PlayStateGame) ps);

            GameMode gameMode = playStateGame.getMode();

            return ((ServerRealization) gameMode.getRealization()).isEnemies(launching, victim);
        }

        return launching != victim;
    }

    public boolean isAutoBalanceEnabled()
    {
        return autoBalanceEnabled;
    }

    public int getAutoBalanceValue()
    {
        return autoBalanceValue;
    }

    public float getRestartIn()
    {
        return restartIn;
    }

    public ServerController.RespawnKind getRespawnKind()
    {
        return respawnKind;
    }

    public float getRespawnWaveTime()
    {
        return respawnWaveTime;
    }

    public float getRespawnWaveMinTime()
    {
        return respawnWaveMinTime;
    }

    public float getRespawnWaveRate()
    {
        return respawnWaveRate;
    }

    public float getModeDelay()
    {
        return modeDelay;
    }

    public ObjectMap<String, String> getLevels()
    {
        return levels;
    }

    public BaseConditions getBaseConditions()
    {
        return baseConditions;
    }

    public boolean isDeveloperMode()
    {
        return developerMode;
    }

    public boolean isProductionMode()
    {
        return !developerMode;
    }

    public boolean isRatingEnabled()
    {
        return ratingEnabled;
    }

    public String getDailyContainer()
    {
        return dailyContainer;
    }

    public String getMapsFilter()
    {
        return mapsFilter;
    }

    public Preset getPreset(String id)
    {
        return presets.get(id);
    }

    public ObjectMap<String, Integer> getPrices()
    {
        return prices;
    }

    public long getRoundTime()
    {
        return roundTime;
    }

    public boolean isWeaponAutoLoad()
    {
        return weaponAutoLoad;
    }

    public String getFullDrop()
    {
        return fullDrop;
    }

    public int getDeathsRequired()
    {
        return deathsRequired;
    }
}
