package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpParametersUtils;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.popups.ConfirmationPopup;
import org.anthillplatform.runtime.services.EnvironmentService;
import org.anthillplatform.runtime.services.LoginService;

import java.util.HashMap;

public class ReportPlayerPopup extends ConfirmationPopup
{
    private final String accountID;
    private final String userName;
    private boolean onTop;

    private TextArea reportDetails;
    private SelectBox<String> reason;

    public ReportPlayerPopup(String accountID, String userName)
    {
        super();

        this.accountID = accountID;
        this.userName = userName;
        this.onTop = true;
    }

    @Override
    public String buttonYes()
    {
        return L.get("MENU_SEND");
    }

    @Override
    public String buttonStyleYes()
    {
        return "button-green";
    }

    @Override
    public String getTitle()
    {
        return L.get("MENU_REPORT_TITLE", userName);
    }

    @Override
    protected String getTitleBackgroundStyle()
    {
        return "form-red";
    }

    @Override
    protected String getContentBackgroundStyle()
    {
        return "form-border-red";
    }

    @Override
    public String buttonNo()
    {
        return L.get("MENU_CLOSE");
    }

    @Override
    protected void initContent(Table data)
    {
        {
            Label title = new Label(L.get("MENU_CHOOSE_CATEGORY"), BrainOutClient.Skin, "title-small");
            title.setAlignment(Align.center);
            data.add(title).expandX().center().pad(10).row();

            reason = new SelectBox<String>(BrainOutClient.Skin, "select-badged");

            reason.setItems("cheating", "abuse", "other");
            reason.setSelected("cheating");

            data.add(reason).expandX().fillX().row();
        }

        {
            Label title = new Label(L.get("MENU_REPORT_DETAILS"), BrainOutClient.Skin, "title-small");
            title.setAlignment(Align.center);
            data.add(title).expandX().center().pad(10).padTop(26).row();

            reportDetails = new TextArea("", BrainOutClient.Skin, "edit-default");
            reportDetails.setPrefRows(6);
            setKeyboardFocus(reportDetails);

            data.add(reportDetails).expandX().fillX().height(128).row();
        }
    }

    @Override
    protected float getButtonHeight()
    {
        return 64;
    }

    @Override
    protected boolean reverseOrder()
    {
        return true;
    }

    @Override
    public void yes()
    {
        onTop = false;

        send();
    }

    private void send()
    {
        LoginService loginService = LoginService.Get();

        LoginService.AccessToken token = loginService.getCurrentAccessToken();

        HashMap<String, String> args = new HashMap<>();

        args.put("access_token", token.get());
        args.put("report_account", accountID);
        args.put("report_reason", reason.getSelected());
        args.put("report_text", reportDetails.getText());
        args.put("avatar", BrainOutClient.ClientController.getUserProfile().getAvatar());
        args.put("username", BrainOutClient.ClientController.getUserProfile().getName());

        Object www = EnvironmentService.Get().getEnvironmentVariables().get("www");

        if (www == null)
            return;

        Net.HttpRequest request = new Net.HttpRequest("POST");
        request.setUrl(www.toString() + "/report");
        request.setContent(HttpParametersUtils.convertHttpParameters(args));

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener()
        {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse)
            {

            }

            @Override
            public void failed(Throwable t)
            {

            }

            @Override
            public void cancelled()
            {

            }
        });
    }

    @Override
    public void no()
    {
        onTop = false;
    }

    @Override
    protected float getFade()
    {
        return Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE;
    }

    @Override
    public boolean stayOnTop()
    {
        return onTop;
    }

}
