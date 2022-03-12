package com.desertkun.brainout.playstate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.states.packages.CSPackagesLoad;
import com.desertkun.brainout.common.msg.ModeMessage;

import com.desertkun.brainout.gs.LoadingState;
import com.desertkun.brainout.mode.ClientRealization;

public class ClientPSGame extends PlayStateGame
{
    private final Array<String> packagesToLoad;
    private final Array<String> teamNames;
    private final Array<PackageInfo> packages;

    public ClientPSGame()
    {
        this.packagesToLoad = new Array<>();
        this.packages = new Array<>();
        this.teamNames = new Array<>();
    }

    public static class PackageInfo
    {
        public String name;
        public String version;

        public PackageInfo() {}

        public PackageInfo(String name, String version)
        {
            this.name = name;
            this.version = version;
        }
    }

    @Override
    public void init(InitCallback done)
    {
        BrainOutClient.getInstance().switchState(new LoadingState());

        super.init(done);

        for (PackageInfo packageInfo : packages)
        {
            packagesToLoad.add(packageInfo.name);
        }

        if (System.getenv("BRAINOUT_NO_SOUND") != null)
        {
            packagesToLoad.removeValue("sounds", false);
        }

        BrainOutClient.ClientController.setState(new CSPackagesLoad(packagesToLoad));
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        Gdx.app.postRunnable(this::updated);

        teamNames.clear();

        JsonValue teamItems = jsonData.get("teams");

        if (teamItems != null)
        {
            for (JsonValue item : teamItems)
            {
                String name = item.asString();
                teamNames.add(name);
            }
        }

        JsonValue defines = jsonData.get("defines");

        if (defines != null)
        {
            BrainOutClient.PackageMgr.getDefines().clear();

            for (JsonValue define : defines)
            {
                BrainOutClient.PackageMgr.setDefine(define.name(), define.asString());
            }
        }

        initClientDefines();

        JsonValue packages = jsonData.get("packages");

        this.packages.clear();

        if (packages != null)
        {
            for (JsonValue packageValue : packages)
            {
                String name = packageValue.getString("name");
                String version = packageValue.getString("version");

                PackageInfo info = new PackageInfo(name, version);

                this.packages.add(info);
            }
        }
    }

    private void initClientDefines()
    {
        BrainOutClient.ClientController.initClientDefines();
    }

    @Override
    public void release()
    {
        if (getMode() == null)
            return;
        
        getMode().getRealization().release();
    }

    @Override
    public boolean received(Object from, ModeMessage o)
    {
        if (getMode() == null)
            return super.received(from, o);

        ClientRealization realization = (ClientRealization) getMode().getRealization();

        return realization.received(o) || super.received(from, o);
    }

    public Array<String> getTeamNames()
    {
        return teamNames;
    }
}
