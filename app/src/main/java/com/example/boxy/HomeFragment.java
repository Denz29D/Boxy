package com.example.boxy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.boxy.models.Video;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment (consider renaming to fragment_home.xml)
        View view = inflater.inflate(R.layout.activity_home_acivity, container, false);

        // Set up edge-to-edge insets for the view with id "main"
        View mainView = view.findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
        CardView cardGymLocator = view.findViewById(R.id.card_gym_locator);
        cardGymLocator.setOnClickListener(v -> {
            // Navigate to the gym locator fragment
            Navigation.findNavController(view)
                    .navigate(R.id.action_nav_home_to_gymLocatorFragment);
        });

        // Get a reference to the LinearLayout container inside the HorizontalScrollView
        LinearLayout videoContainer = view.findViewById(R.id.video_container);

        // Fetch videos from Firestore
        FirebaseFirestore.getInstance().collection("videos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Video video = doc.toObject(Video.class);
                        if (video == null) continue;

                        // Inflate a video card for each video
                        View videoCard = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_video_card, videoContainer, false);

                        // Bind data to the video card
                        ImageView thumbnail = videoCard.findViewById(R.id.iv_video_thumbnail);
                        TextView title = videoCard.findViewById(R.id.tv_video_title);
                        Glide.with(getContext()).load(video.getThumbnailUrl()).into(thumbnail);
                        title.setText(video.getTitle());

                        // Set an onClickListener to launch the VideoPlayerActivity with the videoId
                        videoCard.setOnClickListener(v -> {
                            Intent intent = new Intent(getContext(), VideoPlayerActivity.class);
                            intent.putExtra("videoId", video.getVideoId());
                            startActivity(intent);
                        });


                        Button btnPushUpCounter = view.findViewById(R.id.btn_pushup_counter);
                        btnPushUpCounter.setOnClickListener(v -> {
                            // Navigate to the PushUpCounterFragment using the navigation action defined in the nav graph.
                            Navigation.findNavController(view)
                                    .navigate(R.id.action_nav_home_to_pushUpCounterFragment);
                        });
                        // Add the video card to the container
                        videoContainer.addView(videoCard);
                    }
                });

        return view;
    }
}
