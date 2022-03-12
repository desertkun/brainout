package com.desertkun.brainout.menu.tutorial;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;

public class AnchorTutorialMenu extends TutorialMenu
{
    private final Table anchor;
    private final TextureRegion image;
    private final String text;
    private final int textX;
    private final int textY;
    private final int textWidth;
    private final int textHeight;
    private final Runnable closed;
    private final int offsetX;
    private final int offsetY;

    private static Vector2 tmp = new Vector2();

    public AnchorTutorialMenu(Table anchor, String image, int offsetX, int offsetY,
                              String text, int textX, int textY, int textWidth, int textHeight,
                              Runnable closed)
    {
        super(closed);

        this.anchor = anchor;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.image = BrainOutClient.getRegion(image);

        this.text = text;
        this.textX = textX;
        this.textY = textY;
        this.textWidth = textWidth;
        this.textHeight = textHeight;

        this.closed = closed;
    }

    @Override
    public void onInit()
    {
        super.onInit();

        tmp.set(0, 0);
        anchor.localToStageCoordinates(tmp);

        if (this.image != null)
        {
            Image image = new Image(this.image);
            image.setSize(this.image.getRegionWidth(), this.image.getRegionHeight());
            image.setPosition(tmp.x + offsetX, tmp.y + offsetY);

            addActor(image);
        }

        Label text = new Label(this.text, BrainOutClient.Skin, "title-small");
        text.setBounds(tmp.x + textX + offsetX, tmp.y + textY + offsetY, textWidth, textHeight);
        text.setWrap(true);
        text.setAlignment(Align.center, Align.center);

        addActor(text);

    }
}
