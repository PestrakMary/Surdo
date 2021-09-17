package by.surdoteam.surdo.fragments;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import by.surdoteam.surdo.R;

public class AboutFragment extends Fragment {

    public AboutFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ((TextView) view.findViewById(R.id.textViewAboutText)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) view.findViewById(R.id.textViewMentioning)).setMovementMethod(LinkMovementMethod.getInstance());
        TextView textViewVersion = view.findViewById(R.id.textViewVersion);
        try {
            textViewVersion.setText(textViewVersion.getText().toString().replace("VERSION_NAME_PLACEHOLDER", requireActivity().getPackageManager().getPackageInfo("by.surdoteam.surdo", 0).versionName));
        } catch (PackageManager.NameNotFoundException | IllegalStateException ignored) {
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
