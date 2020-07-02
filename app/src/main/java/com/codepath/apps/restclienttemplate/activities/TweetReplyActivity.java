package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.target.Target;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.databinding.ActivityComposeBinding;
import com.codepath.apps.restclienttemplate.databinding.ActivityTweetReplyBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import okhttp3.Headers;

public class TweetReplyActivity extends AppCompatActivity {

    public static final String TAG = "TweetReplyActivity";

    EditText etCompose;
    Button btnTweet;
    TextView tvComposeSize;
    Button btnCLose;
    TextView tvScreenName;
    TextView tvName;
    ImageView ivUserImage;
    TextView tvReplyTo;
    long replyToId;

    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTweetReplyBinding binding = ActivityTweetReplyBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        client= TwitterApp.getRestClient(this);

        etCompose = binding.etCompose;
        btnTweet = binding.btnTweet;
        tvComposeSize = binding.tvComposeSize;
        btnCLose = binding.btnClose;
        tvScreenName = binding.tvScreenName;
        tvName = binding.tvName;
        ivUserImage = binding.ivUserImage;
        tvReplyTo = binding.tvReplyTo;

        replyToId = getIntent().getLongExtra("reply_to_id", 0);

        getCurrentUser();
        getReplyToUser(replyToId);

        btnCLose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TweetReplyActivity.this, TimelineActivity.class);
                startActivity(intent);
            }
        });

        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                client.postReply(replyToId, etCompose.getText().toString(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            setResult(RESULT_OK, intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure reply + " + response, throwable);
                    }
                });
            }
        });
    }

    private void getReplyToUser(long id) {
        client.getUser(id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess getUser " + json.toString());
                JSONObject jsonObject = json.jsonObject;
                try {
                    User user = User.fromJson(jsonObject);
                    etCompose.setText("@" + user.screenName);
                    tvReplyTo.setText("In reply to " + user.screenName);
                } catch (JSONException e) {
                    Log.e(TAG, "getUser hit json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure getUser " + response, throwable);
            }
        });
    }

    private void getCurrentUser() {
        client.getCurrentUser(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess getCurrentUser " + json.toString());
                JSONObject jsonObject = json.jsonObject;
                try {
                    User user = User.fromJson(jsonObject);
                    tvScreenName.setText("@" + user.screenName);
                    tvName.setText(user.name);

                    Glide.with(TweetReplyActivity.this)
                            .load(user.publicImageUrl)
                            .transform(new RoundedCorners(30))
                            .override(Target.SIZE_ORIGINAL)
                            .into(ivUserImage);
                } catch (JSONException e) {
                    Log.e(TAG, "getCurrentUser hit json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure getCurrentUser " + response, throwable);
            }
        });
    }
}