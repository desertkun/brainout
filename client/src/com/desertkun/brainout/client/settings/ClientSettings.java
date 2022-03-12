package com.desertkun.brainout.client.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.ClientEnvironment;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.managers.LocalizationManager;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.utils.CoreSettings;

public abstract class ClientSettings extends CoreSettings
{
    private final ClientEnvironment environment;

    private BooleanProperty fullscreen;
    private BooleanProperty vSync;
    private ScreenResolutionProperty displayMode;
    private IntegerEnumProperty graphicsQuality;
    private StringEnumProperty language;
    private BooleanProperty blood;

    private IntegerRangeProperty soundVolume;
    private IntegerRangeProperty musicVolume;
    private IntegerRangeProperty voiceChatVolume;
    private IntegerRangeProperty microphoneVolume;

    private Properties sound;
    private Properties graphics;
    private Properties root;
    private KeyProperties controls;
    private ObjectMap<String, GamePadKeyProperties> gamePadControls;
    private IntegerRangeProperty mouseSensitivity;
    private IntegerRangeProperty gamepadSensitivity;

    private KeycodeProperty keyUp;
    private KeycodeProperty keyDown;
    private KeycodeProperty keyLeft;
    private KeycodeProperty keyRight;

    public static final int GRAPHICS_HIGH = 3;
    public static final int GRAPHICS_MEDIUM= 2;
    public static final int GRAPHICS_LOW = 1;
    public static final int GRAPHICS_VERY_LOW = 0;

    public ClientSettings(ClientEnvironment environment)
    {
        this.environment = environment;

        root = new Properties("properties", null);

        graphics = new Properties("graphics", "MENU_SETTING_GRAPHICS", root);
        sound = new Properties("sound", "MENU_SETTING_SOUND", root);
        controls = new KeyProperties("controls", "MENU_SETTING_CONTROLS", root);
        gamePadControls = new ObjectMap<>();
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        root.write(json);

        json.writeObjectStart("game-pads");

        for (ObjectMap.Entry<String, GamePadKeyProperties> entry : gamePadControls)
        {
            json.writeObjectStart(entry.key);
            entry.value.write(json);
            json.writeObjectEnd();
        }

        json.writeObjectEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        root.read(json, jsonData);

        if (jsonData.has("game-pads"))
        {
            gamePadControls.clear();

            for (JsonValue value : jsonData.get("game-pads"))
            {
                getGamePadControls(value.name).read(json, value);
            }
        }

        update();
    }

    public abstract Graphics.DisplayMode getDefaultDisplayMode();

    public abstract Graphics.DisplayMode[] getDisplayModes();

    public void init()
    {
        fullscreen = new BooleanProperty("fullscreen", "MENU_FULLSCREEN", true, graphics)
        {
            @Override
            public boolean setValue(Boolean value)
            {
                super.setValue(value);

                return true;
            }
        };

        vSync = new BooleanProperty("vSync", "MENU_VSYNC", true, graphics)
        {
            @Override
            public boolean setValue(Boolean value)
            {
                super.setValue(value);

                return true;
            }
        };

        displayMode = new ScreenResolutionProperty("screenSize", "MENU_SCREENSIZE", getDefaultDisplayMode(), graphics);

        language = new StringEnumProperty("lang", "MENU_LANGUAGE", null, graphics)
        {
            @Override
            public boolean update()
            {
                return BrainOutClient.LocalizationMgr.setCurrentLanguage(getValue(), LocalizationManager.GetDefaultLanguage());
            }
        };

        graphicsQuality = new IntegerEnumProperty("graphicsQuality", "MENU_GRAPHICS_QUALITY", GRAPHICS_HIGH, graphics);
        graphicsQuality.addOption(GRAPHICS_HIGH, "MENU_HIGH");
        graphicsQuality.addOption(GRAPHICS_MEDIUM, "MENU_MEDIUM");
        graphicsQuality.addOption(GRAPHICS_LOW, "MENU_LOW");
        graphicsQuality.addOption(GRAPHICS_VERY_LOW, "MENU_VERY_LOW");

        if (environment.enableNSFW())
        {
            blood = new BooleanProperty("blood", "MENU_BLOOD", true, graphics);
        }

        soundVolume = new IntegerRangeProperty("sound", "MENU_SOUND_SFX", 100, 0, 100, sound);
        musicVolume = new IntegerRangeProperty("music", "MENU_SOUND_MUSIC", 100, 0, 100, sound)
        {
            @Override
            public void update()
            {
                BrainOutClient.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.audioUpdated));
            }
        };

        voiceChatVolume = new IntegerRangeProperty("voice-chat", "MENU_CONTROL_VOICE_CHAT", 100, 0, 100, sound)
        {
            @Override
            public void update()
            {
                BrainOutClient.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.audioUpdated));
            }
        };

        microphoneVolume = new IntegerRangeProperty("microphone", "MENU_CONTROL_MICROPHONE", 50, 0, 100, sound)
        {
            @Override
            public void update()
            {
                BrainOutClient.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.audioUpdated));
            }
        };

        mouseSensitivity = new IntegerRangeProperty("mouse-sensitivity",
            "MENU_MOUSE_SENVITIVITY", 50, 0, 100, controls);

        gamepadSensitivity = new IntegerRangeProperty("gamepad-sensitivity",
            "MENU_GAMEPAD_SENVITIVITY", 50, 0, 100, controls);

        initKeyCodes();

        for (ObjectMap.Entry<String, GamePadKeyProperties> entry : gamePadControls)
        {
            initGamePadCodes(entry.value, true);
        }
    }

    public GamePadKeyProperties getGamePadControls(String name)
    {
        GamePadKeyProperties properties = gamePadControls.get(name);

        if (properties == null)
        {
            properties = new GamePadKeyProperties("gamepad", "MENU_GAMEPAD", null);
            gamePadControls.put(name, properties);

            initGamePadCodes(properties, false);
        }

        return properties;
    }

    private void initGamePadCodes(GamePadKeyProperties gamePadControls, boolean update)
    {
        new GamePadKeycodeProperty(
                "move-ud", "MENU_GAMEPAD_1", -1, gamePadControls, GamePadKeyProperties.Keys.moveUpDown, true);
        new GamePadKeycodeProperty(
                "move-lr", "MENU_GAMEPAD_2", -1, gamePadControls, GamePadKeyProperties.Keys.moveLeftRight, true);
        new GamePadKeycodeProperty(
                "aim-ud", "MENU_GAMEPAD_3", -1, gamePadControls, GamePadKeyProperties.Keys.aimUpDown, true);
        new GamePadKeycodeProperty(
                "aim-lr", "MENU_GAMEPAD_4", -1, gamePadControls, GamePadKeyProperties.Keys.aimLeftRight, true);
        new GamePadKeycodeProperty(
                "slot-1", "MENU_GAMEPAD_5", -1, gamePadControls, GamePadKeyProperties.Keys.slotPrimary, false);
        new GamePadKeycodeProperty(
                "slot-2", "MENU_GAMEPAD_6", -1, gamePadControls, GamePadKeyProperties.Keys.slotSecondary, false);
        new GamePadKeycodeProperty(
                "slot-3", "MENU_GAMEPAD_7", -1, gamePadControls, GamePadKeyProperties.Keys.slotSpecial, false);
        new GamePadKeycodeProperty(
                "slot-4", "MENU_GAMEPAD_8", -1, gamePadControls, GamePadKeyProperties.Keys.slotKnife, false);
        new GamePadKeycodeProperty(
                "reload", "MENU_GAMEPAD_9", -1, gamePadControls, GamePadKeyProperties.Keys.reload, false);
        new GamePadKeycodeProperty(
                "fire", "MENU_GAMEPAD_10", -1, gamePadControls, GamePadKeyProperties.Keys.fire, false);
        new GamePadKeycodeProperty(
                "aim", "MENU_GAMEPAD_11", -1, gamePadControls, GamePadKeyProperties.Keys.aim, false);
        new GamePadKeycodeProperty(
                "run", "MENU_GAMEPAD_12", -1, gamePadControls, GamePadKeyProperties.Keys.run, false);
        new GamePadKeycodeProperty(
                "crouch", "MENU_GAMEPAD_13", -1, gamePadControls, GamePadKeyProperties.Keys.crouch, false);
        new GamePadKeycodeProperty(
                "firing-mode", "MENU_GAMEPAD_14", -1, gamePadControls, GamePadKeyProperties.Keys.firingMode, false);
        new GamePadKeycodeProperty(
                "player-list", "MENU_GAMEPAD_15", -1, gamePadControls, GamePadKeyProperties.Keys.playerList, false);

        if (update)
        {
            gamePadControls.update();
        }
    }

    private void initKeyCodes()
    {
        keyUp = new KeycodeProperty("up", "MENU_CONTROL_UP", ClientConstants.Keys.KEY_MOVE_UP, controls,
                KeyProperties.Keys.up);
        keyDown = new KeycodeProperty("down", "MENU_CONTROL_DOWN", ClientConstants.Keys.KEY_MOVE_DOWN, controls,
                KeyProperties.Keys.down);
        keyLeft = new KeycodeProperty("left", "MENU_CONTROL_LEFT", ClientConstants.Keys.KEY_MOVE_LEFT, controls,
                KeyProperties.Keys.left);
        keyRight = new KeycodeProperty("right", "MENU_CONTROL_RIGHT", ClientConstants.Keys.KEY_MOVE_RIGHT, controls,
                KeyProperties.Keys.right);

        new KeycodeProperty("slot-1", "MENU_CONTROL_SLOT_1", Input.Keys.NUM_1, controls,
                KeyProperties.Keys.slotPrimary);
        new KeycodeProperty("slot-2", "MENU_CONTROL_SLOT_2", Input.Keys.NUM_2, controls,
                KeyProperties.Keys.slotSecondary);
        new KeycodeProperty("slot-3", "MENU_CONTROL_SLOT_3", Input.Keys.NUM_3, controls,
                KeyProperties.Keys.slotSpecial);
        new KeycodeProperty("slot-4", "MENU_CONTROL_SLOT_4", Input.Keys.NUM_4, controls,
                KeyProperties.Keys.slotKnife);
        new KeycodeProperty("slot-5", "ITEM_BINOCULARS", Input.Keys.B, controls,
                KeyProperties.Keys.slotBinoculars);
        new KeycodeProperty("slot-6", "ITEM_FLASHLIGHT", Input.Keys.L, controls,
                KeyProperties.Keys.slotFlashlight);
        new KeycodeProperty("last-switch", "MENU_CONTROL_SLOT_LAST", Input.Keys.Q, controls,
                KeyProperties.Keys.previousSlot);

        new KeycodeProperty("activate", "MENU_ACTIVATE", Input.Keys.E, controls,
                KeyProperties.Keys.activate);

        new KeycodeProperty("sit-2", "MENU_CONTROL_SIT", Input.Keys.CONTROL_LEFT, controls,
                KeyProperties.Keys.sit);
        new KeycodeProperty("run", "MENU_CONTROL_RUN", Input.Keys.SHIFT_LEFT, controls,
                KeyProperties.Keys.run);
        new KeycodeProperty("reload", "MENU_CONTROL_RELOAD", Input.Keys.R, controls,
                KeyProperties.Keys.reload);
        new KeycodeProperty("player-list", "MENU_CONTROL_PLAYER_LIST", Input.Keys.TAB, controls,
                KeyProperties.Keys.playerList);
        new KeycodeProperty("drop-weapon", "MENU_CONTROL_DROP", Input.Keys.G, controls,
                KeyProperties.Keys.dropWeapon);
        new KeycodeProperty("drop-ammo-2", "MENU_CONTROL_DROP_AMMO", Input.Keys.F, controls,
                KeyProperties.Keys.dropAmmo);
        new KeycodeProperty("team", "MENU_CONTROL_CHANGE_TEAM", Input.Keys.N, controls,
                KeyProperties.Keys.changeTeam);
        new KeycodeProperty("shoot-mode", "MENU_CONTROL_SHOOT_MODE", Input.Keys.V, controls,
                KeyProperties.Keys.shootMode);
        new KeycodeProperty("unload-weapon", "MENU_CONTROL_UNLOAD_WEAPON", Input.Keys.T, controls,
                KeyProperties.Keys.unloadWeapon);
        new KeycodeProperty("zoom", "MENU_CONTROL_ZOOM", Input.Keys.C, controls,
                KeyProperties.Keys.zoom);
        new KeycodeProperty("chat", "MENU_CONTROL_CHAT", ClientConstants.Keys.KEY_CHAT, controls,
                KeyProperties.Keys.chat);
        new KeycodeProperty("team-chat", "MENU_CONTROL_TEAM_CHAT", ClientConstants.Keys.KEY_TEAM_CHAT, controls,
                KeyProperties.Keys.teamChat);
        new KeycodeProperty("console", "MENU_CONTROL_CONSOLE", ClientConstants.Keys.KEY_DEBUG, controls,
                KeyProperties.Keys.console);
        new KeycodeProperty("voice-chat", "MENU_CONTROL_VOICE_CHAT", Input.Keys.X, controls,
                KeyProperties.Keys.voiceChat);
        new KeycodeProperty("hide-interface", "MENU_CONTROL_HIDE_INTERFACE", Input.Keys.MINUS, controls,
                KeyProperties.Keys.hideInterface);
        new KeycodeProperty("sit-3", "MENU_CONTROL_SQUAT", Input.Keys.Z, controls,
                KeyProperties.Keys.squat);
        new KeycodeProperty("friendly", "MENU_CONTROL_FRIENDS_FP", Input.Keys.F1, controls,
                KeyProperties.Keys.freePlayFriends);

        controls.update();
    }

    public void save()
    {
        try
        {
            new Json().toJson(this, Gdx.files.local("settings.json"));
        }
        catch (Exception ignored)
        {
            //
        }
    }

    public BooleanProperty getFullscreen()
    {
        return fullscreen;
    }

    public BooleanProperty getvSync()
    {
        return vSync;
    }

    public ScreenResolutionProperty getDisplayMode()
    {
        return displayMode;
    }

    public int getLightDiv()
    {
        switch (graphicsQuality.getValue())
        {
            case GRAPHICS_LOW:
            case GRAPHICS_VERY_LOW:
            {
                return 8;
            }
            case GRAPHICS_MEDIUM:
            {
                return 4;
            }
            default:
            {
                return 1;
            }
        }
    }

    public boolean isBackgroundEffectsEnabled()
    {
        switch (graphicsQuality.getValue())
        {
            case GRAPHICS_LOW:
            case GRAPHICS_VERY_LOW:
            {
                return false;
            }
            default:
            {
                return true;
            }
        }
    }

    public IntegerEnumProperty getGraphicsQuality()
    {
        return graphicsQuality;
    }

    public boolean hasShadows()
    {
        if (BrainOutClient.ClientController.getGameMode().getID() == GameMode.ID.free)
        {
            return true;
        }

        return graphicsQuality.getValue() != GRAPHICS_VERY_LOW;
    }

    public boolean hasAnimatedTrees()
    {
        GameMode gm = BrainOutClient.ClientController.getGameMode();
        if (gm == null || gm.getID() != GameMode.ID.free)
        {
            return false;
        }

        return graphicsQuality.getValue() == GRAPHICS_HIGH;
    }

    public Batch allocateNewBatch()
    {
        return hasAnimatedTrees() ? new PolygonSpriteBatch() : new SpriteBatch();
    }

    public boolean hasSoftShadows()
    {
        switch (graphicsQuality.getValue())
        {
            case GRAPHICS_LOW:
            case GRAPHICS_VERY_LOW:
            {
                return false;
            }
            default:
            {
                return true;
            }
        }
    }

    public boolean hasPostEffectShaders()
    {
        switch (graphicsQuality.getValue())
        {
            case GRAPHICS_HIGH:
            {
                return true;
            }
            default:
            {
                return false;
            }
        }
    }

    private static boolean EXTENSIONS_CHECKED = false;
    private static boolean FBO_AVAILABLE = false;

    public static boolean IsFBOSupported()
    {
        if (EXTENSIONS_CHECKED)
        {
            return FBO_AVAILABLE;
        }

        Array<String> exts = new Array<>(Gdx.gl.glGetString(GL20.GL_EXTENSIONS).split(" "));
        FBO_AVAILABLE = exts.contains("GL_ARB_framebuffer_object", false);
        EXTENSIONS_CHECKED = true;
        return FBO_AVAILABLE;
    }

    public boolean isLightsEnabled()
    {
        if (!IsFBOSupported())
        {
            return false;
        }

        GameMode gm = BrainOutClient.ClientController.getGameMode();

        if (gm == null)
            return false;

        if (gm.getID() == GameMode.ID.free)
        {
            return true;
        }

        switch (graphicsQuality.getValue())
        {
            case GRAPHICS_VERY_LOW:
            {
                return false;
            }
            default:
            {
                return true;
            }
        }
    }

    public boolean isShaderEffectsEnabled()
    {
        switch (graphicsQuality.getValue())
        {
            case GRAPHICS_HIGH:
            {
                return true;
            }
            default:
            {
                return false;
            }
        }
    }

    public float getMouseSensitivity()
    {
        float f = mouseSensitivity.getFloatValue();

        return 0.5f + f;
    }

    public float getGamepadSensitivity()
    {
        float f = gamepadSensitivity.getFloatValue();

        return 0.5f + f;
    }

    public boolean hasBlood()
    {
        if (!environment.enableNSFW())
            return false;

        return blood.getValue();
    }

    public StringEnumProperty getLanguage()
    {
        return language;
    }

    public Properties getProperties()
    {
        return root;
    }

    public Properties getGraphics()
    {
        return graphics;
    }

    public IntegerRangeProperty getSoundVolume()
    {
        return soundVolume;
    }

    public IntegerRangeProperty getMusicVolume()
    {
        return musicVolume;
    }

    public IntegerRangeProperty getVoiceChatVolume()
    {
        return voiceChatVolume;
    }

    public IntegerRangeProperty getMicrophoneVolume()
    {
        return microphoneVolume;
    }

    public KeyProperties getControls()
    {
        return controls;
    }

    public KeycodeProperty getKeyUp()
    {
        return keyUp;
    }

    public KeycodeProperty getKeyDown()
    {
        return keyDown;
    }

    public KeycodeProperty getKeyLeft()
    {
        return keyLeft;
    }

    public KeycodeProperty getKeyRight()
    {
        return keyRight;
    }

    public void update()
    {
        controls.update();

        for (ObjectMap.Entry<String, GamePadKeyProperties> entry : gamePadControls)
        {
            entry.value.update();
        }
    }
}
