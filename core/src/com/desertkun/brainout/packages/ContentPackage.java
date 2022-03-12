package com.desertkun.brainout.packages;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.files.FileHandleStream;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;


import java.io.*;
import java.util.Comparator;

public abstract class ContentPackage implements Json.Serializable
{
    private boolean contentLoaded, headerLoaded;
    private String name;
    private Array<String> dependencies;
    protected long crc32;
    private String version;

    protected JsonValue contentValue;

    public boolean validate()
    {
        return true;
    }

    public static class ValidationException extends Exception
    {

    }

    public abstract class PackageFileHandle extends FileHandleStream
    {
        public PackageFileHandle(String path)
        {
            super(path);
        }

        public abstract FileHandle handle();
    }

    public interface ContentBoundAssetManager
    {
        AssetManager get(Content c);
    }

    public void completeLoad(ContentBoundAssetManager bound)
    {
        for (ObjectMap.Entry<String, Content> entry: BrainOut.ContentMgr.getItems())
        {
            Content c = entry.value;

            if (c.getPackage() == this)
            {
                AssetManager b = bound.get(c);

                if (b == null)
                    continue;

                c.completeLoad(b);
            }
        }
    }

    public ContentPackage(String name) throws ValidationException
    {
        this.name = name;
        this.headerLoaded = false;
        this.contentLoaded = false;
        this.version = "0";
        this.dependencies = new Array<>();
        this.crc32 = calculateHash();

        loadHeader();
    }

    public abstract InputStream readStreamEntry(String name);
    public abstract PackageFileHandle getFile(String fileName);
    public abstract boolean hasFile(String fileName);
    public abstract long calculateHash();
    public abstract String getPackagePath();

    public JsonValue readJsonEntry(String name)
    {
        InputStream entryStream = readStreamEntry(name);

        if (entryStream == null)
        {
            return null;
        }

        return new JsonReader().parse(entryStream);
    }

    protected void loadHeader() throws ValidationException
    {
        JsonValue header = readJsonEntry("index.json");

        if (header == null)
        {
            return;
        }

        if (!validate())
            throw new ValidationException();

        read(BrainOut.R.JSON, header);

        headerLoaded = true;
    }

    public void loadContent(ContentBoundAssetManager bound)
    {
        if (contentValue == null)
            return;

        if (contentValue.has("content"))
        {
            final Array<Content> addedContent = new Array<Content>();

            Json json = new Json();

            BrainOut.R.tag(json);

            BrainOut.ContentMgr.loadContent(json, contentValue.get("content"), content ->
            {
                content.setPackage(ContentPackage.this);
                addedContent.add(content);
            });

            sortContent(addedContent);

            for (Content cnt: addedContent)
            {
                cnt.loadContent(bound.get(cnt));
            }
        }
    }

    protected int getContentRank(Content c)
    {
        return 0;
    }

    private void sortContent(Array<Content> addedContent)
    {
        addedContent.sort(Comparator.comparingInt(this::getContentRank));
    }

    public void loadContentHeader()
    {
        contentValue = loadItem("content.json");
    }

    public void releaseContentHeader()
    {
        contentValue = null;
    }

    public static JsonReader READER = new JsonReader();

    private JsonValue convertObject(JsonValue entry)
    {
        if (entry.isObject())
        {
            if (entry.has("@if"))
            {
                String ifCondition = entry.getString("@if");
                String section = BrainOut.PackageMgr.matchIfdef(ifCondition) ? "@then" : "@else";
                return entry.get(section);
            }

            if (entry.has("@switch"))
            {
                String switchCondition = entry.getString("@switch");
                String section = BrainOut.PackageMgr.resolve(switchCondition);
                JsonValue cases = entry.get("@cases");
                JsonValue v = null;
                if (cases != null)
                {
                    v = cases.get(section);
                }

                if (v == null)
                {
                    v = entry.get("@default");
                }

                return v;
            }
        }

        return entry;
    }

    private void remove(JsonValue parent, JsonValue child)
    {
        if (child.prev == null)
        {
            parent.child = child.next;
            if (parent.child != null)
            {
                parent.child.prev = null;
            }
        }
        else
        {
            child.prev.next = child.next;
            if (child.next != null) {
                child.next.prev = child.prev;
            }
        }

        --parent.size;
    }

    private void replace(JsonValue parent, JsonValue old, JsonValue new_)
    {
        if (old.prev == null)
        {
            parent.child = new_;
            new_.next = old.next;
            new_.prev = null;
        }
        else
        {
            old.prev.next = new_;
            new_.next = old.next;
            new_.prev = old.prev;
            if (old.next != null)
            {
                old.next.prev = new_;
            }
        }

        new_.parent = parent;

        old.setType(JsonValue.ValueType.nullValue);

        old.next = null;
        old.prev = null;
        old.parent = null;
        old.name = null;
    }

    private void postProcess(JsonValue object)
    {
        if (object.isObject())
        {
            for (JsonValue entry : object)
            {
                String key = entry.name();
                JsonValue converted = convertObject(entry);

                if (converted != entry)
                {
                    object.remove(key);

                    if (converted != null)
                    {
                        converted.next = null;
                        converted.prev = null;

                        object.addChild(key, converted);
                        entry = converted;
                    }
                }

                postProcess(entry);
            }
        }

        if (object.isArray())
        {
            JsonValue entry = object.child();

            while (entry != null)
            {
                JsonValue next = entry.next();
                JsonValue converted = convertObject(entry);

                if (converted == null)
                {
                    remove(object, entry);
                }
                else if (converted != entry)
                {
                    converted.next = null;
                    converted.prev = null;

                    replace(object, entry, converted);
                    entry = converted;
                }

                postProcess(entry);

                entry = next;
            }
        }
    }

    protected JsonValue loadItem(String path)
    {
        InputStream entryStream;

        try
        {
            entryStream = readStreamEntry(path);
        }
        catch (RuntimeException ignored)
        {
            return null;
        }

        if (entryStream != null)
        {
            try
            {
                JsonValue value = READER.parse(new InputStreamReader(entryStream));
                postProcess(value);
                return value;
            }
            catch (SerializationException e)
            {
                // ignored
            }
        }

        return null;
    }

    public boolean isHeaderLoaded()
    {
        return headerLoaded;
    }

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return version;
    }

    public Array<String> getDependencies()
    {
        return dependencies;
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("name", name);
        json.writeValue("version", version);
        json.writeValue("dependency", dependencies, Array.class, String.class);
        json.writeValue("crc32", crc32);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("name"))
        {
            name = jsonData.getString("name");
        }

        if (jsonData.has("version"))
        {
            version = jsonData.getString("version");
        }

        if (jsonData.has("dependency"))
        {
            dependencies = json.readValue(Array.class, String.class, jsonData.get("dependency"));
        }
    }

    public String toString()
    {
        return "package<" + name + ">";
    }

    public boolean isContentLoaded()
    {
        return contentLoaded;
    }

    public void setContentLoaded(boolean contentLoaded)
    {
        this.contentLoaded = contentLoaded;
    }

    public long getCRC32()
    {
        return crc32;
    }
}
