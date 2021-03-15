package com.example.surdo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.surdo.db.AppDatabase;
import com.example.surdo.db.Command;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class RecognizeFragment extends Fragment implements RecognitionListener {

    private static final String KWS_SEARCH = "wakeup";
    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "активировать";
    /* Named searches allow to quickly reconfigure the decoder */
    private static final String PHRASE_SEARCH = "phrase";
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    List<String> arguments;
    List<Integer> video;
    private VideoView videoViewFragmentRecognize;
    private TextView textViewCommand;
    private SpeechRecognizer recognizer;
    private int permissionCheck;
    AppDatabase database;

    public RecognizeFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recognize, container, false);
        videoViewFragmentRecognize = view.findViewById(R.id.videoViewFragmentRecognize);
        Button backButton = view.findViewById(R.id.buttonBackToMain);
        Button recognizeStart = view.findViewById(R.id.recognizeStartbutton);
        textViewCommand = view.findViewById(R.id.textViewCommand);

        database = ((MainActivity) Objects.requireNonNull(getActivity())).getDatabase();

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

        backButton.setOnClickListener(v -> Objects.requireNonNull(getFragmentManager()).
                beginTransaction().
                replace(R.id.fragmentContainer,
                        ((MainActivity) Objects.requireNonNull(getActivity())).getLibFragment()).
                commit());

        recognizeStart.setOnClickListener(view1 -> switchSearch(PHRASE_SEARCH));

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get permissions
        permissionCheck = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()).getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        startSetup();
    }

    private void startSetup() {
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new SetupTask(this).execute();
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "ru-ru-ptm"))
                .setDictionary(new File(assetsDir, "car.dict"))
                .setBoolean("-remove_noise", true)
                .setSampleRate(8000)
                .setKeywordThreshold(1e-7f)

                //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(this);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        File menuGrammar = new File(assetsDir, "car.gram");
        Log.e("File", menuGrammar.getAbsolutePath());
        recognizer.addGrammarSearch(PHRASE_SEARCH, menuGrammar);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                permissionCheck = PackageManager.PERMISSION_GRANTED;
                new SetupTask(this).execute();
            } else {
                Objects.requireNonNull(getFragmentManager()).beginTransaction().remove(RecognizeFragment.this).commit();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    private void switchSearch(String searchName) {
        Log.e("switchSearch", "Try");
        if (recognizer != null) {
            recognizer.stop();
            // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
            if (searchName.equals(KWS_SEARCH)) {
                recognizer.startListening(searchName);
                Log.e("switchSearch", "Activated");
            } else {
                Log.e("switchSearch", "Start listening");
                recognizer.startListening(searchName, 10000);
            }
        }
    }

    @Override
    public void onError(Exception error) {
        //вывести, что все плохо в TextView
        Log.e("onError", Objects.requireNonNull(error.getMessage()));
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
        Log.e("onTimeout", "Stop listening");
        // hide RecognizeFragment here
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.e("onBeginningOfSpeech", "Start recognition");
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        Log.e("onEndOfSpeech", "End search");
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
        if (text.equals(KEYPHRASE)) {
            switchSearch(PHRASE_SEARCH);
            // show RecognizeFragment here

        }
        Log.e("onPartialResult", text);
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String strText = hypothesis.getHypstr();
            Log.e("onResult", strText);
            if (arguments.contains(strText)) { // necessary until not all phrases in car.gram implemented
                textViewCommand.setText(strText.toCharArray(), 0, strText.length());
                videoViewFragmentRecognize.setVideoURI(Uri.parse("android.resource://" + Objects.requireNonNull(getActivity()).getPackageName() + "/" + video.get(arguments.indexOf(strText))));
                videoViewFragmentRecognize.requestFocus(0);
                videoViewFragmentRecognize.start();
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
                Assets assets = new Assets(Objects.requireNonNull(activityReference.get().getActivity()));
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
                Log.e("onPostExecute", "Failed to init recognizer");
            } else {
                activityReference.get().switchSearch(KWS_SEARCH);
            }
        }
    }
}