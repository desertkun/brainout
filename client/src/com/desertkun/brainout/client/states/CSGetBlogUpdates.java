package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOutClient;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.BlogService;
import org.anthillplatform.runtime.services.LoginService;

public class CSGetBlogUpdates extends ControllerState
{
    @Override
    public ID getID()
    {
        return ID.getBlogUpdates;
    }

    @Override
    public void init()
    {
        LoginService loginService = LoginService.Get();
        BlogService blogService = BlogService.Get();

        if (loginService != null && blogService != null)
        {
            blogService.getBlogEntries(loginService.getCurrentAccessToken(), "main",
                (service, request, result, blogEntries) -> Gdx.app.postRunnable(() ->
            {
                BrainOutClient.ClientController.setBlogEntries(blogEntries);
                proceed();
            }));
        }
        else
        {
            proceed();
        }
    }

    private void proceed()
    {
        switchTo(new CSFindLobby());
    }

    @Override
    public void release()
    {

    }
}
