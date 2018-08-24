package com.kpblog.tt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.Customer;
import com.kpblog.tt.receiver.TraTemptationReceiver;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;

import java.util.ArrayList;
import java.util.Calendar;
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
    private Button getCodeBtn, lockUnlockBtn, submitBtn;
    private long getCodeBtnLastClicked, lockUnlockBtnLastClicked, submitBtnLastClicked;
    private Spinner textActionDropdown, userTypeDropdown, scheduledTimeDropdown;
    private EditText testUsers, promotionName;

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
        adminCode.setTransformationMethod(null);

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

        testUsers = (EditText) getView().findViewById(R.id.testUserEditText);
        testUsers.setText(getTestUsers());

        int recipient = 0;
        if (customers != null && customers.length > 0){
            recipient = customers.length;
        }

        TextView recipientLabel = (TextView) getView().findViewById(R.id.recipientLabel);
        recipientLabel.setText(String.format(getString(R.string.recipients), recipient));

        EditText recipientBox = (EditText) getView().findViewById(R.id.recipientsBox);
        recipientBox.setText(getFormattedRecipientList(customers));

        messageBox = (EditText) getView().findViewById(R.id.messageBox);
        promotionName = (EditText) getView().findViewById(R.id.promotionName);

        textActionDropdown = (Spinner) getView().findViewById(R.id.textActionDropdown);
        textActionDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String action = textActionDropdown.getSelectedItem().toString();
                if (getString(R.string.textNow).equals(action)){
                    userTypeDropdown.setVisibility(View.VISIBLE);
                    scheduledTimeDropdown.setVisibility(View.GONE);
                }
                else {
                    userTypeDropdown.setVisibility(View.GONE);
                    scheduledTimeDropdown.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        userTypeDropdown = (Spinner) getView().findViewById(R.id.userTypeDropdown);
        scheduledTimeDropdown = (Spinner) getView().findViewById(R.id.scheduledTimeDropdown);

        submitBtn = (Button) getView().findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - submitBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD){
                    submitBtnLastClicked = SystemClock.elapsedRealtime();
                    performSelectedAction();
                }
            }
        });
    }

    private void performSelectedAction() {
        String msg = messageBox.getText().toString();
        if (msg.isEmpty()){
            Util.displayToast(getContext(), "Message is empty");
            return;
        }

        String promoName = promotionName.getText().toString();

        String action = textActionDropdown.getSelectedItem().toString();
        if (getString(R.string.textNow).equals(action)){
            String userType = userTypeDropdown.getSelectedItem().toString();
            if (getString(R.string.realUsers).equals(userType)){
                //text real users from customer list
                textCustomers(msg, promoName);
            }
            else {
                //text test users
                String testUserList = testUsers.getText().toString();
                if (testUserList.isEmpty()){
                    Util.displayToast(getContext(), "Test user list is empty");
                    return;
                }
                List<String> testPhoneNumbers = splitIntoTestPhoneNumbers(testUserList);
                Util.textPromoToMultipleRecipientsAndUpdateLastTexted(testPhoneNumbers, msg, handler, true, promoName);
                Util.displayToast(getContext(), String.format("Message sent to %d test users", testPhoneNumbers.size()));
            }
        }
        else {
            //schedule
            //e.g 10:00
            Calendar scheduledTime = getScheduledTime();
            String type = Constants.BROADCAST_TYPE_ON_DEMAND;
            int broadcastId = handler.insertIntoCustomerBroadcastTable(scheduledTime.getTimeInMillis(), msg, type, promoName, customers);

            //need to set the alarm to send the message
            Intent intent = new Intent(getContext(), TraTemptationReceiver.class);
            intent.setAction(Constants.SCHEDULED_TEXT_ACTION);
            Util.setAlarmForScheduledJob(getContext(), scheduledTime, intent, broadcastId);
        }
    }

    @NonNull
    private Calendar getScheduledTime() {
        String[] scheduledTime = scheduledTimeDropdown.getSelectedItem().toString().split(":");
        int scheduleHour = Integer.parseInt(scheduledTime[0]);
        int scheduledMin = Integer.parseInt(scheduledTime[1]);

        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, scheduleHour);
        alarmTime.set(Calendar.MINUTE, scheduledMin);
        alarmTime.set(Calendar.SECOND, 0);

        final long now = System.currentTimeMillis();
        if (now >= alarmTime.getTimeInMillis()){
            //current time is already later than the alarm time, set for next day
            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        return alarmTime;
    }

    private List<String> splitIntoTestPhoneNumbers(String testUserList) {
        List<String> phoneList = new ArrayList<String>();
        String[] phoneNum = testUserList.split(",");
        for (int i = 0; i < phoneNum.length; i++){
            if (!phoneNum[i].trim().isEmpty()){
                phoneList.add(Util.getUnformattedPhoneNumber(phoneNum[i].trim()));
            }
        }

        return phoneList;
    }

    private String getTestUsers() {
        StringBuffer sb = new StringBuffer();
        List<String> testUsers = handler.getTestUsers();
        for (int i = 0; i < testUsers.size(); i++){
            if (i > 0){
                sb.append(", ");
            }

            sb.append(Util.formatPhoneNumber(testUsers.get(i)));
        }
        return sb.toString();
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

    private void textCustomers(String msg, String promoName) {
        if (customers != null && customers.length > 0){

            List<String> recipients = new ArrayList<String>();
            for (Customer c : customers){
                recipients.add(c.getCustomerId());
            }

            Util.textPromoToMultipleRecipientsAndUpdateLastTexted(recipients, msg, handler, true, promoName);
            Util.displayToast(getContext(),"message sent to " + recipients.size() + " recipients");
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
