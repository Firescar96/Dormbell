package edu.mit.dormbell.dormbell;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import edu.mit.dormbell.MainActivity;
import edu.mit.dormbell.R;
import edu.mit.dormbell.org.json.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RingRingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RingRingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RingRingFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private OnFragmentInteractionListener mListener;
    static MainActivity context = MainActivity.context;
    static private RingRingFragment thisFrag;

    private View frame;
    private int section_number;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static RingRingFragment newInstance(int section_number) {
        RingRingFragment fragment = new RingRingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, section_number);
        fragment.setArguments(args);
        return fragment;
    }

    public RingRingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            section_number = getArguments().getInt(ARG_SECTION_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        frame = inflater.inflate(R.layout.fragment_ring_ring, container, false);
        return frame;
    }

    public void onRing(View v) {

        /*JSONObject jsonObject = new JSONObject(); //TODO: Fix DoorbellsFragment so this testing code can be removed
        try {
            JSONArray locks = new JSONArray();
            locks.put("everybodyusingtheappfortesting");
            jsonObject.put("locks", locks);
            jsonObject.put("update", true);
            jsonObject.put("username", context.appData.getString("username"));
            context.sendJSONToBackend(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        JSONObject data = new JSONObject();
        try {
            data.put("ring", true);
            data.put("sender", context.appData.getString("username"));
            data.put("lock","everybodyusingtheappfortesting");
            data.put("hash",computeSHA1(context.appData.getString("username")+"everybodyusingtheappfortesting" ));
            context.sendJSONToBackend(data);
        }
        catch (Exception e) {

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int id);
    }

    public static String computeSHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        StringBuffer hashcode = new StringBuffer();
        for (int i = 0; i < sha1hash.length; i++)
        {
            String h = Integer.toHexString(0xFF & sha1hash[i]);
            while (h.length() < 2)
                h = "0" + h;
            hashcode.append(h);
        }
        return hashcode.toString();
    }
}
