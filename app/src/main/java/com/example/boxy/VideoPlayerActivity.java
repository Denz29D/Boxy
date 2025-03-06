package com.example.boxy;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
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

        // Retrieve the video ID from the intent extras
        String videoId = getIntent().getStringExtra("videoId");
        if (videoId == null || videoId.isEmpty()) {
            videoId = DEFAULT_VIDEO_ID;
        }

        // Find the YouTubePlayerView in the layout
        YouTubePlayerView youTubePlayerView = findViewById(R.id.youtube_player_view);
        // Ensure the YouTubePlayerView observes the activity's lifecycle
        getLifecycle().addObserver(youTubePlayerView);

        // Add a listener to initialize and play the video when the player is ready
        String finalVideoId = videoId;
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                // Cue the video; if you prefer autoplay, use loadVideo(videoId, 0)
                youTubePlayer.cueVideo(finalVideoId, 0);
            }
        });
    }
}
