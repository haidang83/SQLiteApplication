package com.kpblog.tt;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kpblog.tt.adapter.BroadcastListViewAdapter;
import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.CustomerBroadcast;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_ScheduledBroadcast.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_ScheduledBroadcast#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_ScheduledBroadcast extends Fragment implements TextView.OnEditorActionListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private Spinner statusDropdown, adminDropdown;
    private DatabaseHandler handler;
    ListView listview;
    EditText adminCode;
    Button getCodeBtn, lockUnlockBtn;
    long getCodeBtnLastClicked = 0;
    long lockUnlockBtnLastClicked = 0;

    public Fragment_ScheduledBroadcast() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_ScheduledBroadcast.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_ScheduledBroadcast newInstance(String param1, String param2) {
        Fragment_ScheduledBroadcast fragment = new Fragment_ScheduledBroadcast();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment__scheduled_broadcast, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        handler = new DatabaseHandler(getContext());

        adminCode = (EditText) (getView().findViewById(R.id.adminCode));
        adminCode.setTransformationMethod(null);
        adminCode.setOnEditorActionListener(this);

        adminDropdown = (Spinner) getView().findViewById(R.id.adminPhoneDropdown);
        String[] admins = handler.getAllAdmins().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, admins);
        adminDropdown.setAdapter(adapter);

        getCodeBtn = (Button) (getView().findViewById(R.id.getCodeBtn));
        getCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - getCodeBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD) {
                    Util.sendAdminCodeAndSaveToSharedPref(adminDropdown.getSelectedItem().toString(), getActivity(), getString(R.string.adminCodeTextMsg));
                    Toast.makeText(getContext().getApplicationContext(), getString(R.string.adminCodeSentToastMsg), Toast.LENGTH_LONG).show();
                    getCodeBtnLastClicked = SystemClock.elapsedRealtime();
                }
            }
        });

        lockUnlockBtn = (Button) (getView().findViewById(R.id.lockUnlockBtn));
        lockUnlockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - lockUnlockBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD) {
                    lockUnlockAdminScreen();
                    lockUnlockBtnLastClicked = SystemClock.elapsedRealtime();
                }
            }
        });

        statusDropdown = (Spinner) getView().findViewById(R.id.broadcastStatusDropdown);
        statusDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String broadcastStatus = statusDropdown.getSelectedItem().toString();
                if (broadcastStatus.equals(getString(R.string.broadcastStatus_all))){
                    broadcastStatus = "";
                }
                CustomerBroadcast[] cbs = handler.getAllCustomerBroadcastByStatus(broadcastStatus.toLowerCase());

                BroadcastListViewAdapter adapter = new BroadcastListViewAdapter(getContext(), R.layout.broadcast_row_layout, R.id.timestamp, cbs);
                listview.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        listview = (ListView) getView().findViewById(R.id.listview);
        // Inflate customerHeader view
        ViewGroup headerView = (ViewGroup)getLayoutInflater().inflate(R.layout.broadcast_header, listview,false);
        // Add customerHeader view to the ListView
        listview.addHeaderView(headerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Util.isAdminCodeRequired(getActivity())){
            showAdminScreen();
        }
    }

    private void lockUnlockAdminScreen() {
        final String buttonAction = getString(R.string.unlock);
        if (lockUnlockBtn.getText().toString().equals(buttonAction)){
            unlockScreen();
        }
        else {
            lockScreen();
        }
    }

    private void lockScreen() {
        lockUnlockBtn.setText(getString(R.string.unlock));
        getCodeBtn.setEnabled(true);
        adminCode.setEnabled(true);

        getView().findViewById(R.id.unlockedContentLayout).setVisibility(View.INVISIBLE);

        Util.expireAdminCode(getActivity());
    }

    private void unlockScreen() {
        String inputCode = adminCode.getText().toString();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String expectedCode = sp.getString(Constants.SHARED_PREF_ADMIN_CODE_KEY, null); // Second parameter is the default value.

        TextInputLayout adminCodeLayout = (TextInputLayout) (getView().findViewById(R.id.adminCodeLayout));

        if (!inputCode.isEmpty() && inputCode.equals(expectedCode)){
            adminCodeLayout.setErrorEnabled(false);

            Util.clearAdminCodeAndSetExpirationTime(sp);
            showAdminScreen();
        }
        else {
            adminCodeLayout.setError(getString(R.string.claimCode_err_msg));
        }
    }

    private void showAdminScreen() {
        //change unlock button text to lock
        lockUnlockBtn.setText(getString(R.string.lock));
        getCodeBtn.setEnabled(false);
        adminCode.setText("");
        adminCode.setEnabled(false);

        //open admin screen
        getView().findViewById(R.id.unlockedContentLayout).setVisibility(View.VISIBLE);
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        String id = textView.getResources().getResourceEntryName(textView.getId());
        String adminCodeId = adminCode.getResources().getResourceEntryName(adminCode.getId());

        if (id.equals(adminCodeId)) {
            unlockScreen();
        }
        return true;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
