package com.codepath.apps.restclienttemplate.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.target.Target;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.activities.TweetDetailsActivity;
import com.codepath.apps.restclienttemplate.activities.TweetReplyActivity;
import com.codepath.apps.restclienttemplate.models.Tweet;

import java.text.ParseException;
import java.util.List;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    public static final String TAG = "TweetsAdapter";

    Context context;
    List<Tweet> tweets;

    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        Tweet tweet = tweets.get(position);
        // Bind the tweet with view holder
        try {
            holder.bind(tweet);
        } catch (ParseException e) {
            Log.e(TAG, "Parse date exception");
        }
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Clear all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Tweet> tweetList) {
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    // Define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private static final int DETAILS_REQUEST_CODE = 30;
        private static final int REPLY_REQUEST_CODE = 40;

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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvName = itemView.findViewById(R.id.tvName);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvTimeElapsed = itemView.findViewById(R.id.tvTimeElapsed);
            ivTweetImage = itemView.findViewById(R.id.ivTweetImage);
            tvRetweetCount = itemView.findViewById(R.id.tvRetweetCount);
            rvFavCount = itemView.findViewById(R.id.tvFavCount);
            btnReply = itemView.findViewById(R.id.btnReply);
            btnRetweet = itemView.findViewById(R.id.btnRetweet);
            btnFav = itemView.findViewById(R.id.btnFav);
            itemView.setOnClickListener(this);

            btnReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, TweetReplyActivity.class);
                    intent.putExtra("reply_to_id", tweets.get(getAdapterPosition()).userId);
                    ((Activity) context).startActivityForResult(intent, REPLY_REQUEST_CODE);
                }
            });
        }

        public void bind(Tweet tweet) throws ParseException {
            tvName.setText(tweet.user.name);
            tvBody.setText(tweet.body);
            tvScreenName.setText("@" + tweet.user.screenName);
            tvTimeElapsed.setText(Tweet.getRelativeTimeAgo(tweet.createdAt));
            tvRetweetCount.setText(String.valueOf(tweet.retweetCount));
            rvFavCount.setText(String.valueOf(tweet.favCount));
            if (tweet.favorited != null && tweet.favorited) {
                setFavorited();
            }
            else if (tweet.favorited != null && tweet.favorited == false) {
                setUnFavorited();
            }
            if (tweet.retweeted != null && tweet.retweeted == true) {
                setRetweeted();
            }
            else if (tweet.retweeted != null && tweet.retweeted == false) {
                setUnRetweeted();
            }

            Glide.with(context)
                    .load(tweet.user.publicImageUrl)
                    .override(Target.SIZE_ORIGINAL)
                    .into(ivProfileImage);

            Glide.with(context)
                    .load(tweet.tweetImageUrl)
                    .into(ivTweetImage);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, TweetDetailsActivity.class);
            intent.putExtra("tweetId", tweets.get(getAdapterPosition()).id);
            intent.putExtra("position", getAdapterPosition());
            ((Activity) context).startActivityForResult(intent, DETAILS_REQUEST_CODE);
        }

        public void setFavorited() {
            btnFav.setBackgroundResource(R.drawable.ic_vector_heart);
            btnFav.getBackground().setTint(Color.parseColor("#F44336"));
        }

        public void setUnFavorited() {
            btnRetweet.setBackgroundResource(R.drawable.ic_vector_heart_stroke);
        }

        public void setRetweeted() {
            btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet);
            btnRetweet.getBackground().setTint(Color.parseColor("#6a9d51"));
        }

        public void setUnRetweeted() {
            btnRetweet.setBackgroundResource(R.drawable.ic_vector_retweet_stroke);
        }
    }
}
