package by.surdoteam.surdo.views;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;

import java.util.LinkedList;
import java.util.Queue;

public class MultipleVideoView extends GestureView {
    private final Queue<Uri> videosQueue;
    private boolean finished;

    public MultipleVideoView(Context context) {
        this(context, null);
    }

    public MultipleVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultipleVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MultipleVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        finished = true;
        videosQueue = new LinkedList<>();
        setOnCompletionListener(mp -> startNext());
    }

    private void startNext() {
        Uri video = videosQueue.poll();
        if (video != null) {
            finished = false;
            this.setVideoURI(video);
            this.requestFocus(0);
            this.start();
        } else {
            finished = true;
        }
    }

    public void addVideo(Uri video) {
        videosQueue.add(video);
        if (finished) {
            startNext();
        }
    }

    public void reset() {
        stopPlayback();
        videosQueue.clear();
        finished = true;
    }
}