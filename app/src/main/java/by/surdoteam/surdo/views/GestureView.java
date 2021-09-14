package by.surdoteam.surdo.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import by.surdoteam.surdo.R;

public class GestureView extends VideoView {
    private static final String KEY = "mvv_gestures_speed";
    private final SharedPreferences sharedPref;
    private final MediaPlayer.OnInfoListener backgroundDisabler = (mp, what, extra) -> {
        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            // video started; hide the placeholder.
            setBackgroundColor(Color.TRANSPARENT);
            return true;
        }
        return false;
    };
    //    No, it cannot be local variable
    private final SharedPreferences.OnSharedPreferenceChangeListener listener;
    private final PlaybackParams gvPlayBackParams;

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
        gvPlayBackParams = new PlaybackParams();
        updatePlaybackParams();
        listener = (sharedPreferences, key) -> {
            if (key.equals(KEY)) {
                updatePlaybackParams();
            }
        };

        sharedPref.registerOnSharedPreferenceChangeListener(listener);
        this.setOnPreparedListener(
                mp -> {
                    //works only from api 23
                    mp.setPlaybackParams(gvPlayBackParams);
                    mp.setOnInfoListener(backgroundDisabler);
                }
        );
        setBackgroundColor(0xFF454545);
    }

    private void updatePlaybackParams() {
        gvPlayBackParams.setSpeed(Float.parseFloat(sharedPref.getString(KEY, getResources().getString(R.string.gestures_speed_default_value)))); //you can set speed here
    }
}
