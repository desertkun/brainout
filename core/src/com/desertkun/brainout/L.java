package com.desertkun.brainout;

public class L
{
    public static String get(String id)
    {
        return BrainOut.LocalizationMgr.get(id);
    }

    public static boolean has(String id)
    {
        return BrainOut.LocalizationMgr.has(id);
    }

    public static String getForLanguage(String id, String lang)
    {
        return BrainOut.LocalizationMgr.getForLanguage(id, lang);
    }

    public static String get(String id, String format)
    {
        return BrainOut.LocalizationMgr.get(id, format);
    }

    public static String get(String id, String... format)
    {
        return BrainOut.LocalizationMgr.get(id, format);
    }
}
