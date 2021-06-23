package by.surdoteam.surdo.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import by.surdoteam.surdo.MainActivity;
import by.surdoteam.surdo.MultipleVideoView;
import by.surdoteam.surdo.R;
import by.surdoteam.surdo.db.AppDatabase;
import by.surdoteam.surdo.db.Command;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class RecognizeFragment extends Fragment implements RecognitionListener {

    private static final String KWS_SEARCH = "wakeup";
    /* Keyword we are looking for to activate menu */
    private String keyphrase;
    /* Named searches allow to quickly reconfigure the decoder */
    private static final String PHRASE_SEARCH = "phrase";
    private SpeechRecognizer recognizer;
    private Pattern splitter;
    private int permissionCheck;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private List<String> arguments;
    private List<Integer> video;
    private TextView textViewCommand;
    private FloatingActionButton recognizeStart;
    private SharedPreferences sharedPref;
    //    listener must not be converted to local variable as registerOnSharedPreferenceChangeListener
    //    doesn't create strong link, so local var will be garbage collected
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private boolean settingsChanged;

    private MultipleVideoView videoViewFragmentRecognize;

    public RecognizeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recognize, container, false);
        videoViewFragmentRecognize = new MultipleVideoView(view.findViewById(R.id.videoViewFragmentRecognize));

        recognizeStart = view.findViewById(R.id.recognizeStartButton);
        recognizeStart.setScaleType(ImageView.ScaleType.FIT_CENTER);
        textViewCommand = view.findViewById(R.id.textViewCommand);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    result -> {
                        if (result) {
                            // Recognizer initialization is a time-consuming and it involves IO,
                            // so we execute it in async task
                            permissionCheck = PackageManager.PERMISSION_GRANTED;
                            recognizeStart.setEnabled(false);
                            recognizeStart.setOnClickListener(view1 -> switchSearch(PHRASE_SEARCH));
                            startSetup();
                        }
                    });
            recognizeStart.setOnClickListener(view1 -> mPermissionResult.launch(Manifest.permission.RECORD_AUDIO));
            recognizeStart.setEnabled(true);
            recognizeStart.setImageResource(R.drawable.microphone_off);
        } else {
            recognizeStart.setOnClickListener(view1 -> switchSearch(PHRASE_SEARCH));
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        settingsChanged = false;
//        cause the preference manager does not currently store a strong reference to the listener
        listener = (sharedPreferences, key) -> {
//            Toast.makeText(requireActivity().getApplicationContext(), R.string.restart_app, Toast.LENGTH_LONG).show();
            if (key.startsWith("rec_")) {
                settingsChanged = true;
            }
        };
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
        // get permissions
        permissionCheck = ContextCompat.checkSelfPermission(requireActivity().getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        AppDatabase database = ((MainActivity) requireActivity()).getDatabase();

        arguments = database.CommandDao().
                getAll().
                stream().
                map(Command::getWord).
                collect(Collectors.toList());
        video = database.CommandDao().
                getAll().
                stream().
                map(Command::getPath).
                collect(Collectors.toList());
        startSetup();
    }

    private void startSetup() {
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new SetupTask(this).execute();
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
//        Log.e("Settings", Integer.toString(sharedPref.getInt("rec_kws_threshold", 6)) + " " +
//                             sharedPref.getString("rec_grammar_name", getString(R.string.grammar_name_default_value)) + " " +
//                            Boolean.toString(sharedPref.getBoolean("rec_save_logs", false)));
        int kws_threshold = sharedPref.getInt("rec_kws_threshold", getResources().getInteger(R.integer.kws_threshold_default_value));
        float vad_threshold = sharedPref.getInt("rec_vad_threshold", getResources().getInteger(R.integer.vad_threshold_default_value)) / 100.0f;
        String grammar_name = sharedPref.getString("rec_grammar_name", getString(R.string.grammar_name_default_value));
        SpeechRecognizerSetup setup = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "ru-ru-ptm"))
                .setDictionary(new File(assetsDir, "car.dict"))
                .setBoolean("-remove_noise", true)
                .setSampleRate(8000)
                .setKeywordThreshold((float) Math.pow(10, -kws_threshold))
                .setFloat("-vad_threshold", vad_threshold);

        if (sharedPref.getBoolean("rec_save_logs", false)) {
            setup.setRawLogDir(assetsDir); // To disable logging of raw audio comment out this call (takes a lot of space on the device)
        }
        recognizer = setup.getRecognizer();
        recognizer.addListener(this);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        keyphrase = getString(R.string.rec_keyphrase);
        recognizer.addKeyphraseSearch(KWS_SEARCH, keyphrase);
        File menuGrammar = new File(assetsDir, grammar_name);
        Log.d("File", menuGrammar.getAbsolutePath());
        StringBuilder sb = new StringBuilder("((?<=^| )(?:(?:");
        for (int i = 0; i < arguments.size(); i++) {
            sb.append(arguments.get(i));
            if (i < arguments.size() - 1) {
                sb.append(")|(?:");
            }
        }
        sb.append("))(?=$| ))+");
        splitter = Pattern.compile(sb.toString());
        recognizer.addGrammarSearch(PHRASE_SEARCH, menuGrammar);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        videoViewFragmentRecognize.reset();
        if (recognizer != null) {
            recognizer.cancel();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (recognizer != null) {
            if (settingsChanged) {
                recognizeStart.setEnabled(false);
                settingsChanged = false;
                recognizer.cancel();
                recognizer.shutdown();
                startSetup();
            } else {
                recognizeStart.setEnabled(true);
                switchSearch(KWS_SEARCH);
            }
        }
    }

    private void switchSearch(String searchName) {
        Log.d("switchSearch", "Try to switch");
        if (recognizer != null) {
            recognizer.stop();
            // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
            if (searchName.equals(KWS_SEARCH)) {
                recognizer.startListening(searchName);
                recognizeStart.setImageResource(R.drawable.microphone);
                Toast.makeText(requireActivity().getApplicationContext(), "Скажите \"активировать\"", Toast.LENGTH_SHORT).show();
                Log.d("switchSearch", "Activated");
            } else {
                recognizer.startListening(searchName, getResources().getInteger(R.integer.rec_timeout));
                recognizeStart.setImageResource(R.drawable.microphone_on);
                Toast.makeText(requireActivity().getApplicationContext(), "Слушаю", Toast.LENGTH_SHORT).show();
                Log.d("switchSearch", "Start listening");
            }
        }
    }

    @Override
    public void onError(Exception error) {
        //вывести, что все плохо в TextView
        Toast.makeText(requireActivity().getApplicationContext(), Objects.requireNonNull(error.getLocalizedMessage()), Toast.LENGTH_LONG).show();
        Log.e("onError", Objects.requireNonNull(error.getMessage()));
        Log.d("onError", Arrays.toString(Objects.requireNonNull(error.getStackTrace())));
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
        Log.d("onTimeout", "Stop listening");
        // hide RecognizeFragment here
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d("onBeginningOfSpeech", "Start recognition");
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        Log.d("onEndOfSpeech", "End search");
        if (recognizer.getSearchName().equals(PHRASE_SEARCH)) {
            switchSearch(KWS_SEARCH);
            // hide RecognizeFragment here
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;
        String text = hypothesis.getHypstr();
        if (text.equals(keyphrase)) {
            switchSearch(PHRASE_SEARCH);
            // show RecognizeFragment here

        }
        Log.d("onPartialResult", text);
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String strText = hypothesis.getHypstr();
            textViewCommand.setText(strText.toCharArray(), 0, strText.length());
            Log.d("onResult", strText);
            Matcher matcher = splitter.matcher(strText);
            while (matcher.find()) {
                String s = strText.substring(matcher.start(), matcher.end());
                if (arguments.contains(s)) { // necessary until not all phrases in car.gram implemented
                    videoViewFragmentRecognize.addVideo(Uri.parse("android.resource://" + requireActivity().getPackageName() + "/" + video.get(arguments.indexOf(s))));
                }
            }
        }
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<RecognizeFragment> activityReference;

        SetupTask(RecognizeFragment activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get().requireActivity());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                Toast.makeText(activityReference.get().requireActivity().getApplicationContext(), Objects.requireNonNull(result.getLocalizedMessage()), Toast.LENGTH_LONG).show();
                Log.e("onPostExecute", "Failed to init recognizer");
                Log.e("onPostExecute", Objects.requireNonNull(result.getMessage()));
                Log.d("onPostExecute", Arrays.toString(Objects.requireNonNull(result.getStackTrace())));
            } else {
                activityReference.get().switchSearch(KWS_SEARCH);
                activityReference.get().requireView().findViewById(R.id.recognizeStartButton).setEnabled(true);
            }
        }
    }
}