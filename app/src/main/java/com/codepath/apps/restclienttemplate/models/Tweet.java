package com.codepath.apps.restclienttemplate.models;

import android.text.format.DateUtils;
import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Parcel
@Entity(foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "userId"))
public class Tweet {

    @ColumnInfo
    @PrimaryKey
    public long id;

    @ColumnInfo
    public String body;

    @ColumnInfo
    public String fullBody;

    @ColumnInfo
    public String createdAt;

    @ColumnInfo
    public long userId;

    @ColumnInfo
    public int retweetCount;

    @ColumnInfo
    public int favCount;

    @ColumnInfo
    public String tweetImageUrl;

    @ColumnInfo
    public Boolean favorited;

    @ColumnInfo
    public Boolean retweeted;

    @Ignore
    public User user;

    // Empty constructor needed by Parceler library
    public Tweet() {}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.id = jsonObject.getLong("id");
        if (jsonObject.has("text"))
            tweet.body = jsonObject.getString("text");
        if (jsonObject.has("full_text"))
            tweet.fullBody = jsonObject.getString("full_text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.retweetCount = jsonObject.getInt("retweet_count");
        tweet.favCount = jsonObject.getInt("favorite_count");
        tweet.favorited = jsonObject.getBoolean("favorited");
        tweet.retweeted = jsonObject.getBoolean("retweeted");

        User user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.user = user;
        tweet.userId = user.id;
        if (jsonObject.has("extended_entities")) {
            JSONArray mediaArray = jsonObject.getJSONObject("extended_entities").getJSONArray("media");
            tweet.tweetImageUrl = mediaArray.getJSONObject(0).getString("media_url_https");
        }
        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i=0; i<jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }
}
