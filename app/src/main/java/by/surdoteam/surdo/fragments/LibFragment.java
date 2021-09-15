package by.surdoteam.surdo.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import by.surdoteam.surdo.MainActivity;
import by.surdoteam.surdo.R;
import by.surdoteam.surdo.db.AppDatabase;
import by.surdoteam.surdo.db.Command;

public class LibFragment extends Fragment {

    private VideoView videoViewFragmentLib;
    List<String> arguments;
    List<Integer> video;
    AppDatabase database;

    public LibFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lib, container, false);
        ListView listViewFragmentLib = view.findViewById(R.id.listViewFragmentLib);
        videoViewFragmentLib = view.findViewById(R.id.videoViewFragmentLib);

        listViewFragmentLib.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, arguments));
        listViewFragmentLib.setOnItemClickListener((parent, itemClicked, position, id) -> {
            TextView textView = (TextView) itemClicked;
            String strText = textView.getText().toString(); // получаем текст нажатого элемента
            videoViewFragmentLib.setVideoPath("android.resource://" + requireActivity().getPackageName() + "/" + video.get(arguments.indexOf(strText)));
            videoViewFragmentLib.requestFocus(0);
            videoViewFragmentLib.start();
        });
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = ((MainActivity) requireActivity()).getDatabase();

        arguments = database.CommandDao().
                getAll().
                stream().
                map((command -> command.getWord().split("\\|")[0])).
                collect(Collectors.toList());
        video = database.CommandDao().
                getAll().
                stream().
                map(Command::getPath).
                collect(Collectors.toList());
    }
}
