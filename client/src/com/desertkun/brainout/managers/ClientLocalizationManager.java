package com.desertkun.brainout.managers;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.settings.StringEnumProperty;
import com.desertkun.brainout.utils.SkinFromStream;

public class ClientLocalizationManager extends LocalizationManager
{
    private Array<FontReference> refs;
    private BitmapFont initFont;

    public interface FontReference
    {
        void update(BitmapFont font);
    }

    public ClientLocalizationManager()
    {
        refs = new Array<>();
    }

    @Override
    public void init()
    {
        super.init();

        StringEnumProperty langOption = BrainOutClient.ClientSett.getLanguage();
        String lang = langOption.getValue();

        if (lang == null)
        {
            lang = parseLanguage(System.getProperty("user.language").toUpperCase(), LocalizationManager.GetDefaultLanguage());

            setCurrentLanguage(lang, LocalizationManager.GetDefaultLanguage());
            BrainOutClient.ClientSett.getLanguage().setValue(lang);
        }
        else
        {
            setCurrentLanguage(lang, LocalizationManager.GetDefaultLanguage());
        }

        updateLanguages();
    }

    public void updateLanguages()
    {
        StringEnumProperty langOption = BrainOutClient.ClientSett.getLanguage();

        langOption.clear();

        for (String language : languages)
        {
            String text;

            if (language.equals(currentLanguage))
            {
                text = get("LANGUAGE_" + language);
            }
            else
            {
                text = getForLanguage("LANGUAGE_" + language, language) + " (" + get("LANGUAGE_" + language) + ")";
            }

            langOption.addOption(language, text);
        }
    }
}
