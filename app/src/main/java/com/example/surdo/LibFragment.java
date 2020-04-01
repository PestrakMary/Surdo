package com.example.surdo;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;

import java.util.List;

public class LibFragment extends Fragment {

    private ListView listViewFragmentLib;
    private VideoView videoViewFragmentLib;
    private Button backButton;
    List<String> arguments;
    List<Integer> video;
    public LibFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lib, container, false);
        listViewFragmentLib = (ListView) view.findViewById(R.id.listViewFragmentLib);
        videoViewFragmentLib = view.findViewById(R.id.videoViewFragmentLib);
        backButton = view.findViewById(R.id.buttonBackToHome);

        arguments = getArguments().getStringArrayList("text");
        video = getArguments().getIntegerArrayList("video");

        listViewFragmentLib.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, arguments));
        listViewFragmentLib.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                TextView textView = (TextView) itemClicked;
                String strText = textView.getText().toString(); // получаем текст нажатого элемента
                videoViewFragmentLib.setVideoURI(Uri.parse("android.resource://" + getArguments().getString("packageName") + "/" + video.get(arguments.indexOf(strText))));
                videoViewFragmentLib.requestFocus(0);
                videoViewFragmentLib.start();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().remove(LibFragment.this).commit();
            }
        });

        return view;
    }
}
