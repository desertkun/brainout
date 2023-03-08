package com.desertkun.brainout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.controllers.GameController;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.IntroMenu;
import com.desertkun.brainout.menu.impl.PackagedLoadingMenu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.FreeplayInventoryUserPanel;
import com.desertkun.brainout.menu.ui.TechUserPanel;
import com.desertkun.brainout.menu.ui.UserPanel;
import com.desertkun.brainout.packages.PackageManager;

import java.util.Map;

public abstract class ClientEnvironment extends Environment
{
    private GameUser gameUser;
    private String currentRoom;
    private Graphics.DisplayMode targetFullScreenDisplayMode;

    public abstract GameController getGameController();

    public void initClientDefines(PackageManager packageMgr)
    {
    }

    public enum Platform
    {
        windows,
        mac,
        linux,
        unknown
    }

    public ClientEnvironment()
    {

    }

    public String getAppName()
    {
        return "brainout";
    }

    @Override
    public void init()
    {
        super.init();

        if (this.gameUser == null)
        {
            this.gameUser = newUser();
        }
    }

    public String getDefaultCurrency()
    {
        return "USD";
    }

    public String getStoreName()
    {
        return null;
    }

    public boolean storeEnabled()
    {
        return true;
    }

    public boolean greenlightEnabled() { return true; }

    public boolean enableNSFW()
    {
        return true;
    }

    public boolean hasProbabilitiesMenu()
    {
        return false;
    }

    public void openProbabilitiesMenu(GameState gs)
    {
        //
    }

    public UserPanel createUserPanel(boolean caseButton)
    {
        return new UserPanel(caseButton);
    }

    public TechUserPanel createTechUserPanel()
    {
        return new TechUserPanel();
    }

    public FreeplayInventoryUserPanel createFreeplayInventoryUserPanel(ConsumableContainer inventory)
    {
        return new FreeplayInventoryUserPanel(inventory);
    }

    public abstract String getStoreComponent();

    public void getStoreEnvironment(Map<String, String> env)
    {
        env.put("language", BrainOutClient.LocalizationMgr.getCurrentLanguage());
    }

    public boolean openURI(String uri)
    {
        boolean success = Gdx.net.openURI(uri);

        if (success)
        {
            Gdx.app.postRunnable(() ->
                BrainOutClient.getInstance().topState().pushMenu(new AlertPopup(L.get("MENU_BROWSER_TAB"))));
        }

        return success;
    }


    public boolean openURI(String uri, Runnable done)
    {
        boolean success = openURI(uri);
        done.run();
        return success;
    }

    public GameUser.Account getCurrentAccount()
    {
        return getGameUser().getAccounts().getAccount();
    }

    public static boolean isWindows()
    {
        String osName = System.getProperty("os.name").toLowerCase();
        return (osName.contains("win"));
    }

    public static boolean isMac()
    {
        String osName = System.getProperty("os.name").toLowerCase();
        return (osName.contains("mac"));
    }

    public static boolean isUnix()
    {
        String osName = System.getProperty("os.name").toLowerCase();
        return (osName.contains("nix") || osName.contains("nux")|| osName.contains("aix"));
    }

    public abstract GameUser newUser();

    public static Platform getPlatform()
    {
        if (isWindows())
        {
            return Platform.windows;
        }
        if (isMac())
        {
            return Platform.mac;
        }
        if (isUnix())
        {
            return Platform.linux;
        }

        return Platform.unknown;
    }

    @Override
    protected void initEnvironmentValues(ObjectMap<String, String> data)
    {
        super.initEnvironmentValues(data);

        final GameUser user = getGameUser();
        if (user != null && user.getAccounts() != null)
        {
            GameUser.Account account = user.getAccounts().getAccount();

            if (account != null)
            {
                data.put("anon-id", account.getId());
                data.put("anon-env", user.getAccounts().getCurrentEnvironment());
            }
        }

        data.put("user-version", String.valueOf(GameUser.CURRENT_PROFILE_VERSION));
    }

    public GameUser getGameUser()
    {
        return gameUser;
    }

    public void initOnline()
    {
        BrainOutClient.ClientController.initOnline();
    }

    public void update(float dt)
    {
        if (getGameController() != null)
        {
            getGameController().update(dt);
        }
    }

    @Override
    public void release()
    {
        super.release();

        gameUser.release();
    }

    public void pause()
    {
        //
    }

    public void gameStarted(String room)
    {
        currentRoom = room;
    }

    public void setCurrentRoom(String currentRoom)
    {
        this.currentRoom = currentRoom;
    }

    public void gameCompleted()
    {
        currentRoom = null;
    }

    public void resume()
    {
        //
    }

    public void setTargetFullScreenDisplayMode(Graphics.DisplayMode targetFullScreenDisplayMode)
    {
        this.targetFullScreenDisplayMode = targetFullScreenDisplayMode;
    }

    public Graphics.DisplayMode getTargetFullScreenDisplayMode()
    {
        return targetFullScreenDisplayMode;
    }

    public String getCurrentRoom()
    {
        return currentRoom;
    }

    public String getUserCurrency()
    {
        return BrainOutClient.ClientController.getUserProfile().getCurrency();
    }

    public boolean storeAccessToken()
    {
        return true;
    }

    public Menu createIntroMenu()
    {
        return new IntroMenu();
    }

    public Menu createPackageLoadingMenu()
    {
        return new PackagedLoadingMenu();
    }

    public String getOfflineBuildError(String kind)
    {
        return "You are running an offline build server, therefore the matchmaking is not possible. Please run the server in the mode " + kind +
                " instead to experience this option.";
    }
}
