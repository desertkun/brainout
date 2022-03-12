package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.common.msg.client.PromoCodeMsg;
import com.desertkun.brainout.common.msg.server.PromoCodeResultMsg;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.PromoCodeResultEvent;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.YesNoInputPopup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PromoCodeMenu extends YesNoInputPopup
{
    private static Pattern PROMO_PATTERN = Pattern.compile("([A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}).*");

    private boolean onTop;

    private class PromoResultPopup extends AlertPopup
    {
        public PromoResultPopup(String text)
        {
            super(text);
        }

        @Override
        public boolean popIfFocusOut()
        {
            return true;
        }

        @Override
        public String getTitle()
        {
            return L.get("MENU_PROMO_TITLE");
        }
    }

    private class PromoCodeResultMenu extends WaitLoadingMenu implements EventReceiver
    {
        public PromoCodeResultMenu(String loadingTitle)
        {
            super(loadingTitle, false);
        }

        @Override
        public boolean popIfFocusOut()
        {
            return true;
        }

        @Override
        public boolean lockUpdate()
        {
            return true;
        }

        @Override
        public boolean onEvent(Event event)
        {
            switch (event.getID())
            {
                case promoCodeResult:
                {
                    promoResult(((PromoCodeResultEvent) event).result);

                    break;
                }
            }

            return false;
        }

        private void promoResult(PromoCodeResultMsg.Result result)
        {
            pop();

            switch (result)
            {
                case success:
                {
                    pushMenu(new PromoResultPopup(L.get("MENU_PROMO_SUCCESS")));

                    break;
                }
                case codeIsNotValid:
                {
                    pushMenu(new PromoResultPopup(L.get("MENU_PROMO_NOT_VALID")));

                    break;
                }
                case error:
                {
                    pushMenu(new PromoResultPopup(L.get("MENU_PROMO_ERROR")));

                    break;
                }
            }
        }

        @Override
        public void onInit()
        {
            super.onInit();

            BrainOutClient.EventMgr.subscribe(Event.ID.promoCodeResult, this);
        }

        @Override
        public void onRelease()
        {
            super.onRelease();

            BrainOutClient.EventMgr.unsubscribe(Event.ID.promoCodeResult, this);
        }
    }

    public PromoCodeMenu()
    {
        super(L.get("MENU_ENTER_PROMO"), detectPromo());

        onTop = true;
    }

    @Override
    protected TextField newEdit(String value)
    {
        return new TextField(value, BrainOutClient.Skin, "edit-focused");
    }

    @Override
    protected void initContent(Table data)
    {
        super.initContent(data);

        valueEdit.setAlignment(Align.center);
    }

    @Override
    public String getTitle()
    {
        return L.get("MENU_PROMO_TITLE");
    }

    @Override
    protected String getYesButtonText()
    {
        return L.get("MENU_PROMO_ACTIVATE");
    }

    @Override
    protected String getNoButtonText()
    {
        return L.get("MENU_CANCEL");
    }

    @Override
    public boolean stayOnTop()
    {
        return onTop;
    }

    private static String detectPromo()
    {
        String clip = Gdx.app.getClipboard().getContents();

        if (clip != null)
        {
            Matcher matcher = PROMO_PATTERN.matcher(clip);
            if (matcher.matches())
            {
                return matcher.group(1);
            }
        }

        return "";
    }

    private void valid(String promoCode)
    {
        BrainOutClient.ClientController.sendTCP(new PromoCodeMsg(promoCode));
    }

    @Override
    public void pop()
    {
        onTop = false;

        super.pop();
    }

    @Override
    public void ok()
    {
        String value = getValue();

        if (value == null || value.equals(""))
            return;

        Matcher matcher = PROMO_PATTERN.matcher(value);
        if (matcher.matches())
        {
            pushMenu(new PromoCodeResultMenu(""));
            valid(matcher.group(1));
        }
        else
        {
            pushMenu(new PromoResultPopup(L.get("MENU_PROMO_NOT_VALID")));
        }
    }

    @Override
    protected float getInputWidth()
    {
        return 240;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }
}
