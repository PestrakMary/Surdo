package by.surdoteam.surdo.views;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Pair;

import androidx.annotation.MainThread;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class QueueGestureVideoView extends GestureView {
    private final Queue<Pair<Uri, Map<String, String>>> videosQueue;
    private boolean finished;

    public QueueGestureVideoView(Context context) {
        this(context, null);
    }

    public QueueGestureVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QueueGestureVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public QueueGestureVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        finished = true;
        videosQueue = new LinkedList<>();
        setOnCompletionListener(mp -> startNext());
    }

    @Override
    @MainThread
    public void setVideoURI(Uri uri, Map<String, String> headers) {
        videosQueue.add(Pair.create(uri, headers));
        if (finished) {
            startNext();
        }
    }

    @Override
    public void stopPlayback() {
        reset();
    }

    @MainThread
    private void reset() {
        super.stopPlayback();
        videosQueue.clear();
        finished = true;
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == GONE) {
            reset();
        }
    }

    @MainThread
    private void startNext() {
        Pair<Uri, Map<String, String>> video = videosQueue.poll();
        if (video != null) {
            finished = false;
            super.setVideoURI(video.first, video.second);
            requestFocus(0);
            start();
        } else {
            finished = true;
        }
    }
}