package edu.mit.dormbell.dormbell;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import edu.mit.dormbell.MainActivity;
import edu.mit.dormbell.R;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class DoorbellsFragment extends ListFragment implements AbsListView.OnItemClickListener,
Validator.ValidationListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "DoorbellsFragment";
    private static MainActivity context = MainActivity.context;

    private int section_number;

    private OnFragmentInteractionListener mListener;
    private View frame;

    @NotEmpty(message = "this door needs a name")
    private EditText addLockText;

    private Validator validator;
    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    private ArrayList<String> contentList;
    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter listAdapter;

    public static DoorbellsFragment newInstance(int section_number) {
        DoorbellsFragment fragment = new DoorbellsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER,section_number);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DoorbellsFragment() {
    }

    @Override
    public void onValidationSucceeded() {
        Log.i(TAG,"validation succeeded");
        try {
            String newLock = ((EditText) frame.findViewById(R.id.addLockText)).getText().toString();
            //context.appData.getJSONArray("locks").put(newLock);
            contentList.add(newLock);
            //listAdapter.notifyDataSetChanged();
            Log.i(TAG,"data set changed");
        } catch (Exception e) {
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(context);

            // Display error messages ;)
            if (view == addLockText) {
                addLockText.setError(message);
            }
        }
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
        frame = inflater.inflate(R.layout.fragment_doorbells, container, false);

        validator = new Validator(this);
        validator.setValidationListener(this);

        addLockText = (EditText) frame.findViewById(R.id.addLockText);

        contentList = new ArrayList<String>();

        try {
            JSONArray lockSon = MainActivity.appData.getJSONArray("locks");
            int len = lockSon.length();
            for (int i=0;i<len;i++){
                contentList.add(lockSon.get(i).toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG,contentList.toString());
        listAdapter = new ArrayAdapter<String>(frame.getContext(),android.R.layout.simple_list_item_1,contentList);

        // Set the adapter
        mListView = (ListView) frame.findViewById(android.R.id.list);
        mListView.setAdapter(listAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        /*mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { //TODO: Implement this section to allow people to remove locks with a single click
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
            {
                view.animate().setDuration(1000).alpha(0);
                view.animate().setDuration(1000).alpha(1);
                if((ColorDrawable)view.getBackground() == null)
                {
                    view.setBackgroundColor(Color.rgb(224, 0, 224));
                    while(mateSelected.size() < position+1) mateSelected.add(false);
                    mateSelected.set(position, true);
                }
                else
                {
                    view.setBackground(null);
                    while(mateSelected.size() < position+1) mateSelected.add(false);
                    mateSelected.set(position, false);
                }
            }
        });*/

        return frame;
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


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {

        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public void addLock(View v) {
        validator.validate();
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

}
