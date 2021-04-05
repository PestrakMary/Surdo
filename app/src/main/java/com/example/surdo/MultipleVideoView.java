package com.example.surdo;

import android.net.Uri;
import android.widget.VideoView;

import java.util.LinkedList;
import java.util.Queue;

public class MultipleVideoView {
    private final VideoView videoView;
    private final Queue<Uri> videosQueue;
    private boolean finished;

    public MultipleVideoView(VideoView videoView) {
        this.videoView = videoView;
        finished = true;
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
            finished = false;
            videoView.setVideoURI(video);
            videoView.requestFocus(0);
            videoView.start();
        } else {
            finished = true;
        }
    }

    public void addVideo(Uri video) {
        synchronized (videosQueue) {
            videosQueue.add(video);
        }
        if (finished) {
            startNext();
        }
    }
}