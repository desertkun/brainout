package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Version;
import com.desertkun.brainout.content.Authors;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.MenuHelper;
import com.desertkun.brainout.menu.ui.RichLabel;

public class AuthorsMenu extends Menu
{
    private ScrollPane pane;
    private int soundVolume;

    @Override
    public Table createUI()
    {
        Table data = new Table();

        RichLabel contents = new RichLabel(getContents(), BrainOutClient.Skin, "title-small");

        Table wrapper = new Table();

        int pad = BrainOutClient.getHeight() - 140;
        wrapper.add(contents).padBottom(pad + 64).padTop(pad);

        pane = new ScrollPane(wrapper, BrainOutClient.Skin, "scroll-default");

        data.add(pane).pad(64).expand().fill();

        return data;
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        if (pane.getScrollPercentY() > 0.98f )
        {
            pane.setScrollY(0);
            pane.updateVisualScroll();
        }
        else
        {
            pane.setScrollY(pane.getScrollY() + delta * 60.0f);
        }

    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    private String getContents()
    {
        Authors authors = BrainOutClient.ContentMgr.get("authors-list", Authors.class);
        ObjectMap<String, String[]> authorsList = authors.getAuthors();

        String result = "";

        result +=
                        "<img padBottom=\"16\">logo-small</img><br/>" +
                        "<div padBottom=\"32\">" +
                        "<text style=\"title-yellow\">Version: " + Version.VERSION + ", Tag: " + Version.TAG +"</text>" +
                        "</div><br/>" +
                        "<div padBottom=\"32\">" +
                        "<text style=\"title-yellow\">Graphics and design</text><br/>";

        for (String author : authorsList.get("graphicsAndDesign"))
        {
            result += "<text style=\"title-small\">" + author + "</text><br/>";
        }

        result +=
                        "</div><br/>" +

                        "<div padBottom=\"32\">" +
                        "<text style=\"title-yellow\">Game design</text><br/>";

        for (String author : authorsList.get("gameDesign"))
        {
            result += "<text style=\"title-small\">" + author + "</text><br/>";
        }

        result +=
                        "</div><br/>" +

                        "<div padBottom=\"32\">" +
                        "<img>icon-health</img><img>icon-health</img><img>icon-health</img><br/>" +
                        "<text style=\"title-yellow\">Special Thanks To</text><br/>";

        for (String author : authorsList.get("specialThanksTo"))
        {
            result += "<text style=\"title-small\">" + author + "</text><br/>";
        }

        result +=
                        "</div><br/>" +

                        "<div padBottom=\"32\">" +
                        "<img padBottom=\"16\">icon-gd</img><br/>" +
                        "<text style=\"title-yellow\">Ideological Inspirers</text><br/>";

        for (String author : authorsList.get("ideologicalInspirers"))
        {
            result += "<text style=\"title-small\">" + author + "</text><br/>";
        }

        result +=
                "</div><br/>" +

                        "<div padBottom=\"32\">" +
                        "<text style=\"title-yellow\">Music</text><br/>";

        for (String author : authorsList.get("music"))
        {
            result += "<text style=\"title-small\">" + author + "</text><br/>";
        }

        result +=
                "</div><br/>" +

                "<div padBottom=\"32\">" +
                "<img padBottom=\"16\">medal-purple-heart</img><br/>" +
                "<text style=\"title-yellow\">Beta Testers</text><br/>";

        for (String author : authorsList.get("betaTesters"))
        {
            result += "<text style=\"title-small\">" + author + "</text><br/>";
        }

        result +=
                "</div><br/>" +

                "<div padBottom=\"16\">" +
                "<img>medal-skin-maker</img><br/>" +
                "<text style=\"title-yellow\" padBottom=\"8\">Content Creators</text><br/>" +
                "<div padRight=\"8\">";

        String [] contentCreators = authorsList.get("contentCreators");

        for (int i = 0; i <= contentCreators.length / 2; i++)
        {
            result += "<text style=\"title-small\">" + contentCreators[i] + "</text><br/>";
        }

        result +=
                "</div>" +
                "<div padLeft=\"8\">";

        for (int i = contentCreators.length / 2 + 1; i < contentCreators.length; i++)
        {
            result += "<text style=\"title-small\">" + contentCreators[i] + "</text><br/>";
        }

        if (contentCreators.length % 2 != 0)
            result += "<text style=\"title-small\"></text><br/>";

        result +=
                "</div></div><br />" +

                "<div padBottom=\"32\">" +
                "<img padBottom=\"16\">localization-icon</img><br/>" +
                "<text style=\"title-yellow\">Localization</text><br/>";

        for (String author : authorsList.get("localization"))
        {
            result += "<text style=\"title-small\">" + author + "</text><br/>";
        }

        result +=
                "</div><br />" +

                "<div padBottom=\"32\">" +
                "<img padBottom=\"16\">medal-wiki</img><br/>" +
                "<text style=\"title-yellow\">Fandom editors</text><br/>";

        for (String author : authorsList.get("fandomEditors"))
        {
            result += "<text style=\"title-small\">" + author + "</text><br/>";
        }

        result +=
                "</div><br />" +

                "<div padBottom=\"32\">" +
                "<img padBottom=\"16\">medal-steam</img><br/>" +
                "<text style=\"title-yellow\">Level design</text><br/>";

        for (String author : authorsList.get("levelDesign"))
        {
            if (author.startsWith("map:"))
                result += "<text style=\"title-green\">\"" + author.replaceFirst("map:", "") + "\"</text><br/>";
            else
                result += "<text style=\"title-small\">" + author + "</text><br/>";
        }

        result +=
                "</div><br />" +

                "<div padBottom=\"32\">" +
                "<img>icon-events-duck-hunt</img><br/>" +
                "<text style=\"title-yellow\">Community Support</text><br/>";

        for (String author : authorsList.get("communitySupport"))
        {
            result += "<text style=\"title-small\">" + author + "</text><br/>";
        }

        result +=
                "</div><br />" +

                "<text style=\"title-yellow\" padBottom=\"16\">Created with</text><br/>" +
                "<img padBottom=\"16\">icon-libgdx</img><br/>" +
                "<img>icon-spine</img><br/>" +
                "<img padBottom=\"8\">icon-tornado</img>";

        return result;
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-ingame");
    }

    @Override
    public void onInit()
    {
        super.onInit();

        MenuHelper.AddCloseButton(this, this::pop);

        soundVolume = BrainOutClient.ClientSett.getSoundVolume().getValue();
        BrainOutClient.ClientSett.getSoundVolume().setValue(0);

        BrainOutClient.MusicMng.playMusic("music-authors", true);
    }

    @Override
    public void onRelease()
    {
        super.onRelease();


        BrainOutClient.ClientSett.getSoundVolume().setValue(soundVolume);

        BrainOutClient.MusicMng.playMusic("music-lobby", true);
    }
}
