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
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

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
        View view = inflater.inflate(R.layout.activity_home_acivity, container, false);

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
            // ✅ Manual Fragment Transaction to GymLocatorFragment
            GymLocatorFragment gymLocatorFragment = new GymLocatorFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, gymLocatorFragment)
                    .addToBackStack(null) // Allows the user to navigate back
                    .commit();
        });

        LinearLayout videoContainer = view.findViewById(R.id.video_container);

        FirebaseFirestore.getInstance().collection("videos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Video video = doc.toObject(Video.class);
                        if (video == null) continue;

                        View videoCard = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_video_card, videoContainer, false);

                        ImageView thumbnail = videoCard.findViewById(R.id.iv_video_thumbnail);
                        TextView title = videoCard.findViewById(R.id.tv_video_title);
                        Glide.with(getContext()).load(video.getThumbnailUrl()).into(thumbnail);
                        title.setText(video.getTitle());

                        videoCard.setOnClickListener(v -> {
                            Intent intent = new Intent(getContext(), VideoPlayerActivity.class);
                            intent.putExtra("videoId", video.getVideoId());
                            startActivity(intent);
                        });

                        videoContainer.addView(videoCard);
                    }
                });

        // Fix for the Push-up Counter Navigation
        Button btnPushUpCounter = view.findViewById(R.id.btn_pushup_counter);
        btnPushUpCounter.setOnClickListener(v -> {
            //  Manual Fragment Transaction to PushUpCounterFragment
            PushUpCounterFragment pushUpCounterFragment = new PushUpCounterFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, pushUpCounterFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
