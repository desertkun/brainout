package com.desertkun.brainout.menu.popups;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ArrayMap;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class YesNoInputPopup extends InputPopup
{
    public YesNoInputPopup(String text, String value)
    {
        super(text, value);

        init();
    }

    protected String getYesButtonText()
    {
        return L.get("MENU_OK");
    }

    protected String getNoButtonText()
    {
        return L.get("MENU_CANCEL");
    }

    private void init()
    {
        ArrayMap<String, PopupButtonStyle> buttons = new ArrayMap<>();

        buttons.put(getNoButtonText(), new PopupButtonStyle(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                playSound(MenuSound.back);

                pop();
            }
        }));

        buttons.put(getYesButtonText(), new PopupButtonStyle(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                playSound(MenuSound.select);

                pop();
                ok();
            }
        }));

        setButtons(buttons);
    }

    public void ok()
    {

    }

}