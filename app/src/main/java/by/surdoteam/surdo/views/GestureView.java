package by.surdoteam.surdo.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.PlaybackParams;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.VideoView;

import by.surdoteam.surdo.R;

public class GestureView extends VideoView {
    private final SharedPreferences sharedPref;
    //    No, it cannot be local variable
    private final SharedPreferences.OnSharedPreferenceChangeListener listener;
    private float speed;
    private PlaybackParams myPlayBackParams;

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
            if (key.equals("mvv_gestures_speed")) {
                updatePlaybackParams();
            }
        };
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
        this.setOnPreparedListener(
                mp -> {
                    //works only from api 23
                    mp.setPlaybackParams(myPlayBackParams);
                }
        );
    }

    private void updatePlaybackParams() {
        speed = Float.parseFloat(sharedPref.getString("mvv_gestures_speed", getResources().getString(R.string.gestures_speed_default_value)));
        myPlayBackParams = new PlaybackParams();
        myPlayBackParams.setSpeed(speed); //you can set speed here
    }
}
