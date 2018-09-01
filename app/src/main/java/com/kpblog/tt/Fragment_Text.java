package com.kpblog.tt;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.Customer;
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
public class Fragment_Text extends Fragment implements TextView.OnEditorActionListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String KEY_CUSTOMER_ARRAY = "customerArray";
    private static final String KEY_QUERY_TYPE = "param2";

    // TODO: Rename and change types of parameters
    private Customer[] customers;
    private String queryType;

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
     * @param queryType Parameter 2.
     * @return A new instance of fragment Fragment_Text.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Text newInstance(Customer[] customers, String queryType) {
        Fragment_Text fragment = new Fragment_Text();
        Bundle args = new Bundle();
        args.putSerializable(KEY_CUSTOMER_ARRAY, customers);
        args.putString(KEY_QUERY_TYPE, queryType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            customers = (Customer[]) getArguments().getSerializable(KEY_CUSTOMER_ARRAY);
            queryType = getArguments().getString(KEY_QUERY_TYPE);
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

        //test code, remove when done
        //Util.sendScheduledBroadcast(getContext(), handler);

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
        setMessageContentAndPromoNameBasedOnQueryType();

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

    @Override
    public void onResume() {
        super.onResume();
        if (!Util.isAdminCodeRequired(getActivity())){
            showAdminScreen();
        }
    }

    private void setMessageContentAndPromoNameBasedOnQueryType() {
        if (isDrinkCreditReminderQueryType()){
            messageBox.setText(getString(R.string.drinkCreditReminderMessage));
        }
        else if (isInactiveNewPromoQueryType()){
            //set the new promo in the message
            final String inactiveMessage = getString(R.string.inactiveUser_sendPromo);
            String inactiveMesageWithPromo = inactiveMessage.replace(Constants.PROMO_NAME_PLACE_HOLDER, Constants.INACTIVE_USER_PROMO_NAME);
            messageBox.setText(inactiveMesageWithPromo);
            promotionName.setText(Constants.INACTIVE_USER_PROMO_NAME);
        }
        else if (isInactiveOldPromoQueryType()){
            final String inactiveMessageOldPromoFormat = getString(R.string.inactiveUser_promoReminder);
            messageBox.setText(inactiveMessageOldPromoFormat);
            //we'll load the promo name for each customer at texting time
            promotionName.setText("");
        }
    }

    private boolean isInactiveOldPromoQueryType() {
        return getString(R.string.queryType_inactiveOldPromo).equals(queryType);
    }

    private boolean isInactiveNewPromoQueryType(){
        return getString(R.string.queryType_inactiveNewPromo).equals(queryType);
    }

    private boolean isDrinkCreditReminderQueryType() {
        return getString(R.string.queryType_drinkCreditReminder).equals(queryType);
    }

    private void performSelectedAction() {
        String msg = messageBox.getText().toString();
        if (msg.isEmpty()){
            Util.displayToast(getContext(), "Message is empty");
            return;
        }
        msg = msg.replace("%s", Constants.CLAIM_CODE_PLACE_HOLDER);
        String promoName = promotionName.getText().toString();

        String action = textActionDropdown.getSelectedItem().toString();
        if (getString(R.string.textNow).equals(action)){
            msg = msg.replace(Constants.PROMO_NAME_PLACE_HOLDER, promoName);
            String userType = userTypeDropdown.getSelectedItem().toString();
            if (getString(R.string.realUsers).equals(userType)){
                //text real users from customer list
                if (isDrinkCreditReminderQueryType()){
                    textDrinkCreditReminderToCustomers(customers, msg);
                }
                else {
                    textCustomers(msg, promoName);
                }
            }
            else {
                //text test users
                String testUserList = testUsers.getText().toString();
                if (testUserList.isEmpty()){
                    Util.displayToast(getContext(), "Test user list is empty");
                    return;
                }
                List<String> testPhoneNumbers = splitIntoTestPhoneNumbers(testUserList);

                //need to check the query type to determine whether dynamic info is needed
                if (isDrinkCreditReminderQueryType()){
                    textDrinkCreditReminderUsingPhoneList(testPhoneNumbers, msg);
                }
                else {
                    Util.textPromoToMultipleRecipientsAndUpdateLastTexted(testPhoneNumbers, msg, handler, true, promoName);
                }

                Util.displayToast(getContext(), String.format("Message sent to %d test users", testPhoneNumbers.size()));
            }
        }
        else {
            //schedule
            Calendar scheduledTime = getScheduledTime();
            String type = getBroadcastType();
            int broadcastId = handler.insertIntoCustomerBroadcastTable(scheduledTime.getTimeInMillis(), msg, type, promoName, customers, getContext());

            //need to set the alarm to send the message
            Util.setAlarmForScheduledJob(getActivity().getApplicationContext(), scheduledTime, broadcastId);
            Util.displayToast(getContext(), String.format("Text scheduled for %s", scheduledTimeDropdown.getSelectedItem().toString()));
            submitBtn.setEnabled(false);
        }
    }

    private void textDrinkCreditReminderToCustomers(Customer[] customers, String msg) {

        long timestamp = System.currentTimeMillis();
        for (Customer c : customers){
            textDrinkCreditReminderToSingleCustomer(msg, timestamp, c);
        }

    }

    private void textDrinkCreditReminderToSingleCustomer(String msg, long timestamp, Customer c) {
        msg = String.format(msg, c.getTotalCredit(), Constants.FREE_DRINK_THRESHOLD - c.getTotalCredit());
        Util.textSingleRecipient(c.getCustomerId(), msg);
        handler.updateLastTexted(c.getCustomerId(), timestamp);
    }

    /**
     * retrieve the customers by the phone numbers
     * and text the credit reminder
     * @param recipientList
     */
    private void textDrinkCreditReminderUsingPhoneList(List<String> recipientList, String msg) {
        long timestamp = System.currentTimeMillis();

        for (String customerId : recipientList){
            Customer c = handler.getCustomerById(customerId);
            if (c == null){
                c = new Customer();
                c.setCustomerId(customerId);
            }

            textDrinkCreditReminderToSingleCustomer(msg, timestamp, c);
        }
    }

    private String getBroadcastType() {
        String broadcastType = Constants.BROADCAST_TYPE_SCHEDULED_FREE_FORM;

        if (isDrinkCreditReminderQueryType()){
            broadcastType = Constants.BROADCAST_TYPE_SCHEDULED_CREDIT_REMINDER;
        }
        else if (isInactiveNewPromoQueryType()){
            broadcastType = Constants.BROADCAST_TYPE_SCHEDULED_INACTIVE_NEW_PROMO;
        }
        else if (isInactiveOldPromoQueryType()){
            broadcastType = Constants.BROADCAST_TYPE_SCHEDULED_INACTIVE_OLD_PROMO;
        }

        return broadcastType;
    }

    @NonNull
    private Calendar getScheduledTime() {
        //e.g 10:00
        String[] scheduledTime = scheduledTimeDropdown.getSelectedItem().toString().split(":");
        int scheduleHour = Integer.parseInt(scheduledTime[0]);
        int scheduledMin = Integer.parseInt(scheduledTime[1]);

        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, scheduleHour);
        alarmTime.set(Calendar.MINUTE, scheduledMin);
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);

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

            msg = msg.replace(Constants.PROMO_NAME_PLACE_HOLDER, promoName);
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
        getView().findViewById(R.id.adminLayout).setVisibility(View.VISIBLE);
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
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        String id = textView.getResources().getResourceEntryName(textView.getId());
        String adminCodeId = adminCode.getResources().getResourceEntryName(adminCode.getId());

        if (id.equals(adminCodeId)) {
            unlockScreen();
        }
        return true;
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
