package com.desertkun.brainout.packages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.utils.FileCopy;

import java.io.File;
import java.io.IOException;

public class AndroidPackage extends ClientContentPackage
{
    public AndroidPackage(String name)
    {
        super(name);
    }

    public class AndroidFileHandle extends PackageFileHandle
    {
        public AndroidFileHandle(String path)
        {
            super(path);
        }

        @Override
        public FileHandle handle()
        {
            try
            {
                File cacheDir = BrainOut.Env.getCacheDir();

                String ext = ".temp";

                int lastIndex = entryName.lastIndexOf('.');
                if (lastIndex > 0) {
                    ext = entryName.substring(lastIndex);
                }

                File f;
                if (cacheDir != null)
                {
                    f = File.createTempFile("zipFile" + crc32, ext, cacheDir);
                }
                else
                {
                    f = File.createTempFile("zipFile" + crc32, ext);
                }

                f.deleteOnExit();
                FileCopy.copyFile(read(), f);

                return Gdx.files.absolute(f.getAbsolutePath());
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public PackageFileHandle getFile(String fileName)
    {
        if (fileName.endsWith(".mp3"))
        {
            // extract mp3's because Android can't load them from zip
            return new AndroidFileHandle(fileName);
        }

        return super.getFile(fileName);
    }
}
