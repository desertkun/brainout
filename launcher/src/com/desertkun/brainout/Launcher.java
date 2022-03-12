package com.desertkun.brainout;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.desertkun.brainout.menu.AlertMenu;
import com.desertkun.brainout.menu.ConnectingMenu;
import com.desertkun.brainout.menu.DownloadMenu;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.utils.MD5Hash;
import org.anthillplatform.runtime.OnlineLib;
import org.anthillplatform.runtime.entity.ApplicationInfo;
import org.anthillplatform.runtime.services.DLCService;

import java.io.IOException;
import java.util.LinkedList;

public class Launcher implements ApplicationListener
{
    public static Launcher APP;
    public static Skin SKIN;
    public static OnlineLib Online;

    public static String SUBFOLDER = "brainout";


    public static String GAMESPACE = "brainout:launcher";
    public static String ENV_SERVICE = "http://env.brainout.org";
    public static String APP_ID = "brainout_launcher";
    public static String APP_VERSION = "1.0";

    public static String GAME_ID = "brainout";
    public static String GAME_VERSION = "last";

    private final String[] launchArguments;

    private Menu currentMenu;
    private LinkedList<DownloadFile> filesToDownload;

    public Launcher(String[] launchArguments)
    {
        this.launchArguments = launchArguments;
    }

    @Override
    public void create()
    {
        APP = this;
        currentMenu = null;

        init();

        setMenu(new ConnectingMenu());
    }

    public static class DownloadFile
    {
        public String url;
        public String name;
    }

    public static float getWidth()
    {
        return Gdx.graphics.getWidth();
    }

    public static float getHeight()
    {
        return Gdx.graphics.getHeight();
    }

    private void init()
    {
        ApplicationInfo info = new ApplicationInfo(
            ENV_SERVICE, APP_ID, APP_VERSION, GAMESPACE);

        Online = new OnlineLib(info, "0.2");

        FileHandle version = Gdx.files.local(SUBFOLDER + "/version.txt");

        if (version.exists())
        {
            GAME_VERSION = version.readString();
        }

        filesToDownload = new LinkedList<DownloadFile>();

        BitmapFont font = new BitmapFont(Gdx.files.internal("assets/medium-20.fnt"));
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("assets/LAUNCHER.atlas"));

        SKIN = new Skin();

        SKIN.addRegions(atlas);
        SKIN.add("medium-20", font);

        SKIN.load(Gdx.files.internal("assets/skin.json"));
    }

    public void setMenu(Menu menu)
    {
        if (this.currentMenu != null)
        {
            this.currentMenu.release();
        }

        this.currentMenu = menu;
        this.currentMenu.init();

        Gdx.input.setInputProcessor(this.currentMenu);
    }

    @Override
    public void resize(int width, int height)
    {

    }

    @Override
    public void render()
    {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (currentMenu != null)
        {
            currentMenu.draw();
            currentMenu.act();
        }
    }

    @Override
    public void pause()
    {

    }

    @Override
    public void resume()
    {

    }

    @Override
    public void dispose()
    {
        if (this.currentMenu != null)
        {
            this.currentMenu.release();
        }
    }

    public static TextureRegion getRegion(String name)
    {
        return SKIN.getRegion(name);
    }

    public static NinePatch getNinePatch(String name)
    {
        return SKIN.getPatch(name);
    }

    public void download(DLCService service)
    {
        try
        {
            this.filesToDownload.clear();

            for (DLCService.DLCRecord record: service.getFileRecords())
            {
                String fileName = SUBFOLDER + "/" + record.name;
                String hash = record.hash;
                String url = record.url;

                FileHandle fileHandle = Gdx.files.local(fileName);

                boolean download = false;

                if (fileHandle.exists())
                {
                    String newHash = MD5Hash.hashFile(fileHandle.file());
                    if (newHash == null || !newHash.equals(hash))
                    {
                        download = true;
                    }
                }
                else
                {
                    download = true;
                }

                if (download)
                {
                    DownloadFile downloadFile = new DownloadFile();
                    downloadFile.name = fileName;
                    downloadFile.url = url;

                    filesToDownload.add(downloadFile);
                }
            }

            downloadNext();
        }
        catch (Exception e)
        {
            setMenu(new AlertMenu(e.getMessage(), "Try again", new Runnable()
            {
                @Override
                public void run()
                {
                    setMenu(new ConnectingMenu());
                }
            }));
        }
    }

    private void downloadNext()
    {
        if (filesToDownload.isEmpty())
        {
            launch();
        }
        else
        {
            final DownloadFile downloadFile = filesToDownload.pop();
            setMenu(new DownloadMenu(downloadFile, new DownloadMenu.DownloadResult()
            {
                @Override
                public void success()
                {
                    downloadNext();
                }

                @Override
                public void failed(String text)
                {
                    setMenu(new AlertMenu("Error downloading file " + downloadFile.name + ": " + text, "Try again", new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setMenu(new ConnectingMenu());
                        }
                    }));
                }
            }));
        }
    }

    public static void launch()
    {
        try
        {
            FileHandle toLaunchFile = Gdx.files.local("launch.txt");

            String toLaunch;

            if (toLaunchFile.exists())
            {
                toLaunch = toLaunchFile.readString();
            }
            else
            {
                toLaunch = "java -Xms512m -Xmx1024m -jar brainout-desktop.jar";
            }

            if (APP.launchArguments.length > 0)
            {
                StringBuilder builder = new StringBuilder();

                builder.append(toLaunch).append(" ");

                for(String s : APP.launchArguments)
                {
                    builder.append(s).append(" ");
                }

                toLaunch = builder.toString();
            }

            Runtime.getRuntime().exec(toLaunch, new String[]{},
                Gdx.files.local(SUBFOLDER).file());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Gdx.app.exit();
    }
}
