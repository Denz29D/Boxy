package com.example.boxy.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Workout {
    private String workoutId;
    private String title;
    private String description;
    private String duration;
    private String difficulty;
    private int calories;

    // Use imageUrl from Firestore
    private String imageUrl; // e.g., "agility.jpg"

    private String videoId;

    @ServerTimestamp
    private Date createdAt;

    public Workout() { }

    public Workout(String workoutId, String title, String description, String duration,
                   String difficulty, int calories, String imageUrl, String videoId) {
        this.workoutId = workoutId;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.difficulty = difficulty;
        this.calories = calories;
        this.imageUrl = imageUrl;
        this.videoId = videoId;
    }

    // Getters & setters
    public String getWorkoutId() { return workoutId; }
    public void setWorkoutId(String workoutId) { this.workoutId = workoutId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    // IMPORTANT: Make sure your Firestore doc field is also named "imageUrl"
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
