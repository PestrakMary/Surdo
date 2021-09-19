package by.surdoteam.surdo.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import androidx.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.VideoView;

import androidx.core.content.ContextCompat;

import by.surdoteam.surdo.R;

public class GestureView extends VideoView {
    private static final String KEY = "mvv_gestures_speed";
    private final int bgColor = ContextCompat.getColor(getContext(), R.color.colorGestureBackground);;
    private final SharedPreferences sharedPref;
    private float speed;
    private final MediaPlayer.OnInfoListener backgroundDisabler = (mp, what, extra) -> {

        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(speed));
            // video started; hide the placeholder.
            setBackgroundColor(Color.TRANSPARENT);
            return true;
        }
        return false;
    };
    //    No, it cannot be local variable
    private final SharedPreferences.OnSharedPreferenceChangeListener listener;

    public GestureView(Context context) {
        this(context, null);
    }

    public GestureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GestureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        updatePlaybackParams();
        listener = (sharedPreferences, key) -> {
            if (key.equals(KEY)) {
                updatePlaybackParams();
            }
        };
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
        setOnPreparedListener(
                mp -> {
                    // For cases when app was stopped and resumed again
                    mp.setOnInfoListener(backgroundDisabler);
                }
        );
        setBackgroundColor(bgColor);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == GONE) {
//        Return from the background with proper color, and no flashes between videos in queue
            setBackgroundColor(bgColor);
        }
    }

    private void updatePlaybackParams() {
        speed = Float.parseFloat(sharedPref.getString(KEY, getResources().getString(R.string.gestures_speed_default_value))); //you can set speed here
    }
}
