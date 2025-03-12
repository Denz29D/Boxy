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
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.boxy.models.Video;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {

    private TextView tvUserName;

    public HomeFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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

        // Reference UI elements
        tvUserName = view.findViewById(R.id.tv_user_name);
        CardView cardGymLocator = view.findViewById(R.id.card_gym_locator);
        CardView cardLeaderboard = view.findViewById(R.id.card_leaderboard);
        Button btnPushUpCounter = view.findViewById(R.id.btn_pushup_counter);
        LinearLayout videoContainer = view.findViewById(R.id.video_container);

        // 1) Load and display the currently signed-in user's name
        loadUserName();

        // 2) Gym Locator navigation
        cardGymLocator.setOnClickListener(v -> {
            GymLocatorFragment gymLocatorFragment = new GymLocatorFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, gymLocatorFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // 3) Leaderboards navigation
        cardLeaderboard.setOnClickListener(v -> {
            LeaderboardsFragment leaderboardsFragment = new LeaderboardsFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, leaderboardsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // 4) Push-Up Counter navigation
        btnPushUpCounter.setOnClickListener(v -> {
            PushUpCounterFragment pushUpCounterFragment = new PushUpCounterFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, pushUpCounterFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // 5) Load tutorial videos from Firestore
        FirebaseFirestore.getInstance().collection("videos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Video video = doc.toObject(Video.class);
                        if (video == null) continue;

                        View videoCard = LayoutInflater.from(requireContext())
                                .inflate(R.layout.item_video_card, videoContainer, false);

                        ImageView thumbnail = videoCard.findViewById(R.id.iv_video_thumbnail);
                        TextView title = videoCard.findViewById(R.id.tv_video_title);

                        Glide.with(requireContext()).load(video.getThumbnailUrl()).into(thumbnail);
                        title.setText(video.getTitle());

                        videoCard.setOnClickListener(v -> {
                            Intent intent = new Intent(requireContext(), VideoPlayerActivity.class);
                            intent.putExtra("videoId", video.getVideoId());
                            startActivity(intent);
                        });

                        videoContainer.addView(videoCard);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load videos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        return view;
    }

    /**
     * Loads the currently signed-in user's name from Firestore
     * and sets it in the tvUserName TextView.
     */
    private void loadUserName() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            tvUserName.setText("Guest");
            return;
        }
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        if (fullName != null && !fullName.isEmpty()) {
                            tvUserName.setText(fullName);
                        } else {
                            tvUserName.setText("No Name Found");
                        }
                    } else {
                        tvUserName.setText("No Document Found");
                    }
                })
                .addOnFailureListener(e -> {
                    tvUserName.setText("Error Loading Name");
                });
    }
}
