package com.desertkun.brainout.packages;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.files.FileHandleStream;
import com.desertkun.brainout.BrainOut;
import com.esotericsoftware.minlog.Log;

import java.io.*;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipContentPackage extends ContentPackage
{
    protected ZipFile zip;

    private class PackageDirectoryHandle extends FileHandleStream
    {
        private final String entryName;

        public PackageDirectoryHandle(String path)
        {
            super(path);

            entryName = path;
        }

        @Override
        public boolean isDirectory() {
            return true;
        }

        @Override
        public FileHandle child(String name)
        {
            return new ZipPackageFileHandle(entryName + "/" + name);
        }

        @Override
        public FileHandle parent()
        {
            return super.parent();
        }
    }

    public ZipContentPackage(String name) throws ValidationException
    {
        super(name);

        File packageFile = BrainOut.Env.getFile(getPackagePath());

        try
        {
            this.zip = new ZipFile(packageFile);

            if (Log.INFO) Log.info("Package file loaded, reading header...");

            loadHeader();
        }
        catch (FileNotFoundException e)
        {
            if (Log.INFO) Log.info("Warning: no package " + name + " found.");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public class ZipPackageFileHandle extends PackageFileHandle
    {
        private final ZipEntry entry;
        protected final String entryName;

        public ZipPackageFileHandle(String path)
        {
            super(path);

            entry = zip.getEntry("content/" + path);

            if (entry == null)
            {
                if (Log.ERROR) Log.error("Entry not found: content/" + path);
            }

            entryName = path;
        }

        public FileHandle handle()
        {
            return this;
        }

        @Override
        public String path()
        {
            return getName() + ":" + super.path();
        }

        @Override
        public boolean isDirectory()
        {
            return false;
        }

        @Override
        public File file()
        {
            return null;
        }

        @Override
        public long length()
        {
            InputStream stream = read();

            if (stream != null)
            {
                try
                {
                    return stream.available();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return 0;
                }
            }

            return 0;
        }

        @Override
        public InputStream read()
        {
            try
            {
                return zip.getInputStream(entry);
            }
            catch (IOException e)
            {
                e.printStackTrace();

                return null;
            }
        }

        @Override
        public FileHandle parent()
        {
            int separatorIndex = entryName.lastIndexOf('/');
            return (separatorIndex < 0) ? null : new PackageDirectoryHandle(entryName.substring(0, separatorIndex));
        }

        @Override
        public String nameWithoutExtension()
        {
            int separatorIndex = entryName.lastIndexOf('/');
            String onlyName = (separatorIndex < 0) ? entryName : entryName.substring(separatorIndex + 1, entryName.length());

            int dotIndex = onlyName.lastIndexOf('.');
            if (dotIndex == -1) return onlyName;
            return onlyName.substring(0, dotIndex);
        }
    }

    @Override
    protected void loadHeader() throws ValidationException
    {
        if (zip == null)
        {
            return;
        }

        super.loadHeader();
    }

    @Override
    public InputStream readStreamEntry(String name)
    {
        ZipEntry entryName = zip.getEntry(name);

        if (entryName == null)
        {
            throw new RuntimeException("Failed to find entry " + name + " in package " + getName());
        }

        try
        {
            return zip.getInputStream(entryName);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PackageFileHandle getFile(String fileName)
    {
        return new ZipPackageFileHandle(fileName);
    }

    @Override
    public boolean hasFile(String fileName)
    {
        return zip.getEntry("content/" + fileName) != null;
    }

    public String packageFilename()
    {
        return packageFilename(getName());
    }

    public static String packageFilename(String name)
    {
        return "packages/" + name + ".zip";
    }

    public static File packageFile(String name)
    {
        return BrainOut.Env.getFile(BrainOut.Env.getExternalPath(packageFilename(name)));
    }

    public String getPackagePath()
    {
        return BrainOut.Env.getExternalPath(packageFilename());
    }

    @Override
    public long calculateHash()
    {
        return calculateHash(getName());
    }

    public static long calculateHash(String name)
    {
        InputStream packageStream;

        try
        {
            File packageFile = packageFile(name);
            packageStream = new BufferedInputStream(new FileInputStream(packageFile));
        }
        catch (Exception e)
        {
            return -1;
        }

        CRC32 crc = new CRC32();

        crc.reset();

        try
        {
            byte buff[] = new byte[65535];

            int read;
            while ((read = packageStream.read(buff)) != -1)
            {
                crc.update(buff, 0, read);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();

            return -1;
        }

        return crc.getValue();
    }
}
