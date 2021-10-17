package by.surdoteam.surdo.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.StorageService;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import by.surdoteam.surdo.MainActivity;
import by.surdoteam.surdo.R;
import by.surdoteam.surdo.db.AppDatabase;
import by.surdoteam.surdo.db.Command;


public class RecognizeFragment extends Fragment implements RecognitionListener {
    static private final int STATE_START = 0;
    static private final int STATE_READY = 1;
    static private final int STATE_DONE = 2;
    static private final int STATE_LISTENING = 3;
    static private final int STATE_NO_PERMISSION = 4;
    static private final int STATE_CRASH = 5;
    static private final String RECOGNIZE_UNK = "[unk]";
    static private final int NO_TIMEOUT = -1;

    private Pattern splitter;
    private int permissionCheck;
    private List<String> arguments;
    private List<Integer> video;
    private TextView textViewCommand;
    private FloatingActionButton recognizeStart;
    private SharedPreferences sharedPref;
    //    listener must not be converted to local variable as registerOnSharedPreferenceChangeListener
    //    doesn't create strong link, so local var will be garbage collected
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private boolean settingsChanged;

    private VideoView videoViewFragmentRecognize;

    private SpeechService speechService;
    private int listening_timeout;

    public RecognizeFragment() {
        //        cause the preference manager does not currently store a strong reference to the listener
        listener = (sharedPreferences, key) -> {
//            Toast.makeText(requireActivity().getApplicationContext(), R.string.restart_app, Toast.LENGTH_LONG).show();
            if (key.startsWith("rec_")) {
                settingsChanged = true;
            } else if (key.equals("lst_listening_timeout_default_value")) {
                getListeningTimeout();
            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        settingsChanged = false;
        getListeningTimeout();
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
        // get permissions
        permissionCheck = ContextCompat.checkSelfPermission(requireActivity().getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        AppDatabase database = ((MainActivity) requireActivity()).getDatabase();

        arguments = new LinkedList<>();
        video = new LinkedList<>();
        for (Command command : database.CommandDao().getAll()) {
            for (String word : command.getWord().split("\\|")) {
                arguments.add(word);
                video.add(command.getPath());
            }
        }
        startSetup();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recognize, container, false);
        videoViewFragmentRecognize = view.findViewById(R.id.videoViewFragmentRecognize);

        recognizeStart = view.findViewById(R.id.recognizeStartButton);
        recognizeStart.setScaleType(ImageView.ScaleType.FIT_CENTER);
        textViewCommand = view.findViewById(R.id.textViewCommand);

//        We already started setup in onCreate, but can call switchSearch only here, when UI was created
        if (speechService != null) {
//            We don't know what will be called first: onCreateView or callback in startSetup
            switchSearch(STATE_READY);
        } else if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            switchSearch(STATE_NO_PERMISSION);
        } else {
            switchSearch(STATE_START);
        }
        return view;
    }

    private void startSetup() {
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        StorageService.unpack(this.getContext(), "vosk-model-small-ru-0.15", "model",
                (model) -> {
                    if (isAdded()) {
                        setupRecognizer(model);
                        if (recognizeStart != null) {
                            switchSearch(STATE_READY);
                        }
                    }
                },
                (exception) -> setErrorState("Failed to unpack the model: " + exception.getLocalizedMessage()));
    }

    private void setupRecognizer(Model m) {
        StringBuilder parserPattern = new StringBuilder("((?<=^| )(?:(?:");
        StringBuilder grammarJSON = new StringBuilder("[");
        for (int i = 0; i < arguments.size(); i++) {
            parserPattern.append(arguments.get(i));
            grammarJSON.append("\"").append(arguments.get(i)).append("\", ");
            if (i < arguments.size() - 1) {
                parserPattern.append(")|(?:");
            }
        }
        parserPattern.append("))(?=$| ))+");
        grammarJSON.append("\"").append(RECOGNIZE_UNK).append("\"]");
        splitter = Pattern.compile(parserPattern.toString());

        try {
            Recognizer rec = new Recognizer(m, 16000.0f, grammarJSON.toString());
            speechService = new SpeechService(rec, 16000.0f);
        } catch (IOException e) {
            setErrorState(e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechService != null) {
            speechService.cancel(); // no callbacks will be called
            speechService.shutdown();
            speechService = null;
        }
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (speechService != null) {
            speechService.stop();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if (speechService != null) {
            if (settingsChanged) {
                settingsChanged = false;
                speechService.stop();
                speechService.shutdown();
                speechService = null;
                switchSearch(STATE_START);
                startSetup();
            } else {
                switchSearch(STATE_READY);
            }
        }
    }

    // UI and recognizer
    private void switchSearch(int state) {
        switch (state) {
            case STATE_LISTENING:
                recognizeStart.setOnClickListener(view1 -> switchSearch(STATE_DONE));
                recognizeStart.setEnabled(true);
                recognizeStart.setActivated(true);
                speechService.startListening(this, listening_timeout);
                break;
            case STATE_DONE:
                speechService.stop();
//                Yes, no break
            case STATE_READY:
                recognizeStart.setOnClickListener(view1 -> switchSearch(STATE_LISTENING));
                recognizeStart.setEnabled(true);
                recognizeStart.setActivated(false);
                break;
            case STATE_START:
                recognizeStart.setEnabled(false);
                recognizeStart.setActivated(false);
                break;
            case STATE_NO_PERMISSION:
                ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        result -> {
                            if (result) {
                                permissionCheck = PackageManager.PERMISSION_GRANTED;
                                recognizeStart.setEnabled(false);
                                recognizeStart.setImageResource(R.drawable.microphone);
                                startSetup();
                            }
                        });
                recognizeStart.setOnClickListener(view1 -> mPermissionResult.launch(Manifest.permission.RECORD_AUDIO));
                recognizeStart.setEnabled(true);
                recognizeStart.setImageResource(R.drawable.microphone_disabled);
                break;
            case STATE_CRASH:
                recognizeStart.setEnabled(false);
                recognizeStart.setImageResource(R.drawable.microphone_disabled);
                if (speechService != null) {
                    speechService.stop();
                    speechService.shutdown();
                }
                break;
        }
    }

    @Override
    public void onError(Exception error) {
        setErrorState(error.getLocalizedMessage());
    }

    private void setErrorState(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), Objects.requireNonNull(message), Toast.LENGTH_LONG).show();
            switchSearch(STATE_CRASH);
        }
    }

    @Override
    public void onTimeout() {
        switchSearch(STATE_READY);
    }

    @Override
    public void onFinalResult(String hypothesis) {
        onResult(hypothesis);
        switchSearch(STATE_READY);
    }

    @Override
    public void onPartialResult(String hypothesis) {
// Just nothing
    }


    @Override
    public void onResult(String res) {
        String hypothesis = getHypothesis(res);
        if (!hypothesis.isEmpty()) {
            if (hypothesis.contains(RECOGNIZE_UNK))
                hypothesis = hypothesis.replace(RECOGNIZE_UNK, getString(R.string.unable_to_recognize));
            textViewCommand.setText(hypothesis);
            Matcher matcher = splitter.matcher(hypothesis);
            while (matcher.find()) {
                String s = hypothesis.substring(matcher.start(), matcher.end());
                if (arguments.contains(s)) { // necessary until not all phrases in car.gram implemented
                    videoViewFragmentRecognize.setVideoPath("android.resource://" + requireActivity().getPackageName() + "/" + video.get(arguments.indexOf(s)));
                }
            }
        }
    }

    private String getHypothesis(String res) {
        if (res == null)
            return "";
        try {
            return new JSONObject(res).optString("text");
        } catch (JSONException e) {
            return "";
        }
    }

    private void getListeningTimeout() {
        int t = sharedPref.getInt("lst_listening_timeout_default_value", getResources().getInteger(R.integer.listening_timeout_default_value));
        listening_timeout = t == 0 ? NO_TIMEOUT : t * 1000;
    }
}