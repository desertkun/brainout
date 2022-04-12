package com.desertkun.brainout.playstate;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.common.msg.client.editor.CreateMapMsg;
import com.desertkun.brainout.common.msg.client.editor.GetMapListMsg;
import com.desertkun.brainout.common.msg.client.editor.LoadMapMsg;
import com.desertkun.brainout.common.msg.server.editor.MapListMsg;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.packages.ContentPackage;
import com.desertkun.brainout.server.mapsource.EditorMapSource;
import com.desertkun.brainout.server.mapsource.EmptyMapSource;
import com.desertkun.brainout.server.mapsource.SingleMapSource;
import com.desertkun.brainout.server.mapsource.StreamMapSource;

import java.io.File;
import java.io.InputStream;

public class ServerPSEmpty extends PlayStateEmpty
{
    private PlayerClient messageClient;

    @Override
    public void init(InitCallback done)
    {
        BrainOutServer.PackageMgr.loadPackages(() -> packagesLoaded(done));
    }

    private void packagesLoaded(InitCallback done)
    {
        done.done(true);
    }

    @SuppressWarnings("unused")
    public boolean received(final GetMapListMsg msg)
    {
        final PlayerClient messageClient = getMessageClient();

        BrainOutServer.PostRunnable(() ->
        {
            String mapsFilter = BrainOutServer.Settings.getMapsFilter();

            File dir = new File("maps");
            File [] files = dir.listFiles((dir1, name) -> {

                if (!mapsFilter.isEmpty())
                {
                    if (!name.startsWith(mapsFilter))
                    {
                        return false;
                    }
                }

                return name.endsWith(".map");
            });

            Array<String> maps = new Array<>();

            for (File file : files)
            {
                maps.add(file.getName());
            }

            String[] asArray = maps.toArray(String.class);

            messageClient.sendTCP(new MapListMsg(asArray));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final LoadMapMsg msg)
    {
        if (!validateMod()) return true;
        if (!msg.map.matches("^[A-Za-z0-9_-]+\\.map$")) return true;

        BrainOutServer.PostRunnable(() -> {
            BrainOutServer.Controller.setMapSource(new SingleMapSource("maps/" + msg.map, GameMode.ID.editor));

            BrainOutServer.PackageMgr.unloadPackages(true);
            BrainOutServer.Controller.next(null);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final CreateMapMsg msg)
    {
        if (!validateMod()) return true;

        BrainOutServer.PostRunnable(() -> {
            String mapName = msg.mapName + ".map";
            BrainOutServer.Controller.setMapSource(new EditorMapSource(mapName,
                    msg.mapWidth, msg.mapHeight));

            BrainOutServer.PackageMgr.unloadPackages(true);
            BrainOutServer.Controller.next(null);
        });

        return true;
    }

    @Override
    public boolean received(Object from, ModeMessage o)
    {
        this.messageClient = ((PlayerClient) from);

        return super.received(from, o);
    }

    public PlayerClient getMessageClient()
    {
        return messageClient;
    }

    public boolean validateMod()
    {
        /*
        final PlayerClient messageClient = getMessageClient();
        if (isMod(messageClient)) return true;

        messageClient.sendTCP(new SimpleMsg(SimpleMsg.Code.notAllowed));

        return false;
        */

        return true;
    }

    public boolean isMod(Client client)
    {
        switch (client.getRights())
        {
            case admin:
            case mod:
            case owner:
                return true;
            default:
                return false;
        }
    }

    public interface UploadMapResult
    {
        void result(boolean success);
    }

    public void uploadMap(InputStream input, UploadMapResult result, String mapName, ObjectMap<String, String> custom)
    {
        Map.Dispose();

        BrainOutServer.Controller.setMapSource(
            new StreamMapSource(input, getNextMode(), mapName, custom));

        BrainOutServer.PackageMgr.unloadPackages(true);
        BrainOutServer.Controller.next(success ->
        {
            if (success)
            {
                result.result(true);
            }
            else
            {
                result.result(false);
                rollback();
            }
        });
    }

    private void rollback()
    {
        BrainOutServer.Controller.setMapSource(new EmptyMapSource(getNextMode()));
        BrainOutServer.PackageMgr.unloadPackages(true);
        BrainOutServer.Controller.next(null);
    }


    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        super.write(json, componentWriter, owner);

        json.writeObjectStart("defines");
        for (ObjectMap.Entry<String, String> entry : BrainOutServer.PackageMgr.getDefines())
        {
            json.writeValue(entry.key, entry.value);
        }
        json.writeObjectEnd();

        json.writeArrayStart("packages");
        for (ObjectMap.Entry<String, ContentPackage> entry: BrainOut.PackageMgr.getPackages())
        {
            json.writeObjectStart();

            json.writeValue("name", entry.key);
            json.writeValue("version", entry.value.getVersion());
            long crc32 = entry.value.getCRC32();

            json.writeValue("crc32", crc32, Long.class);

            json.writeObjectEnd();
        }
        json.writeArrayEnd();
    }

}
