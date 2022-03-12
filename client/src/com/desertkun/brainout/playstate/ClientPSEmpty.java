package com.desertkun.brainout.playstate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.states.CSEmpty;
import com.desertkun.brainout.client.states.packages.CSPackagesLoad;
import com.desertkun.brainout.gs.LoadingState;

public class ClientPSEmpty extends PlayStateEmpty
{
    private final Array<String> packagesToLoad;
    private final Array<PackageInfo> packages;

    public ClientPSEmpty()
    {
        this.packagesToLoad = new Array<>();
        this.packages = new Array<>();
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

        BrainOutClient.ClientController.setState(new CSPackagesLoad(packagesToLoad, new Runnable()
        {
            @Override
            public void run()
            {
                BrainOutClient.getInstance().switchState(new LoadingState());

                // game play state means download map and so on
                BrainOutClient.ClientController.setState(new CSEmpty(getNextMode()));
            }
        }));
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        Gdx.app.postRunnable(this::updated);

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
        //
    }

}
