package edu.mit.dormbell.dormbell;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.mit.dormbell.MainActivity;
import edu.mit.dormbell.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    static MainActivity context = MainActivity.context;

    private static final String TAG = "ProfileFragment";
    private int section_number;

    private View frame;

    private String sender;
    private String senderfull;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ProfileFragment newInstance(int section_number, Bundle args) {
        ProfileFragment fragment = new ProfileFragment();
        args.putInt(ARG_SECTION_NUMBER, section_number);
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            section_number = getArguments().getInt(ARG_SECTION_NUMBER);
            sender = getArguments().getString("sender");
            senderfull = getArguments().getString("senderfull");
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        frame = inflater.inflate(R.layout.fragment_status, container, false);
        ((TextView) frame.findViewById(R.id.fullname)).setText(getArguments().getString("fullname"));
        ((TextView) frame.findViewById(R.id.username)).setText(getArguments().getString("username"));
        return frame;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
