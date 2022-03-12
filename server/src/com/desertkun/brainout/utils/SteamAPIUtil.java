package com.desertkun.brainout.utils;

import com.badlogic.gdx.utils.ObjectSet;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class SteamAPIUtil
{
    public static class WorkshopItemResult
    {
        private int subscribers;
        private ObjectSet<String> tags;
        private int timeUpdated;

        public WorkshopItemResult()
        {
            subscribers = 0;
            timeUpdated = 0;
            tags = new ObjectSet<>();
        }

        public int getSubscribers()
        {
            return subscribers;
        }

        private void setSubscribers(int subscribers)
        {
            this.subscribers = subscribers;
        }

        public ObjectSet<String> getTags()
        {
            return tags;
        }

        private void addTag(String tag)
        {
            tags.add(tag);
        }

        private void setTimeUpdated(int timeUpdated)
        {
            this.timeUpdated = timeUpdated;
        }

        public int getTimeUpdated()
        {
            return timeUpdated;
        }
    }

    public static InputStream DownloadWorkshopMap(String workshopItem, int timeUpdated)
    {
        try
        {
            return Unirest.get("https://workshop-gold.brainout.org/item/578310/" + workshopItem + "/map")
                    .queryString("time_updated", timeUpdated)
                    .asBinary().getBody();
        }
        catch (UnirestException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static WorkshopItemResult GetWorkshopItem(String workshopItem)
    {
        HttpResponse<JsonNode> response;

        try
        {
            response = Unirest.post("https://api.steampowered.com/ISteamRemoteStorage/GetPublishedFileDetails/v1/")
                .field("itemcount", "1").field("publishedfileids[0]", workshopItem).asJson();
        }
        catch (UnirestException e)
        {
            e.printStackTrace();
            return null;
        }

        JSONObject response_ = response.getBody().getObject().getJSONObject("response");
        JSONObject detail_ = response_.getJSONArray("publishedfiledetails").getJSONObject(0);

        WorkshopItemResult result = new WorkshopItemResult();
        result.setSubscribers(detail_.optInt("subscriptions", 0));
        result.setTimeUpdated(detail_.optInt("time_updated", 0));

        JSONArray tags_ = detail_.getJSONArray("tags");

        for (int i = 0, t = tags_.length(); i < t; i++)
        {
            JSONObject tag_ = tags_.getJSONObject(i);

            result.addTag(tag_.getString("tag"));
        }

        return result;
    }
}
