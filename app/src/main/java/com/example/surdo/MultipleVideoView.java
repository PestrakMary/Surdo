package com.example.surdo;

import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;

import java.net.URI;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class MultipleVideoView {
    private final VideoView videoView;
    private final Queue<Uri> videosQueue;

    public MultipleVideoView(VideoView videoView) {
        this.videoView = videoView;
        videosQueue = new LinkedList<>();
        videoView.setOnCompletionListener(mp -> {
            startNext();
        });
    }

    private void startNext() {
        Uri video;
        synchronized (videosQueue) {
            video = videosQueue.poll();
        }
        if (video != null) {
            videoView.setVideoURI(video);
            videoView.requestFocus(0);
            videoView.start();
        }
    }

    public void addVideo(Uri video) {
        synchronized (videosQueue) {
            videosQueue.add(video);
        }
        if (!videoView.isPlaying()) {
            startNext();
        }
    }
}