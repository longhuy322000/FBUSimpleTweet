package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.databinding.ActivityComposeBinding;
import com.codepath.apps.restclienttemplate.databinding.ActivityTweetDetailsBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import okhttp3.Headers;

public class TweetDetailsActivity extends AppCompatActivity {

    public static final String TAG = "TweetDetailsActivity";

    Tweet tweet;
    TwitterClient client;

    ImageView ivProfileImage;
    TextView tvBody;
    TextView tvScreenName;
    TextView tvName;
    TextView tvTimeElapsed;
    ImageView ivTweetImage;
    TextView tvRetweetCount;
    TextView rvFavCount;
    Button btnReply;
    Button btnRetweet;
    Button btnFav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use ViewBinding
        ActivityTweetDetailsBinding binding = ActivityTweetDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ivProfileImage = binding.ivProfileImage;
        tvBody = binding.tvBody;
        tvName = binding.tvName;
        tvScreenName = binding.tvScreenName;
        tvTimeElapsed = binding.tvTimeElapsed;
        ivTweetImage = binding.ivTweetImage;
        tvRetweetCount = binding.tvRetweetCount;
        rvFavCount = binding.tvFavCount;
        btnReply = binding.btnReply;
        btnRetweet = binding.btnRetweet;
        btnFav = binding.btnFav;

        long tweetId = getIntent().getLongExtra("tweetId", 0);
        client = TwitterApp.getRestClient(this);

        // get Tweet for full_text body
        getTweet(tweetId);
        Log.i(TAG, "tweetId " + tweetId);

        btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tweet.favorited) {
                    client.createFavorite(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess create fav");
                            tweet.favorited = true;
                            tweet.favCount += 1;
                            rvFavCount.setText(String.valueOf(tweet.favCount));
                            setFavorited();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure create fav", throwable);
                        }
                    });
                }
                else {
                    client.destroyFavorite(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess destroy fav");
                            tweet.favorited = false;
                            tweet.favCount -= 1;
                            rvFavCount.setText(String.valueOf(tweet.favCount));
                            setUnFavorited();
                            btnFav.setBackgroundResource(R.drawable.ic_vector_heart_stroke);
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure destroy fav", throwable);
                        }
                    });
                }
            }
        });

        btnRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tweet.retweeted) {
                    client.postRetweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess retweeted");
                            tweet.retweeted = true;
                            tweet.retweetCount += 1;
                            tvRetweetCount.setText(String.valueOf(tweet.retweetCount));
                            setRetweeted();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure retweeted", throwable);
                        }
                    });
                }
                else {
                    client.postUnRetweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            tweet.retweeted = false;
                            tweet.retweetCount -= 1;
                            tvRetweetCount.setText(String.valueOf(tweet.retweetCount));
                            Log.i(TAG, "onSuccess unRetweeted");
                            setUnRetweeted();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure unRetweeted", throwable);
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            intent.putExtra("position", getIntent().getIntExtra("position", 0));
            intent.putExtra("favorited", tweet.favorited);
            intent.putExtra("retweeted", tweet.retweeted);
            intent.putExtra("favCount", tweet.favCount);
            intent.putExtra("retweetCount", tweet.retweetCount);
            Log.i("testingonly", tweet.favorited + " " + tweet.retweeted);
            setResult(RESULT_OK, intent);
            // Close the activity, pass data to parent
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getTweet(long id) {
        client.getTweet(id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    JSONObject jsonObject = json.jsonObject;
                    tweet = Tweet.fromJson(jsonObject);
                    bind();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure loading tweet", throwable);
            }
        });
    }

    private void bind() {
        tvName.setText(tweet.user.name);
        tvBody.setText(tweet.fullBody);
        tvScreenName.setText("@" + tweet.user.screenName);
        tvTimeElapsed.setText(Tweet.getRelativeTimeAgo(tweet.createdAt));
        tvRetweetCount.setText(String.valueOf(tweet.retweetCount));
        rvFavCount.setText(String.valueOf(tweet.favCount));
        if (tweet.favorited != null && tweet.favorited == true) {
            setFavorited();
        }
        if (tweet.retweeted != null && tweet.retweeted == true) {
            setRetweeted();
        }

        Glide.with(this)
                .load(tweet.user.publicImageUrl)
                .override(Target.SIZE_ORIGINAL)
                .into(ivProfileImage);

        Glide.with(this)
                .load(tweet.tweetImageUrl)
                .override(Target.SIZE_ORIGINAL)
                .into(ivTweetImage);
    }

    public void setFavorited() {
        btnFav.setBackgroundResource(R.drawable.ic_vector_heart);
        btnFav.getBackground().setTint(Color.parseColor("#F44336"));
    }

    public void setUnFavorited() {
        btnFav.setBackgroundResource(R.drawable.ic_vector_heart_stroke);
    }

    public void setRetweeted() {
        btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet);
        btnRetweet.getBackground().setTint(Color.parseColor("#6a9d51"));
    }

    public void setUnRetweeted() {
        btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet_stroke);
    }
}