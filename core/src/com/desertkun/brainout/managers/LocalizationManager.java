package com.desertkun.brainout.managers;

import java.util.regex.MatchResult;

import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.L;
import com.desertkun.brainout.utils.StringFunctions;
import com.esotericsoftware.minlog.Log;


public class LocalizationManager implements Json.Serializable
{
	public Array<String> languages = new Array<String>();
	public String currentLanguage;
    public ObjectMap<String, ObjectMap<String, String>> data;
    private boolean inited = false;

    public static String GetDefaultLanguage()
    {
        return BrainOut.Env.getDefaultLanguage();
    }

	public String get(String id)
	{
	    if (!GetDefaultLanguage().equals(currentLanguage))
        {
            try
            {
                ObjectMap<String, String> d = data.get(id);
                return d.get(currentLanguage, d.get(GetDefaultLanguage()));
            }
            catch (Exception e)
            {
                return id;
            }
        }

		try
		{
            return data.get(id).get(currentLanguage);
		} 
		catch (Exception e)
		{
			return id;
		}
	}

	public boolean has(String id)
    {
        return data.containsKey(id);
    }

    public String getForLanguage(String id, String language)
    {
        try
        {
            return data.get(id).get(language);
        }
        catch (Exception e)
        {
            return id;
        }
    }

    public void init() {}

    public String get(String id, String format)
    {
        String l = get(id);

        if (l == null)
            return "???";

        return l.replace("%s", format);
    }

    public String get(String id, String[] format)
    {
        String data = get(id);

        int i = 1;

        for (String s : format)
        {
            data = data.replace("{" + i + "}", s);
            i++;
        }

        return data;
    }
	
	public String getCurrentLanguage()
	{
		return currentLanguage;
	}
	
	public boolean setCurrentLanguage(String lang, String def)
	{
        String newLanguage;

        if (languages.contains(lang, false))
        {
            newLanguage = lang;
        }
        else
        {
            newLanguage = def;
        }

        boolean differentGroup = getFontGroup(currentLanguage) != getFontGroup(newLanguage);
        currentLanguage = newLanguage;
        BrainOut.PackageMgr.setDefine("language", currentLanguage);
        return differentGroup;
	}

    public String parseLanguage(String language, String fallback)
    {
        if (languages.contains(language, false))
        {
            return language;
        }

        return fallback;
    }
	
	public LocalizationManager()
    {
        data = new ObjectMap<>();

		currentLanguage = GetDefaultLanguage();
	}

    public void clear()
    {
        data.clear();
    }

    @Override
    public void write(Json json)
    {
        //
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Json json, JsonValue jsonData)
    {
        if (Log.INFO) Log.info("Registering localizations...");

        JsonValue dataValue = jsonData.get("data");

        if (dataValue.isObject())
        {
            data.ensureCapacity(dataValue.size);

            for (JsonValue item : dataValue)
            {
                if (item.isObject())
                {
                    ObjectMap<String, String> record = new ObjectMap<String, String>(item.size);

                    for (JsonValue langRecord: item)
                    {
                        String lang = langRecord.name;

                        String translation;

                        if (langRecord.isString())
                        {
                            translation = langRecord.asString();
                        }
                        else
                        {
                            translation = langRecord.getString("one", "???");
                        }

                        if (!languages.contains(lang, false))
                        {
                            languages.add(lang);
                        }

                        record.put(lang, translation.replace("\\n", "\n"));
                    }

                    data.put(item.name, record);
                }
            }
        }

    }

    public String parse(String data)
    {
        return StringFunctions.replaceWithFunction(data, "\\{([A-Za-z0-9_]+)\\,?([^\\}]+)?\\}", new StringFunctions.ReplaceFunction()
        {
            @Override
            public String replace(MatchResult from)
            {
                if (from.group(2) != null)
                {
                    return get(from.group(1), from.group(2));
                }

                return get(from.group(1));
            }
        });
    }

    public void update()
    {
        if (!inited)
        {
            init();

            inited = true;
        }
    }

    public int getFontGroup(String localization)
    {
        if (localization == null)
            return 1;

        switch (localization)
        {
            case "ZH":
            case "ZH_CN":
                return 2;
            case "KO":
                return 3;
            default:
                return 1;
        }
    }
}
