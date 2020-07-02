package com.codepath.apps.restclienttemplate.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.codepath.apps.restclienttemplate.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetDao;
import com.codepath.apps.restclienttemplate.models.TweetWithUser;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

import static android.provider.CalendarContract.CalendarCache.URI;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    private static final int COMPOSE_REQUEST_CODE = 20;
    private static final int DETAILS_REQUEST_CODE = 30;
    private static final int REPLY_REQUEST_CODE = 40;

    TweetDao tweetDao;
    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter tweetsAdapter;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use ViewBinding
        ActivityTimelineBinding binding = ActivityTimelineBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        progressBar = binding.progressBar;

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // remove title in toolbar

        client = TwitterApp.getRestClient(this);
        tweetDao = ((TwitterApp) getApplicationContext()).getMyDatabase().tweetDao();

        swipeContainer = binding.swipeContainer;
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "fetching new data");
                populateHomeTimeline();
            }
        });

        // Find the recycler view
        rvTweets = binding.rvTweets;
        // Init the list of tweets and adapter
        tweets = new ArrayList<>();
        tweetsAdapter = new TweetsAdapter(this, tweets);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Recycler view setup: layout manager and the adapter
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(tweetsAdapter);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "onLoadMore: " + page);
                loadMoreData();
            }
        };
        // Adds the scroll listener to recyclerview
        rvTweets.addOnScrollListener(scrollListener);

        loadDataFromDB();
        populateHomeTimeline();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.compose) {
            // Navigate to the compose activity
            Intent intent = new Intent(this, ComposeActivity.class);
            startActivityForResult(intent, COMPOSE_REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == COMPOSE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get data from the intent (tweet)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // Update the RV with the tweet object
            // Modify data source of tweets
            tweets.add(0, tweet);
            // Update the adapter
            tweetsAdapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }
        else if(requestCode == DETAILS_REQUEST_CODE && resultCode == RESULT_OK) {
            int pos = data.getIntExtra("position", 0);
            tweets.get(pos).favorited = data.getBooleanExtra("favorited", false);
            tweets.get(pos).retweeted = data.getBooleanExtra("retweeted", false);
            Log.i("testingonly", "1 " + tweets.get(pos).favorited + " " + tweets.get(pos).retweeted);
            tweets.get(pos).retweetCount = data.getIntExtra("retweetCount", 0);
            tweets.get(pos).favCount = data.getIntExtra("favCount", 0);
            tweetsAdapter.notifyItemChanged(data.getIntExtra("position", 0));
        }
        else if(requestCode == REPLY_REQUEST_CODE && resultCode == RESULT_OK) {
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            tweets.add(0, tweet);
            tweetsAdapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void loadMoreData() {
        // Send an API request to retrieve appropriate paginated data
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess for loadMoreData! " + json.toString());
                //  --> Deserialize and construct new model objects from the API response
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    tweetsAdapter.addAll(tweets);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "onFailure for loadMoreData", throwable);
            }
        }, tweets.get(tweets.size() -1).id);
    }

    private void loadDataFromDB() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
            Log.i(TAG, "Showing data from database");
            List<TweetWithUser> tweetWithUsers = tweetDao.recentItems();
            List<Tweet> tweetsFromDB = TweetWithUser.getTweetList(tweetWithUsers);
            tweetsAdapter.clear();
            tweetsAdapter.addAll(tweetsFromDB);
            }
        });
    }

    private void populateHomeTimeline() {
        showProgressBar();
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess! " + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    final List<Tweet> tweetFromNetwork = Tweet.fromJsonArray(jsonArray);
                    hideProgressBar();
                    tweetsAdapter.clear();
                    tweetsAdapter.addAll(tweetFromNetwork);
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Saving data into database");
                            // Insert users first
                            List<User> usersFromNetwork = User.fromJsonTweetArray(tweetFromNetwork);
                            tweetDao.insertModel(usersFromNetwork.toArray(new User[0]));
                            // insert tweets next
                            tweetDao.insertModel(tweetFromNetwork.toArray(new Tweet[0]));
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                hideProgressBar();
                Log.i(TAG, "onFailure! " + response, throwable);
            }
        });
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}