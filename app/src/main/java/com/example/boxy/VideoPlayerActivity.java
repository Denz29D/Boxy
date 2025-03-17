package com.example.boxy;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

/*
 * Activity that plays a YouTube video using the Android YouTube Player library.
 * The app retrieves a video ID from the intent extras (or uses a default ID) and cues the video.
 * A toolbar with an up button is set up to allow navigation back.
 */
public class VideoPlayerActivity extends AppCompatActivity {

    // Fallback video ID in case none is provided via Intent.
    private static final String DEFAULT_VIDEO_ID = "dQw4w9WgXcQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the activity layout.
        setContentView(R.layout.activity_video_player);

        // Set up the Toolbar and enable the up button.
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        // Retrieve the video ID from the intent extras; use a fallback if none is provided.
        String videoId = getIntent().getStringExtra("videoId");
        if (videoId == null || videoId.isEmpty()) {
            videoId = DEFAULT_VIDEO_ID;
        }

        // Initialize the YouTubePlayerView and add it as a lifecycle observer.
        YouTubePlayerView youTubePlayerView = findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(youTubePlayerView);

        // Once the player is ready, cue the video with the specified video ID.
        final String finalVideoId = videoId;
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                // Cue the video at the beginning (0 seconds).
                youTubePlayer.cueVideo(finalVideoId, 0);
            }
        });
    }

    // Handle the toolbar's up button press by finishing the activity.
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
