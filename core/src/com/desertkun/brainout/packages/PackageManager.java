package com.desertkun.brainout.packages;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.common.enums.OperationList;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.managers.ContentManager;
import com.esotericsoftware.minlog.Log;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackageManager implements AssetErrorListener
{
    protected ObjectMap<String, ContentPackage> packages;
    protected AssetManager[] assetManagers;
    protected Array<ContentPackage> loadingPackages;
    private ObjectMap<String, String> defines;
    private ObjectMap<Content, AssetManager> bound;
    private Runnable loadComplete;
    private boolean validationError;

    private static Pattern IF_MATCH = Pattern.compile("(.+?)\\s*(>|<|==|!=)\\s*(.+?)");

    public PackageManager(int threads)
    {
        this.packages = new ObjectMap<>();
        this.loadingPackages = new Array<>();

        this.bound = new ObjectMap<>();
        this.assetManagers = new AssetManager[threads];

        for (int i = 0; i < threads; i++)
        {
            AssetManager m = new AssetManager(this::getFile);

            m.setErrorListener(this);
            m.setLogger(new Logger("AssetManager #" + i, Application.LOG_INFO));

            this.assetManagers[i] = m;
        }

        defines = new ObjectMap<>();
        validationError = false;
    }

    public boolean isValidationError()
    {
        return validationError;
    }

    public FileHandle getFile(String fileName)
    {
        String[] res = fileName.split(":", 2);
        if (res.length >= 2)
        {
            String packageName = res[0];
            String file = res[1];

            ContentPackage pkg = packages.get(packageName);
            if (pkg != null)
            {
                return pkg.getFile(file).handle();
            }
        }
        else
        {
            // if we have no package selection, so try to getByIndex from all

            for (ObjectMap.Entry<String, ContentPackage> entry: packages)
            {
                if (entry.value.hasFile(fileName))
                {
                    return entry.value.getFile(fileName).handle();
                }
            }
        }

        return Gdx.files.absolute(fileName);
    }

    public ContentPackage createPackage(String name) throws ContentPackage.ValidationException
    {
        return new ZipContentPackage(name);
    }

    public static Integer versionCompare(String str1, String str2)
    {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;

        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i]))
        {
            i++;
        }
        if (i < vals1.length && i < vals2.length)
        {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        else
        {
            return Integer.signum(vals1.length - vals2.length);
        }
    }

    public void loadPackages(final Runnable loadComplete)
    {
        if (BrainOut.Operations.isStarted())
        {
            this.loadComplete = loadComplete;
            return;
        }

        loadingPackages.sort((o1, o2) -> {
            if (o1.getDependencies().indexOf(o2.getName(), false) >= 0)
            {
                return 1;
            }

            if (o2.getDependencies().indexOf(o1.getName(), false) >= 0)
            {
                return -1;
            }

            return 0;
        });

        for (ContentPackage pack: loadingPackages)
        {
            pack.loadContentHeader();
        }

        this.loadComplete = loadComplete;

        initOperations();

        BrainOut.Operations.start();
    }

    protected float getAssetManagersProgress()
    {
        float p = 0;

        for (AssetManager manager : assetManagers)
        {
            p += manager.getProgress();
        }

        return p / assetManagers.length;
    }

    protected boolean updateAssetManagers()
    {
        boolean done = true;

        for (AssetManager manager : assetManagers)
        {
            if (!manager.update())
                done = false;
        }

        return done;
    }

    private void clearAssetManagers()
    {
        for (AssetManager manager : assetManagers)
        {
            manager.dispose();
            manager.clear();
        }

        bound.clear();
    }

    public AssetManager getFirstAssetManager()
    {
        return assetManagers[0];
    }

    protected void initOperations()
    {
        BrainOut.Operations.addOperation(new OperationList.Operation()
        {
            @Override
            public void run()
            {
                ContentPackage.ContentBoundAssetManager bind = new ContentPackage.ContentBoundAssetManager()
                {
                    private int cnt = 0;

                    @Override
                    public AssetManager get(Content c)
                    {
                        if (assetManagers.length == 1)
                        {
                            AssetManager result = assetManagers[0];
                            bound.put(c, result);
                            return result;
                        }

                        if (isParallelLoadingRequired(c))
                        {
                            AssetManager result = assetManagers[cnt];
                            bound.put(c, result);

                            cnt++;
                            if (cnt >= assetManagers.length - 1)
                            {
                                cnt = 0;
                            }

                            return result;
                        }
                        else
                        {
                            AssetManager result = assetManagers[assetManagers.length - 1];
                            bound.put(c, result);
                            return result;
                        }
                    }
                };

                for (ContentPackage pack : loadingPackages)
                {
                    pack.loadContent(bind);
                }
            }

            @Override
            public void done()
            {
                packagesLoaded();

                bound.clear();
            }

            @Override
            public boolean complete(float dt)
            {
                for (int i = 0; i < 4; i++)
                {
                    if (updateAssetManagers())
                    {
                        return true;
                    }
                }

                return false;
            }

            @Override
            public float getProgress()
            {
                return getAssetManagersProgress();
            }
        });
    }

    protected boolean isParallelLoadingRequired(Content c)
    {
        return false;
    }

    public void registerPackage(String name)
    {
        if (packages.containsKey(name)) return;

        if (Log.INFO) Log.info("Registering package: '" + name + "'");

        ContentPackage pack = null;
        try
        {
            pack = createPackage(name);
        }
        catch (ContentPackage.ValidationException e)
        {
            validationError = true;
            return;
        }

        if (pack.isHeaderLoaded())
        {
            packages.put(pack.getName(), pack);
            loadingPackages.add(pack);
        }
    }

    protected String getPackagesFolder()
    {
        return "packages";
    }

    public void searchPackages()
    {
        FileHandle packagesDir = Gdx.files.local(getPackagesFolder());

        if (packagesDir.isDirectory())
        {
            for (FileHandle fileHandle: packagesDir.list())
            {
                if (!fileHandle.isDirectory())
                {
                    registerPackage(fileHandle.nameWithoutExtension());
                }
            }
        }
    }

    public boolean validate()
    {
        for (ContentPackage aPackage : packages.values())
        {
            if (!aPackage.validate())
            {
                return false;
            }
        }

        return true;
    }

    private void packagesLoaded()
    {
        ContentPackage.ContentBoundAssetManager b = c -> bound.get(c);

        for (ContentPackage pack: loadingPackages)
        {
            pack.completeLoad(b);
        }

        for (ContentPackage pack: loadingPackages)
        {
            pack.releaseContentHeader();
            BrainOut.getInstance().packageLoaded(pack);
        }

        loadingPackages.clear();

        if (loadComplete != null)
        {
            loadComplete.run();
            loadComplete = null;
        }
    }

    public interface UnloadPackagesPredicate
    {
        boolean test(ContentPackage contentPackage);
    }

    public void unloadPackages(boolean clearDefines)
    {
        unloadPackages(clearDefines, null);
    }

    public void unloadPackages(boolean clearDefines, UnloadPackagesPredicate predicate)
    {
        if (predicate == null)
        {
            BrainOut.ContentMgr.unloadAllContent();
            packages.clear();
        }
        else
        {
            BrainOut.ContentMgr.unloadContent(content ->
                predicate.test(content.getPackage()));

            Array<String> toRemove = new Array<>();

            for (ObjectMap.Entry<String, ContentPackage> entry : packages)
            {
                if (predicate.test(entry.value))
                    continue;

                toRemove.add(entry.key);
            }

            for (String key : toRemove)
            {
                if (Log.INFO) Log.info("Unloaded package: " + key);

                packages.remove(key);
            }
        }

        if (clearDefines)
        {
            clearDefines();
        }
    }

    public String resolve(String define)
    {
        return getDefines().get(define);
    }

    public boolean matchIfdef(String condition)
    {
        for (ObjectMap.Entry<String, String> entry : getDefines())
        {
            if (entry.value != null)
            {
                condition = condition.replace("$" + entry.key, entry.value);
            }
        }

        Matcher m = IF_MATCH.matcher(condition);

        if (m.matches())
        {
            String a = m.group(1);
            String b = m.group(3);
            String check = m.group(2);

            if (check.equals("=="))
            {
                return a.equals(b);
            }
            else if (check.equals("!="))
            {
                return !a.equals(b);
            }

            return false;
        }
        else
        {
            // just check for existance
            return getDefines().containsKey(condition);
        }
    }

    @Override
    public void error(AssetDescriptor asset, Throwable throwable)
    {
        throwable.printStackTrace();
    }

    public float getLoadProgress()
    {
        return BrainOut.Operations.getProgress();
    }

    public ObjectMap<String, ContentPackage> getPackages()
    {
        return packages;
    }

    public String getDefine(String id, String def)
    {
        return defines.get(id, def);
    }

    public ObjectMap<String, String> getDefines()
    {
        return defines;
    }

    public void clearDefines()
    {
        defines.clear();
    }

    public void setDefine(String define, String value)
    {
        defines.put(define, value);
    }

    public void checkValidation()
    {
        if (isValidationError())
        {
            System.err.println("Validation error, please reinstall the game");
            BrainOut.exit(-1);
        }
    }
}
