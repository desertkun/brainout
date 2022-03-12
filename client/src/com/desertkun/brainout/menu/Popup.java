package com.desertkun.brainout.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.*;
import com.desertkun.brainout.menu.ui.BorderActor;

public class Popup extends Menu
{
    public static class PopupButtonStyle
    {
        private ClickListener listener;
        private String style;

        public PopupButtonStyle(ClickListener listener, String style)
        {
            this.listener = listener;
            this.style = style;
        }

        public PopupButtonStyle(ClickListener listener)
        {
            this(listener, "button-small");
        }
    }

    private ArrayMap<String, PopupButtonStyle> buttons;
    protected final String text;

    public Popup(String text)
    {
        this.text = text;
    }

    public void setButtons(ArrayMap<String, PopupButtonStyle> buttons)
    {
        this.buttons = buttons;
    }

    public String getTitle()
    {
        return null;
    }

    public boolean bg()
    {
        return false;
    }
    @Override
    protected TextureRegion getBackground()
    {
        if (!bg())
            return null;

        return BrainOutClient.getRegion("bg-loading");
    }

    protected String getTitleBackgroundStyle()
    {
        return "form-gray";
    }

    protected String getTitleLabelStyle()
    {
        return "title-level";
    }

    protected String getContentBackgroundStyle()
    {
        return "form-default";
    }

    @Override
    public Table createUI()
    {
        Table root = new Table();

        String title = getTitle();

        if (title != null)
        {
            Label titleLabel = new Label(title, BrainOutClient.Skin, "title-level");
            root.add(new BorderActor(titleLabel, getTitleBackgroundStyle())).expandX().fillX().row();
        }

        Table data = new Table();
        data.setSkin(BrainOutClient.Skin);
        data.setBackground(getContentBackgroundStyle());

        Table content = new Table();
        initContent(content);

        data.add(content).pad(16).expand().fill();

        Table buttons = new Table();

        for (ObjectMap.Entry<String, PopupButtonStyle> entry: this.buttons.entries())
        {
            TextButton btn = new TextButton(entry.key, BrainOutClient.Skin, entry.value.style);
            btn.addListener(entry.value.listener);

            renderButton(buttons, btn);
        }

        buttons.row();

        root.add(data).width(getContentWidth()).expandX().fillX().row();
        root.add(buttons).center().expandX().fillX().row();

        return root;
    }

    protected void renderButton(Table buttons, TextButton btn)
    {
        buttons.add(btn).height(getButtonHeight()).expandX().fillX().uniform();
    }

    protected float getButtonHeight()
    {
        return 32;
    }

    protected float getContentWidth()
    {
        return 544;
    }

    @Override
    public void onFocusIn()
    {
        super.onFocusIn();

        if (ClientConstants.Client.MOUSE_LOCK)
        {
            Gdx.input.setCursorCatched(false);
        }
    }

    protected float getFade()
    {
        return Constants.Menu.MENU_BACKGROUND_FADE;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(getFade(), getBatch());

        super.render();
    }

    protected void initContent(Table data)
    {
        final Label text = new Label(this.text, BrainOutClient.Skin, "title-medium");
        text.setAlignment(Align.center);
        text.setWrap(true);

        data.add(text).pad(16).center().expand().fill().row();
    }

    @Override
    public boolean lockUpdate()
    {
        return true;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
