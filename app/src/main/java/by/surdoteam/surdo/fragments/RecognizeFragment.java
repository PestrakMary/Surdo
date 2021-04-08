package by.surdoteam.surdo.fragments;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import by.surdoteam.surdo.MainActivity;
import by.surdoteam.surdo.MultipleVideoView;
import by.surdoteam.surdo.R;
import by.surdoteam.surdo.db.AppDatabase;
import by.surdoteam.surdo.db.Command;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private SpeechRecognizer recognizer;
    private Pattern splitter;
    private int permissionCheck;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private List<String> arguments;
    private List<Integer> video;
    private TextView textViewCommand;

    private MultipleVideoView videoViewFragmentRecognize;

    public RecognizeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recognize, container, false);
        videoViewFragmentRecognize = new MultipleVideoView(view.findViewById(R.id.videoViewFragmentRecognize));

        FloatingActionButton recognizeStart = view.findViewById(R.id.recognizeStartbutton);
        recognizeStart.setScaleType(ImageView.ScaleType.FIT_CENTER);
        textViewCommand = view.findViewById(R.id.textViewCommand);

        recognizeStart.setOnClickListener(view1 -> switchSearch(PHRASE_SEARCH));
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get permissions
        permissionCheck = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()).getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        AppDatabase database = ((MainActivity) Objects.requireNonNull(getActivity())).getDatabase();

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
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
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
                .setKeywordThreshold(1e-6f)

                //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(this);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        File menuGrammar = new File(assetsDir, "car.gram");
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
            switchSearch(KWS_SEARCH);
        }
    }

    private void switchSearch(String searchName) {
        Log.d("switchSearch", "Try to switch");
        if (recognizer != null) {
            recognizer.stop();
            // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
            if (searchName.equals(KWS_SEARCH)) {
                recognizer.startListening(searchName);
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Скажите \"активировать\"", Toast.LENGTH_SHORT).show();
                Log.d("switchSearch", "Activated");
            } else {
                recognizer.startListening(searchName, 10000);
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Слушаю", Toast.LENGTH_SHORT).show();
                Log.d("switchSearch", "Start listening");
            }
        }
    }

    @Override
    public void onError(Exception error) {
        //вывести, что все плохо в TextView
        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), Objects.requireNonNull(error.getLocalizedMessage()), Toast.LENGTH_LONG).show();
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
        if (text.equals(KEYPHRASE)) {
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
                    videoViewFragmentRecognize.addVideo(Uri.parse("android.resource://" + Objects.requireNonNull(getActivity()).getPackageName() + "/" + video.get(arguments.indexOf(s))));
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
                Toast.makeText(Objects.requireNonNull(activityReference.get().getActivity()).getApplicationContext(), Objects.requireNonNull(result.getLocalizedMessage()), Toast.LENGTH_LONG).show();
                Log.e("onPostExecute", "Failed to init recognizer");
                Log.e("onPostExecute", Objects.requireNonNull(result.getMessage()));
                Log.d("onPostExecute", Arrays.toString(Objects.requireNonNull(result.getStackTrace())));
            } else {
                activityReference.get().switchSearch(KWS_SEARCH);
                Objects.requireNonNull(activityReference.get().getView()).findViewById(R.id.recognizeStartbutton).setEnabled(true);
            }
        }
    }
}