package com.example.surdo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;

import java.util.List;

public class RecognizeFragment extends Fragment {

    private VideoView videoViewFragmentRecognize;
    String word;
    List<Integer> video;
    private Button backButton;
    private Button recognizeStart;
    public RecognizeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recognize, container, false);
        videoViewFragmentRecognize = view.findViewById(R.id.videoViewFragmentRecognize);
        backButton = view.findViewById(R.id.buttonBackToMain);
        recognizeStart = view.findViewById(R.id.recognizeStartbutton);

        video = getArguments().getIntegerArrayList("video");
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().remove(RecognizeFragment.this).commit();
            }
        });

        return view;
    }

}