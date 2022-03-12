package com.desertkun.brainout.packages;

import com.badlogic.gdx.assets.AssetManager;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.common.enums.OperationList;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.effect.RevSoundEffect;
import com.desertkun.brainout.content.effect.SoundEffect;

import java.io.File;

public class ClientPackageManager extends PackageManager
{
    public ClientPackageManager(int threads)
    {
        super(threads);
    }

    @Override
    public ContentPackage createPackage(String name) throws ContentPackage.ValidationException
    {
        return new ClientContentPackage(name);
    }

    @Override
    protected boolean isParallelLoadingRequired(Content c)
    {
        return c instanceof SoundEffect ||
               c instanceof RevSoundEffect;
    }

    @Override
    public String resolve(String define)
    {
        switch (define)
        {
            case "language":
            {
                return BrainOutClient.ClientSett.getLanguage().getSelectValue();
            }
        }

        return super.resolve(define);
    }

    @Override
    protected void initOperations()
    {
        for (ContentPackage p : loadingPackages)
        {
            ClientContentPackage pack = ((ClientContentPackage) p);

            BrainOut.Operations.addOperation(new OperationList.Operation()
            {
                private boolean freeFrame = false;

                @Override
                public void run()
                {
                    pack.loadTextures(getFirstAssetManager());
                }

                @Override
                public void done()
                {
                    pack.texturesLoaded(getFirstAssetManager());
                }

                @Override
                public boolean complete(float dt)
                {
                    if (freeFrame)
                    {
                        return true;
                    }

                    freeFrame = updateAssetManagers();

                    return false;
                }

                @Override
                public float getProgress()
                {
                    return getAssetManagersProgress();
                }
            });
        }

        super.initOperations();
    }
}
