package com.desertkun.brainout.menu.popups;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ArrayMap;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Popup;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class ConfirmationPopup extends Popup
{
    public ConfirmationPopup(String text)
    {
        super(text);
        init();
    }

    public ConfirmationPopup()
    {
        super("");
        init();
    }

    public String buttonYes()
    {
        return L.get("MENU_YES");
    }

    public String buttonNo()
    {
        return L.get("MENU_NO");
    }

    public String buttonStyleYes()
    {
        return "button-small";
    }

    public String buttonStyleNo()
    {
        return "button-small";
    }

    private void addYes(ArrayMap<String, PopupButtonStyle> buttons)
    {
        buttons.put(buttonYes(), new PopupButtonStyle(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                playSound(MenuSound.select);

                yes();
                pop();
            }
        }, buttonStyleYes()));
    }

    private void addNo(ArrayMap<String, PopupButtonStyle> buttons)
    {
        buttons.put(buttonNo(), new PopupButtonStyle(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                playSound(MenuSound.back);

                no();
                pop();
            }
        }, buttonStyleNo()));
    }

    protected boolean reverseOrder()
    {
        return false;
    }

    private void init()
    {
        ArrayMap<String, PopupButtonStyle> buttons = new ArrayMap<String, PopupButtonStyle>();

        if (reverseOrder())
        {
            addNo(buttons);
            addYes(buttons);
        }
        else
        {
            addYes(buttons);
            addNo(buttons);
        }

        setButtons(buttons);
    }

    public void yes() {}
    public void no() {}
}
