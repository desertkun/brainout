package com.desertkun.brainout.editor2;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.GameUser;
import com.desertkun.brainout.L;
import com.desertkun.brainout.data.Editor2Map;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.WaitLoadingMenu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.LoadingBlock;

import java.io.File;

public class PublishMapMenu extends WaitLoadingMenu
{
    private final File previewFile;
    private final File mapFile;
    private final String name;
    private final String description;
    private final ObjectSet<String> modes;

    public PublishMapMenu(File previewFile, File mapFile, String name, String description, ObjectSet<String> modes)
    {
        super(L.get("EDITOR2_PUBLISHING"));

        this.previewFile = previewFile;
        this.mapFile = mapFile;
        this.name = name;
        this.description = description;
        this.modes = modes;
    }

    @Override
    public void onInit()
    {
        super.onInit();

        GameUser gameUser = BrainOutClient.Env.getGameUser();

        ObjectMap<String, File> files = new ObjectMap<>();
        files.put("map", mapFile);

        Array<String> tags = new Array<>();
        tags.add("map");
        for (String mode : modes)
        {
            tags.add(mode);
        }

        gameUser.publishWorkshopItem(name, description, previewFile, files, tags.toArray(String.class),
            "Created", new GameUser.WorkshopUploadCallback()
        {
            @Override
            public void complete(String itemID, String url)
            {
                publishComplete(itemID, url);
            }

            @Override
            public void needToAcceptWLA(Runnable done)
            {
                if (!BrainOutClient.Env.openURI(BrainOutClient.Env.getGameUser().getWorkshopLegalTermsLink()))
                {
                    done.run();
                }
            }

            @Override
            public void failed(String reason)
            {
                PublishMapMenu.this.popMeAndPushMenu(new AlertPopup(reason));
            }
        });
    }

    private void publishComplete(String itemID, String url)
    {
        for (Editor2Map map : Map.All(Editor2Map.class))
        {
            map.setCustom("workshop-item", itemID);
        }

        pop();

        BrainOutClient.Env.openURI(url);
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public boolean escape()
    {
        return false;
    }
}
