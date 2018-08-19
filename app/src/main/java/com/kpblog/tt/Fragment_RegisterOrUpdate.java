package com.kpblog.tt;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kpblog.tt.adapter.AddressAdapter;
import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.Customer;
import com.kpblog.tt.model.CustomerClaimCode;
import com.kpblog.tt.model.CustomerPurchase;
import com.kpblog.tt.util.AsteriskPasswordTransformationMethod;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_RegisterOrUpdate.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_RegisterOrUpdate#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_RegisterOrUpdate extends Fragment implements TextView.OnEditorActionListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private EditText phone, referrerPhone, todayCredit, previousCredit, missingCredit, receiptNum, cashierCode, note;
    private TextInputLayout referrerLayout;
    private Button confirmBtn, cancelBtn;
    private CheckBox optIn;
    private DatabaseHandler handler;
    private ListView listView;
    private AddressAdapter addressAdapter;
    public List<Customer> list;

    // TODO: Rename and change types of parameters
    private String customerId = "";
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private View view;

    public Fragment_RegisterOrUpdate() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param customerId Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_RegisterOrUpdate.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_RegisterOrUpdate newInstance(String customerId, String param2) {
        Fragment_RegisterOrUpdate fragment = new Fragment_RegisterOrUpdate();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, customerId);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            customerId = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_or_update, container, false);

        return view;
    }

    private long confirmBtnLastClicked = 0;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        final PhoneNumberFormattingTextWatcher phoneNumWatcher = new PhoneNumberFormattingTextWatcher();

        phone = (EditText)(getView().findViewById(R.id.phone));
        phone.addTextChangedListener(phoneNumWatcher);
        phone.setOnEditorActionListener(this);
        /*phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus){
                    if (getCustomerInfoFromDatabaseAndUpdateScreen()){
                        //requestFocusOnTodayCredit();
                        //don't request focus here because if the user presses a different input field, then there'd be 2 fields with focus
                    }
                }
            }
        });*/

        referrerLayout = (TextInputLayout) getView().findViewById(R.id.referrerLayout);
        referrerPhone = (EditText) getView().findViewById(R.id.referrerPhone);
        referrerPhone.addTextChangedListener(phoneNumWatcher);
        referrerPhone.setOnEditorActionListener(this);
        referrerPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus){
                    getCustomerInfoFromDatabaseAndUpdateScreen();
                }
            }
        });

        previousCredit = (EditText) getView().findViewById(R.id.previousCredit);
        previousCredit.setText(String.valueOf(0));

        todayCredit = (EditText) getView().findViewById(R.id.todayCredit);
        todayCredit.setTransformationMethod(null);//because we use numberPassword as input, so don't want to mask the input
        todayCredit.setText(String.valueOf(1));
        todayCredit.setOnEditorActionListener(this);
        todayCredit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus){
                    if(isTodayCreditValid()){
                        updateMissingCredit();
                        //((EditText) getView().findViewById(R.id.receiptNumber)).requestFocus();
                        //don't request focus here because if the user press a different input field, then there'll be 2 fields with focus
                    }
                }
                else {
                    getCustomerInfoFromDatabaseAndUpdateScreen();
                }
            }
        });

        note = (EditText) getView().findViewById(R.id.note);

        missingCredit = (EditText) getView().findViewById(R.id.missingCredit);
        missingCredit.setText(String.valueOf(Constants.FREE_DRINK_THRESHOLD - getTodayCredit()));

        receiptNum = (EditText) getView().findViewById(R.id.receiptNumber);
        receiptNum.setOnEditorActionListener(this);
        receiptNum.setTransformationMethod(null);

        cashierCode = (EditText) getView().findViewById(R.id.cashierCode);
        cashierCode.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        cashierCode.setOnEditorActionListener(this);

        optIn = (CheckBox) getView().findViewById(R.id.checkbox_optIn);
        optIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //there's an issue while on todayCredit, user clicks back,
                //after exiting the keyboard, the focus is still on the todayCredit
                todayCredit.clearFocus();
                phone.clearFocus();
            }
        });

        confirmBtn = (Button) getView().findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //to prevent double-click
                if (SystemClock.elapsedRealtime() - confirmBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD){
                    registerOrUpdateCustomer();
                    confirmBtnLastClicked = SystemClock.elapsedRealtime();
                }
            }
        });

        cancelBtn = (Button) getView().findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                gotoHomeScreen();
            }
        });

        handler = new DatabaseHandler(getContext());
        /*List<CustomerPurchase> cpList = handler.getAllCustomerPurchase();
        Log.d("CustomerPurchase: ", cpList.toString());*/

        if (Util.getUnformattedPhoneNumber(customerId).length() ==10){
            //handle on reload of the tab, if there's a valid phone number there, reload the info
            phone.setText(customerId);
            getCustomerInfoFromDatabaseAndUpdateScreen();
        }

        //uncomment to see the db entries on screen
        /*listView = (ListView) getView().findViewById(R.id.addressListView);
        list = handler.getAllAddress();
        addressAdapter = new AddressAdapter(this);
        listView.setAdapter(addressAdapter);*/
    }

    /**
     * if new user, then referrer box is enable.
     * else, populate the referrer info and disable the input
     */
    private void loadReferrerInfo(Customer c) {
        if (c == null){
            referrerLayout.setVisibility(View.VISIBLE);
            if (!referrerPhone.isEnabled()){
                //if it was disabled before, clear out the text before enable it
                //we dont want to clear out the text that the user just entered
                referrerPhone.setText("");
            }
            referrerPhone.setEnabled(true);
        }
        else {
            final String referrerId = c.getReferrerId();
            if (referrerId != null && !referrerId.isEmpty()){
                referrerPhone.setText(Util.formatPhoneNumber(referrerId));
                referrerPhone.setEnabled(false);
                referrerLayout.setVisibility(View.VISIBLE);
            }
            else {
                referrerLayout.setVisibility(View.INVISIBLE);
            }
        }
    }


    private void requestFocusOnPhone() {
        final View view = getView();
        if (view != null){
            EditText phone = (EditText) view.findViewById(R.id.phone);
            if (phone != null){
                phone.requestFocus(phone.getText().length());
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser){
            requestFocusOnPhone();
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

    private boolean isTodayCreditValid() {
        boolean isValid = false;
        EditText todayCreditEditText = (EditText)(getView().findViewById(R.id.todayCredit));
        final String todayCreditStr = todayCreditEditText.getText().toString();
        if (todayCreditStr != null && todayCreditStr.matches(Constants.AT_LEAST_ONE_DIGIT_REGEXP)){
            int todayCredit = Integer.parseInt(todayCreditStr);
            isValid = (todayCredit < Constants.TODAY_CREDIT_LIMIT);
        }

        TextInputLayout todayCreditLayout = (TextInputLayout) getView().findViewById(R.id.todayCreditlayout);
        if (isValid){
            todayCreditLayout.setErrorEnabled(false);
        }
        else {
            todayCreditLayout.setError(getString(R.string.todayCredit_err_msg));
            //((EditText) getView().findViewById(R.id.todayCredit)).requestFocus();
        }
        return isValid;
    }


    String textMsg;
    List<String> targetPhoneNums;
    private void requestPermissionAndSendText(List<String> phoneNums, String msg) {
        try {
            textMsg = msg;
            targetPhoneNums = phoneNums;
            requestSmsPermission();
            //Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getContext().getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }


    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 123;

    private void requestSmsPermission() {

        // check permission is given
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {
            // permission already granted run sms send
            sendSms(targetPhoneNums, textMsg);
            Toast.makeText(getContext().getApplicationContext(), getString(R.string.cashier_toast_msg_sent), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    sendSms(targetPhoneNums, textMsg);
                    Toast.makeText(getContext().getApplicationContext(), getString(R.string.cashier_toast_msg_sent), Toast.LENGTH_LONG).show();
                } else {
                    // permission denied
                    Toast.makeText(getContext().getApplicationContext(), "permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void sendSms(List<String> phoneNumbers, String message){
        SmsManager sms = SmsManager.getDefault();
        for (String phoneNum : phoneNumbers){
            sms.sendTextMessage(phoneNum, null, message, null, null);
        }
        gotoHomeScreen();
    }

    private void gotoHomeScreen() {
        //clear all fields
        phone.setText("");
        TextInputLayout phoneLayout = (TextInputLayout) getView().findViewById(R.id.phoneLayout);
        phoneLayout.setErrorEnabled(false);

        referrerPhone.setText("");
        referrerPhone.setEnabled(true);
        referrerLayout.setErrorEnabled(false);
        referrerLayout.setVisibility(View.VISIBLE);

        previousCredit.setText("0");

        TextInputLayout todayCreditLayout = (TextInputLayout) getView().findViewById(R.id.todayCreditlayout);
        todayCreditLayout.setErrorEnabled(false);
        todayCredit.setText(String.valueOf(1));

        note.setText("");
        TextInputLayout noteLayout = (TextInputLayout) getView().findViewById(R.id.noteLayout);
        noteLayout.setVisibility(View.INVISIBLE);

        receiptNum.setText("");
        TextInputLayout receiptLayout = (TextInputLayout) getView().findViewById(R.id.receiptLayout);
        receiptLayout.setErrorEnabled(false);

        cashierCode.setText("");
        TextInputLayout cashierCodeLayout = (TextInputLayout) getView().findViewById(R.id.cashierCodeLayout);
        cashierCodeLayout.setErrorEnabled(false);

        missingCredit.setText(String.valueOf(Constants.FREE_DRINK_THRESHOLD - getTodayCredit()));

        optIn.setChecked(false);
        optIn.setVisibility(View.VISIBLE);

        phone.requestFocus();
    }

    private void updateMissingCredit() {
        //done with editing
        EditText missingCreditView = (EditText) getView().findViewById(R.id.missingCredit);
        double previousCreditValue = getPreviousCredit();

        int todayCredit = getTodayCredit();
        double totalCredit = previousCreditValue + todayCredit;
        missingCreditView = (EditText) getView().findViewById(R.id.missingCredit);
        final double missingCredit = Constants.FREE_DRINK_THRESHOLD - totalCredit;

        if (missingCredit > 0){
            ((TextInputLayout) getView().findViewById(R.id.missingCreditlayout)).setHintEnabled(true);
            missingCreditView.setText(String.valueOf(missingCredit));
        }
        else {
            //qualifies for free drink
            missingCreditView.setText(R.string.freeDrinkAchieved);
            //((TextInputLayout) getView().findViewById(R.id.missingCreditlayout)).setHintEnabled(false);
        }
    }


    private double getPreviousCredit() {
        double previousCreditValue = 0;
        final String previousCreditStr = previousCredit.getText().toString();
        if (!previousCreditStr.isEmpty()){
            previousCreditValue = Double.parseDouble(previousCreditStr);
        }
        return previousCreditValue;
    }

    private int getTodayCredit() {
        final String todayCreditStr = todayCredit.getText().toString();
        int todayCredit = 0;
        if (todayCreditStr != null && !todayCreditStr.isEmpty()){
            todayCredit = Integer.parseInt(todayCreditStr);
        }
        return todayCredit;
    }

    @Override
    //This is when the user press done on the keypad after entering phone number
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;
        String id = v.getResources().getResourceEntryName(v.getId());
        String phoneId = phone.getResources().getResourceEntryName(phone.getId());
        String todayCreditId = todayCredit.getResources().getResourceEntryName(todayCredit.getId());
        String receiptNumId = receiptNum.getResources().getResourceEntryName(receiptNum.getId());
        String cashierCodeId = cashierCode.getResources().getResourceEntryName(cashierCode.getId());
        String referrerId = referrerPhone.getResources().getResourceEntryName(referrerPhone.getId());

        if (id.equals(phoneId)) {
            //when user is done entering phone number
            if (getCustomerInfoFromDatabaseAndUpdateScreen()){
                //don't request the focus if the phone number entry isnt valid
                if (referrerPhone.isEnabled() && referrerLayout.getVisibility() == View.VISIBLE){
                    referrerPhone.requestFocus();
                }
                else {
                    requestFocusOnTodayCredit();
                }
            }
            handled = true;
        }
        else if (id.equals(referrerId)){
            if (validateReferrerId()){
                requestFocusOnTodayCredit();
            }
        }
        else if(id.equals(todayCreditId)){
            //when user is done entering today's drink
            if (isTodayCreditValid()){
                updateMissingCredit();
                receiptNum.requestFocus();
            }
        }
        else if (id.equals(receiptNumId)){
            if (isReceiptNumberValid()){
                cashierCode.requestFocus();
            }
        }

        else if (id.equals(cashierCodeId)){
            isCashierCodeValid();
        }
        return handled;
    }

    /**
     * if referrerId given: validate valid phone number and customer exists in db
     * @return
     */
    private boolean validateReferrerId() {
        boolean isValid = true;
        String referrerIdStr = referrerPhone.getText().toString();
        if (referrerIdStr != null && !referrerIdStr.isEmpty()){
            String unformattedReferrerId = Util.getUnformattedPhoneNumber(referrerIdStr);
            if (unformattedReferrerId.matches(Constants.AT_LEAST_ONE_DIGIT_REGEXP) &&
                    handler.getCustomerById(unformattedReferrerId) != null){
                isValid = true;
                referrerLayout.setErrorEnabled(false);
            }
            else {
                isValid = false;
                referrerLayout.setError(getString(R.string.invalidReferrerId));
            }
        }

        return isValid;
    }

    private boolean isCashierCodeValid() {
        boolean isValid = false;
        String codeStr = cashierCode.getText().toString();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String expectedCode = sp.getString(Constants.SHARED_PREF_DAILY_CODE_KEY, null);

        TextInputLayout cashierLayout = (TextInputLayout) getView().findViewById(R.id.cashierCodeLayout);

        if (!codeStr.isEmpty() && codeStr.equals(expectedCode)){
            isValid = true;
            cashierLayout.setErrorEnabled(false);
        }
        else {
            cashierLayout.setError(getString(R.string.claimCode_err_msg));
        }

        return isValid;
    }

    private void requestFocusOnTodayCredit() {
        //clearCurrentFocus();
        todayCredit = (EditText) getView().findViewById(R.id.todayCredit);
        todayCredit.requestFocus();
        todayCredit.setSelection(todayCredit.getText().length());
    }

    private boolean isReceiptNumberValid(){
        boolean isValid = false;
        receiptNum = (EditText) getView().findViewById(R.id.receiptNumber);
        String receiptNumStr = receiptNum.getText().toString();

        TextInputLayout receiptNumLayout = (TextInputLayout) getView().findViewById(R.id.receiptLayout);
        if (receiptNumStr != null && receiptNumStr.matches(Constants.AT_LEAST_ONE_DIGIT_REGEXP)){
            receiptNumLayout.setErrorEnabled(false);
            isValid = true;
        }
        else {
            receiptNumLayout.setError(getString(R.string.receipt_err_msg));
            //receiptNum.requestFocus();
        }

        return isValid;
    }


    private boolean getCustomerInfoFromDatabaseAndUpdateScreen() {
        final String phone = Util.getUnformattedPhoneNumber(this.phone.getText().toString());

        if (!Util.isPhoneNumberValid((TextInputLayout) getView().findViewById(R.id.phoneLayout), getString(R.string.phone_err_msg), phone)){
            return false;
        }

        //get customer by phone, if not exist, add
        Customer customer = handler.getCustomerById(phone);

        if (customer != null){
            //existing customer, update the screen with customer info

            int todayCredit = getTodayCredit();
            double previousCreditValue = customer.getTotalCredit();
            if (customer.isOptIn()){
                //hide the checkbox if customer already opted in
                ((CheckBox) getView().findViewById(R.id.checkbox_optIn)).setVisibility(View.INVISIBLE);
            }
            else {
                //handles when 1 number was entered, then another one was enter
                ((CheckBox) getView().findViewById(R.id.checkbox_optIn)).setVisibility(View.VISIBLE);
            }

            previousCredit = (EditText) getView().findViewById(R.id.previousCredit);
            previousCredit.setText(String.valueOf(previousCreditValue));

            updateMissingCredit();

            if (previousCreditValue >= Constants.FREE_DRINK_THRESHOLD){
                //take user to claim tab
                String phoneNum = Util.getUnformattedPhoneNumber(this.phone.getText().toString());
                Fragment_Claim claim = Fragment_Claim.newInstance(phoneNum, null);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.claimFragment,claim).commit();
                TabLayout tabs = (TabLayout)((MainActivity)getActivity()).findViewById(R.id.tabs);
                tabs.getTabAt(1).select();
            }
        }
        else {
            //new customer
            //handles the case where customer looked up 1 number, and then enter a different number, so need to reload
            int todayCredit = getTodayCredit();
            ((CheckBox) getView().findViewById(R.id.checkbox_optIn)).setVisibility(View.VISIBLE);

            int previousCreditValue = 0;
            previousCredit = (EditText) getView().findViewById(R.id.previousCredit);
            previousCredit.setText(String.valueOf(previousCreditValue));

            updateMissingCredit();
        }


        loadReferrerInfo(customer);

        return true;
    }


    public void deleteCustomer(Customer customer){
        handler.deleteCustomer(customer);
    }

    private boolean isAllInputValid(){
        final String phone = Util.getUnformattedPhoneNumber(this.phone.getText().toString());
        return Util.isPhoneNumberValid((TextInputLayout) getView().findViewById(R.id.phoneLayout), getString(R.string.phone_err_msg), phone)
                && isTodayCreditValid() && isReceiptNumberValid() && isCashierCodeValid();
    }

    /**
     * register new customer, and update existing customer.
     * using SQLiteDatabase.insertWithOnConflict() using CONFLICT_IGNORE to avoid overwriting existing data
     * https://stackoverflow.com/questions/13311727/android-sqlite-insert-or-update
     */
    private boolean registerOrUpdateCustomer() {
        if (!isAllInputValid()){
            return false;
        }

        boolean success = false;
        try {
            final Date today = new Date(Calendar.getInstance().getTime().getTime());
            final String unformattedPhoneNumber = Util.getUnformattedPhoneNumber(this.phone.getText().toString());

            boolean isNewCustomer = false;
            Customer customer = handler.getCustomerById(unformattedPhoneNumber);
            if (customer == null){
                customer = new Customer();
                customer.setCustomerId(unformattedPhoneNumber);
                customer.setReferrerId(Util.getUnformattedPhoneNumber(referrerPhone.getText().toString()));
                isNewCustomer = true;
            }

            //only set the opt-in date if it's checked.
            //if customer already opted-in, we'd hide the checkbox. They can opt-out from the ADMINS screen
            if (optIn.isChecked()){
                customer.setOptIn(optIn.isChecked());
                customer.setOptInDate(today);

                //clear out opt-out date
                customer.setOptOutDate(null);
            }
            final int todayCredit = getTodayCredit();
            customer.setPurchaseCredit(customer.getPurchaseCredit() + todayCredit);
            customer.setLastVisitDate(today);

            handler.registerOrUpdateCustomer(customer, isNewCustomer);
            insertCustomerPurchase(customer.getCustomerId(), todayCredit);

            creditReferrer(customer, isNewCustomer, todayCredit);

            if (todayCredit > Constants.SINGLE_PURCHASE_QUANTITY_LIMIT){
                final int receiptNum = Integer.parseInt(this.receiptNum.getText().toString());
                sendAlertTextToAdmin(customer, todayCredit, receiptNum);
            }

            if (customer.isOptIn()){
                //send confirmation, based on total credit
                double totalCredit = customer.getTotalCredit();

                List<String> phoneNumbers = new ArrayList<String>();
                phoneNumbers.add(customer.getCustomerId());

                if (totalCredit >= Constants.FREE_DRINK_THRESHOLD){
                    //send code for free drink
                    final String codeStr = Util.generateRandom4DigitCode();
                    String msg = String.format(getString(R.string.purchase_conf_msg_free), todayCredit, totalCredit, codeStr);
                    requestPermissionAndSendText(phoneNumbers, msg);
                    insertOrUpdateClaimCodeDb(customer.getCustomerId(), codeStr);
                }
                else {
                    //send updated credit
                    double missingCredit = Constants.FREE_DRINK_THRESHOLD - totalCredit;
                    String msg = String.format(getString(R.string.purchase_conf_msg_notFree), todayCredit, totalCredit, missingCredit);
                    requestPermissionAndSendText(phoneNumbers, msg);
                }
            }
            else {
                Toast.makeText(getContext().getApplicationContext(), getString(R.string.update_success_msg), Toast.LENGTH_LONG).show();
                gotoHomeScreen();
            }


            success = true;
        }
        catch (Exception e){
            Log.d("CustomerPurchase", e.getMessage());
        }

        return success;
    }

    private void creditReferrer(Customer customer, boolean isNewCustomer, int todayPurchaseAmount) {
        final String immediateReferrerId = customer.getReferrerId();
        if (!immediateReferrerId.isEmpty()){
            /**
             * if new customer, give referrer 1st purchase credit,
             * else give normal credit
             */
            Customer immediateReferrer = handler.getCustomerById(immediateReferrerId);
            if (isNewCustomer){
                //1st purchase: credit immediate referral with 1st purchase referral credit
                handler.updateReferrerCredit(immediateReferrerId, Constants.FIRST_PURCHASE_IMMEDIATE_REFERRAL_CREDIT);
            }
            else {
                //subsequent purchase: credit immediate referral with immediateReferralRate * todayPurchaseAmount
                handler.updateReferrerCredit(immediateReferrerId, Constants.IMMEDIATE_REFERRAL_CREDIT_RATE * todayPurchaseAmount);

            }

            //for second level, we don't distinguish between 1st or subsequent purchase
            String secondLevelReferrer = immediateReferrer.getReferrerId();
            if (secondLevelReferrer != null && !secondLevelReferrer.isEmpty()){
                handler.updateReferrerCredit(secondLevelReferrer, Constants.SECOND_LEVEL_REFERRAL_CREDIT_RATE * todayPurchaseAmount);
            }
        }
    }

    private void sendAlertTextToAdmin(Customer customer, int todayCredit, int receiptNum) {
        java.util.Date purchaseDate = new java.util.Date();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(purchaseDate);
        String textMsg = String.format(getString(R.string.singlePurchaseLimitAlert), customer.getCustomerId(), todayCredit, receiptNum, dateStr);
        requestPermissionAndSendText(handler.getAllAdmins(), textMsg);
    }

    private void insertOrUpdateClaimCodeDb(String phoneNumber, String codeStr) {
        CustomerClaimCode cc = new CustomerClaimCode();
        cc.setCustomerId(phoneNumber);
        cc.setClaimCode(codeStr);
        cc.setIssuedDate(new java.util.Date());

        handler.insertOrUpdateCustomerClaimCode(cc);
    }

    private void insertCustomerPurchase(String customerId, int todayCredit) {
        CustomerPurchase cp = new CustomerPurchase();

        cp.setCustomerId(customerId);
        cp.setQuantity(todayCredit);
        cp.setPurchaseDate(new java.util.Date());
        cp.setReceiptNum(Integer.parseInt(receiptNum.getText().toString()));

        handler.insertCustomerPurchase(cp);
    }
}
