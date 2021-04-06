package com.example.surdo;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.surdo.db.AppDatabase;
import com.example.surdo.db.Command;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LibFragment extends Fragment {

    private VideoView videoViewFragmentLib;
    List<String> arguments;
    List<Integer> video;
    AppDatabase database;

    public LibFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lib, container, false);
        ListView listViewFragmentLib = view.findViewById(R.id.listViewFragmentLib);
        videoViewFragmentLib = view.findViewById(R.id.videoViewFragmentLib);
        Button backButton = view.findViewById(R.id.buttonBackToHome);

        listViewFragmentLib.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, arguments));
        listViewFragmentLib.setOnItemClickListener((parent, itemClicked, position, id) -> {
            TextView textView = (TextView) itemClicked;
            String strText = textView.getText().toString(); // получаем текст нажатого элемента
            videoViewFragmentLib.setVideoURI(Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + video.get(arguments.indexOf(strText))));
            videoViewFragmentLib.requestFocus(0);
            videoViewFragmentLib.start();
        });
        backButton.setOnClickListener(v -> Objects.requireNonNull(getFragmentManager()).
                beginTransaction().
                replace(R.id.fragmentContainer, ((MainActivity) Objects.requireNonNull(getActivity())).
                        getRecognizeFragment())
                .addToBackStack(null)
                .commit());

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }
}
