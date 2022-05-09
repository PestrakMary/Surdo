package by.surdoteam.surdo.fragments;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.stream.Collectors;

import by.surdoteam.surdo.MainActivity;
import by.surdoteam.surdo.R;
import by.surdoteam.surdo.db.AppDatabase;
import by.surdoteam.surdo.db.Command;

public class LibFragment extends Fragment {

    private VideoView videoViewFragmentLib;
    List<Command> commands;
    AppDatabase database;

    public LibFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = ((MainActivity) requireActivity()).getDatabase();
        commands = database.CommandDao().getPrimary();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lib, container, false);
        ListView listViewFragmentLib = view.findViewById(R.id.listViewFragmentLib);
        videoViewFragmentLib = view.findViewById(R.id.videoViewFragmentLib);

        listViewFragmentLib.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                commands.stream().map(Command::getWord).collect(Collectors.toList())));
        listViewFragmentLib.setOnItemClickListener((parent, itemClicked, position, id) -> {
            videoViewFragmentLib.setVideoPath(commands.get(position).getPath());
            videoViewFragmentLib.requestFocus(0);
            videoViewFragmentLib.start();
        });
        return view;
    }
}
