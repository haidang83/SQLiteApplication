package com.kpblog.tt;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.Customer;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Text.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Text#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Text extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String KEY_CUSTOMER_ARRAY = "customerArray";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private Customer[] customers;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private DatabaseHandler handler;
    private EditText adminCode, messageBox;
    private Spinner adminDropdown;
    private Button getCodeBtn, lockUnlockBtn, textNowBtn;
    private long getCodeBtnLastClicked, lockUnlockBtnLastClicked, textNowBtnLastClicked;

    public Fragment_Text() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param customers Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Text.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Text newInstance(Customer[] customers, String param2) {
        Fragment_Text fragment = new Fragment_Text();
        Bundle args = new Bundle();
        args.putSerializable(KEY_CUSTOMER_ARRAY, customers);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            customers = (Customer[]) getArguments().getSerializable(KEY_CUSTOMER_ARRAY);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment__text, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        handler = new DatabaseHandler(getContext());
        adminCode = (EditText) (getView().findViewById(R.id.adminCode));

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

        int recicientNum = 0;
        if (customers != null && customers.length > 0){
            recicientNum = customers.length;
        }

        TextView recipientLabel = (TextView) getView().findViewById(R.id.recipientLabel);
        recipientLabel.setText(String.format(getString(R.string.recipients), recicientNum));

        EditText recipientBox = (EditText) getView().findViewById(R.id.recipientsBox);
        recipientBox.setText(getFormattedRecipientList(customers));

        messageBox = (EditText) getView().findViewById(R.id.messageBox);

        textNowBtn = (Button) getView().findViewById(R.id.sendTextBtn);
        textNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - textNowBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD){
                    textNowBtnLastClicked = SystemClock.elapsedRealtime();
                    textCustomers(messageBox.getText().toString());
                }
            }
        });
    }

    private String getFormattedRecipientList(Customer[] customers) {
        StringBuffer sb = new StringBuffer();

        if (customers != null && customers.length > 0){
            for (int i = 0; i < customers.length; i++){
                Customer c = customers[i];
                if (i > 0){
                    sb.append(", ");
                }
                sb.append(Util.formatPhoneNumber(c.getCustomerId()));
            }
        }

        return sb.toString();
    }

    private void textCustomers(String msg) {
        if (customers != null && customers.length > 0){
            if (msg.isEmpty()){
                Util.displayToast(getContext(), "Message is empty");
            }
            else {
                List<String> recipients = new ArrayList<String>();
                for (Customer c : customers){
                    recipients.add(c.getCustomerId());
                }

                Util.textMultipleRecipientsAndUpdateLastTexted(recipients, msg, handler, true);
                Util.displayToast(getContext(),"message sent to " + recipients.size() + " recipients");
            }
        }
        else {
            Util.displayToast(getContext(), "Customer list is empty");
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

        getView().findViewById(R.id.adminLayout).setVisibility(View.INVISIBLE);
    }

    private void unlockScreen() {
        String inputCode = adminCode.getText().toString();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String expectedCode = sp.getString(Constants.SHARED_PREF_ADMIN_CODE_KEY, null); // Second parameter is the default value.

        TextInputLayout adminCodeLayout = (TextInputLayout) (getView().findViewById(R.id.adminCodeLayout));

        if (!inputCode.isEmpty() && inputCode.equals(expectedCode)){
            adminCodeLayout.setErrorEnabled(false);

            //clear sharedpref code
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(Constants.SHARED_PREF_ADMIN_CODE_KEY);
            editor.apply();

            //change unlock button text to lock
            lockUnlockBtn.setText(getString(R.string.lock));
            getCodeBtn.setEnabled(false);
            adminCode.setText("");
            adminCode.setEnabled(false);

            //open admin screen
            getView().findViewById(R.id.adminLayout).setVisibility(View.VISIBLE);
        }
        else {
            adminCodeLayout.setError(getString(R.string.claimCode_err_msg));
        }
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
