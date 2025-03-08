package com.example.boxy;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class VideoPlayerActivity extends AppCompatActivity {

    // Fallback video ID in case none is passed in via Intent.
    private static final String DEFAULT_VIDEO_ID = "dQw4w9WgXcQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // Set up the Toolbar with an up/back button.
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // Enable the up button in the toolbar.
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        // Retrieve the video ID from the intent extras, using a fallback if necessary.
        String videoId = getIntent().getStringExtra("videoId");
        if (videoId == null || videoId.isEmpty()) {
            videoId = DEFAULT_VIDEO_ID;
        }

        // Initialize the YouTubePlayerView and add it as a lifecycle observer.
        YouTubePlayerView youTubePlayerView = findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(youTubePlayerView);

        final String finalVideoId = videoId;
        // Add a listener to cue the video once the player is ready.
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                youTubePlayer.cueVideo(finalVideoId, 0);
            }
        });
    }

    // Handle the toolbar's up button press.
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
