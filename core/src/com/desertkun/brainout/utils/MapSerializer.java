package com.desertkun.brainout.utils;

import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.Map;
import com.esotericsoftware.minlog.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class MapSerializer
{
    public static class MapHeader implements Json.Serializable
    {
        private String version;
        private int bodySize;
        private ObjectMap<String, Extension> extensions;

        public static class Extension
        {
            private boolean map;
            private int dataStart;
            private int length;
        }

        public MapHeader()
        {
            extensions = new ObjectMap<>();
        }

        @Override
        public void write(Json json)
        {
            json.writeValue("v", version);
            json.writeValue("sz", bodySize);

            if (extensions.size > 0)
            {
                json.writeArrayStart("e");

                for (ObjectMap.Entry<String, Extension> entry : extensions)
                {
                    Extension extension = entry.value;

                    json.writeObjectStart();

                    json.writeValue("n", entry.key);
                    json.writeValue("s", extension.dataStart);
                    json.writeValue("l", extension.length);
                    if (extension.map)
                    {
                        json.writeValue("m", true);
                    }

                    json.writeObjectEnd();
                }

                json.writeArrayEnd();
            }
        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            this.version = jsonData.getString("v");
            this.bodySize = jsonData.getInt("sz");

            if (jsonData.has("e"))
            {
                for (JsonValue ex : jsonData.get("e"))
                {
                    String name = ex.getString("n");
                    int dataStart = ex.getInt("s");
                    int length = ex.getInt("l");

                    Extension extension = new Extension();
                    extension.dataStart = dataStart;
                    extension.length = length;
                    extension.map = ex.getBoolean("m", false);

                    extensions.put(name, extension);
                }
            }
        }

        public void setBodySize(int bodySize)
        {
            this.bodySize = bodySize;
        }

        public int getBodySize()
        {
            return bodySize;
        }

        public ObjectMap<String, Extension> getExtensions()
        {
            return extensions;
        }

        public void addExtension(String name, int dataStart, int length)
        {
            addExtension(name, dataStart, length, false);
        }

        public void addExtension(String name, int dataStart, int length, boolean map)
        {
            Extension ex = new Extension();
            ex.dataStart = dataStart;
            ex.length = length;
            ex.map = map;

            extensions.put(name, ex);
        }

        public void setVersion(String version)
        {
            this.version = version;
        }

        public String getVersion()
        {
            return version;
        }
    }

    private static byte[] convert(InputStream is) throws IOException
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    public static <T extends Map> Array<T> LoadMaps(InputStream is, Class<T> classType, String key)
    {
        return LoadMaps(is, classType, key, "default");
    }

    public static <T extends Map> Array<T> LoadMaps(InputStream is, Class<T> classType, String key, String defaultMapName)
    {
        try
        {
            byte[] verification = null;

            if (key != null)
            {
                verification = new byte[Constants.Maps.MAP_SIGNATURE_SIZE];

                if (is.read(verification) == -1)
                {
                    return null;
                }
            }

            Json json = new Json();

            byte[] buf = convert(is);
            MapHeader header = null;

            String possibleHeader = new String(buf, 0, Constants.Maps.MAGIC.length());

            int decompressOffset, decompressLength;

            JsonValue headerValue = null;

            if (possibleHeader.equals(Constants.Maps.MAGIC))
            {
                int headerLength = ByteBuffer.wrap(buf, Constants.Maps.MAGIC.length(), 4).getInt();

                byte[] rawHeader = Compressor.Decompress(buf,
                    Constants.Maps.MAGIC.length() + 4, headerLength);

                if (rawHeader == null)
                    return null;

                headerValue = new JsonReader().parse(new ByteArrayInputStream(rawHeader));

                header = new MapHeader();
                header.read(json, headerValue);

                decompressOffset = Constants.Maps.MAGIC.length() + 4 + headerLength;
                decompressLength = header.getBodySize();
            }
            else
            {
                decompressOffset = 0;
                decompressLength = buf.length;
            }

            byte[] data;

            try
            {
                data = Compressor.Decompress(buf, decompressOffset, decompressLength);
            }
            catch (Exception e)
            {
                e.printStackTrace();

                Log.error("Failed to decompress map. buf_size = " + buf.length + ", " +
                        "dc_offset = " + decompressOffset + ", dc_length = " + decompressLength);

                if (headerValue != null)
                {
                    Log.error("Header: " + headerValue.toJson(JsonWriter.OutputType.json));
                }

                return null;
            }

            if (data == null)
                return null;

            if (key != null)
            {
                if (!Arrays.equals(verification, HashUtils.Verify(key, data)))
                {
                    return null;
                }
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(data);

            BrainOut.R.tag(json);

            JsonValue value = new JsonReader().parse(bis);

            Array<T> maps = new Array<>();
            T defaultMap = null;

            if (value.isArray())
            {
                T firstMap = null;

                for (JsonValue mapValue : value)
                {
                    String dimension = mapValue.getString("dimension", defaultMapName);
                    T map = classType.getConstructor(String.class).newInstance(dimension);
                    map.read(json, mapValue);
                    maps.add(map);

                    if (firstMap == null)
                    {
                        firstMap = map;
                    }

                    if (dimension.equals(defaultMapName))
                    {
                        defaultMap = map;
                    }
                }

                if (defaultMap == null)
                {
                    defaultMap = firstMap;
                }
            }
            else
            {
                String dimension = value.getString("dimension", defaultMapName);
                T map = classType.getConstructor(String.class).newInstance(dimension);
                map.read(json, value);
                maps.add(map);
                defaultMap = map;
            }

            if (header != null && defaultMap != null)
            {
                int generalOffset = decompressOffset + decompressLength;

                PoolJsonReader reader = new PoolJsonReader();

                for (ObjectMap.Entry<String, MapHeader.Extension> entry : header.getExtensions())
                {
                    MapHeader.Extension extension = entry.value;

                    if (extension.map)
                    {
                        byte[] de;

                        try
                        {
                            de = Compressor.Decompress(buf, generalOffset + extension.dataStart, extension.length);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();

                            Log.error("Failed to decompress map. buf_size = " + buf.length + ", " +
                                    "dc_offset = " + decompressOffset + ", dc_length = " + decompressLength);

                            continue;
                        }

                        if (de == null)
                            continue;

                        JsonValue exv;

                        {
                            ByteArrayInputStream exbis = new ByteArrayInputStream(de);
                            exv = reader.parse(exbis);
                        }

                        String dimension = exv.getString("dimension", defaultMapName);
                        
                        /*
                        FileOutputStream f = new FileOutputStream(dimension + "_dim.json");
                        f.write(de);
                        f.close();
                        */

                        T map = classType.getConstructor(String.class).newInstance(dimension);
                        map.read(json, exv);
                        maps.add(map);

                        reader.free();
                    }
                    else
                    {
                        byte[] slice = Arrays.copyOfRange(buf, generalOffset + extension.dataStart,
                                generalOffset + extension.dataStart + extension.length);
                        defaultMap.addExtension(entry.key, slice);
                    }
                }

                defaultMap.updateExtensions();
            }

            return maps;
        }
        catch (Exception e)
        {
            e.printStackTrace();

            ExceptionHandler.reportCrash(e, "maperror", null);

            return null;
        }
    }
}
