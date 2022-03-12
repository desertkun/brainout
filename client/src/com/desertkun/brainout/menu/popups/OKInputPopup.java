package com.desertkun.brainout.menu.popups;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ArrayMap;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class OKInputPopup extends InputPopup
{
    public OKInputPopup(String text, String value)
    {
        super(text, value);

        init();
    }

    private void init()
    {
        ArrayMap<String, PopupButtonStyle> buttons = new ArrayMap<>();

        buttons.put(L.get("MENU_OK"), new PopupButtonStyle(new ClickOverListener()
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